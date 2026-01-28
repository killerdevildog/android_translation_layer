#include <androidfw/AssetManager2.h>
#include <androidfw/AttributeResolution.h>
#include <androidfw/ResourceTypes.h>
#include <androidfw/androidfw_c_api.h>
#include "include/androidfw/ResourceTypes.h"

/*
 * wrapping-friendly analogue to android::Guarded<GuardedAssetManager2>
 * according to comments in AOSP source, keeping the mutex next to the rest of the class
 * improves cache locality.
 */
class GuardedAssetManager2 : public android::AssetManager2 {
  std::mutex _mutex;

 public:
  void _lock_mutex() { _mutex.lock(); }
  void _unlock_mutex() { _mutex.unlock(); }
};

extern "C" const struct ApkAssets *ApkAssets_load(const char *path, bool system)
{
  const android::ApkAssets *apkAssets = android::ApkAssets::Load(path, system).release();
  return reinterpret_cast<const struct ApkAssets *>(apkAssets);
}

extern "C" const struct ApkAssets *ApkAssets_loadDir(const char *path)
{
  const android::ApkAssets *apkAssets = android::ApkAssets::Load(android::DirectoryAssetsProvider::Create(path)).release();
  return reinterpret_cast<const struct ApkAssets *>(apkAssets);
}

extern "C" struct AssetManager *AssetManager_new(void)
{
  GuardedAssetManager2 *assetManager = new GuardedAssetManager2();
  return reinterpret_cast<struct AssetManager *>(assetManager);
}

extern "C" void AssetManager_lock(struct AssetManager *asset_manager)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  assetManager->_lock_mutex();
}

extern "C" void AssetManager_unlock(struct AssetManager *asset_manager)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  assetManager->_unlock_mutex();
}

extern "C" void AssetManager_setConfiguration(struct AssetManager *asset_manager, const ResTable_config *config/*, const char* locale*/)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  assetManager->SetConfigurations({*reinterpret_cast<const android::ResTable_config *>(config)});
}

// FIXME filter_incompatible_configs
extern "C" bool AssetManager_setApkAssets(struct AssetManager *asset_manager,
                                          const struct ApkAssets* apk_assets[], size_t num_assets,
                                          bool invalidate_caches, bool filter_incompatible_configs)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);

  std::vector<GuardedAssetManager2::ApkAssetsPtr> apkAssets_vec;
  for (size_t i = 0; i < num_assets; i++) {
    apkAssets_vec.push_back(((const android::ApkAssets**)apk_assets)[i]);
  }
  const GuardedAssetManager2::ApkAssetsList *apkAssets = new const GuardedAssetManager2::ApkAssetsList(apkAssets_vec.begin(), apkAssets_vec.size());
  auto op = assetManager->StartOperation();
  return assetManager->SetApkAssets(*apkAssets, invalidate_caches);
}

const struct ResStringPool *AssetManager_getStringPoolForCookie(struct AssetManager *asset_manager, ApkAssetsCookie cookie)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  return reinterpret_cast<const struct ResStringPool *>(assetManager->GetStringPoolForCookie(cookie));
}

extern "C" const struct ResolvedBag *AssetManager_getBag(struct AssetManager *asset_manager, uint32_t resid)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  auto ret = assetManager->GetBag(resid);
  return reinterpret_cast<const struct ResolvedBag *>(ret.has_value() ? ret.value() : NULL);
}

extern "C" ApkAssetsCookie AssetManager_getResource(struct AssetManager *asset_manager, uint32_t resid,
                                                    bool may_be_bag, uint16_t density_override,
                                                    struct Res_value *out_value, struct ResTable_config *out_selected_config,
                                                    uint32_t *out_flags)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  android::Res_value *outValue = reinterpret_cast<android::Res_value *>(out_value);
  android::ResTable_config *outConfig = reinterpret_cast<android::ResTable_config *>(out_selected_config);

  auto ret = assetManager->GetResource(resid, may_be_bag, density_override/*, outValue, outConfig, out_flags*/);
  if(!ret.has_value())
    return android::kInvalidCookie;

  GuardedAssetManager2::SelectedValue sel_value = ret.value();

  outValue->size = sizeof(struct Res_value);
  outValue->dataType = sel_value.type;
  outValue->data = sel_value.data;
  *outConfig = sel_value.config;
  *out_flags = sel_value.flags;

  return sel_value.cookie;
}

extern "C" ApkAssetsCookie AssetManager_resolveReference(struct AssetManager *asset_manager, ApkAssetsCookie cookie,
                                                         struct Res_value *in_out_value, struct ResTable_config* in_out_selected_config,
                                                         uint32_t *in_out_flags, uint32_t *out_last_reference)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  android::Res_value *inOutValue = reinterpret_cast<android::Res_value *>(in_out_value);
  android::ResTable_config *inOutSelectedConfig = reinterpret_cast<android::ResTable_config *>(in_out_selected_config);

  GuardedAssetManager2::SelectedValue sel_value;
  sel_value.cookie = cookie;
  sel_value.data = inOutValue->data;
  sel_value.type = inOutValue->dataType;
  sel_value.flags = *in_out_flags;
  sel_value.resid = 0;
  sel_value.config = *inOutSelectedConfig;

  auto ret = assetManager->ResolveReference(sel_value);
  if(!ret.has_value())
    return android::kInvalidCookie;

  inOutValue->size = sizeof(struct Res_value);
  inOutValue->dataType = sel_value.type;
  inOutValue->data = sel_value.data;
  *inOutSelectedConfig = sel_value.config;
  *in_out_flags = sel_value.flags;
  *out_last_reference = sel_value.resid;

  return sel_value.cookie;
}

extern "C" uint32_t AssetManager_getResourceId(struct AssetManager *asset_manager, const char *name, const char *type, const char *package)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  auto ret = assetManager->GetResourceId(name, type, package);
  return ret.has_value() ? ret.value() : 0;
}

extern "C" uint32_t AssetManager_getResourceName(struct AssetManager *asset_manager, uint32_t resid, struct resource_name* outName)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  auto ret = assetManager->GetResourceName(resid);
  if(!ret.has_value())
    return false;

  *outName = *reinterpret_cast<struct resource_name *>(&ret.value());

  return true;
}

extern "C" struct Theme *AssetManager_newTheme(struct AssetManager *asset_manager)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  android::Theme *theme = assetManager->NewTheme().release();
  return reinterpret_cast<struct Theme *>(theme);
}

extern "C" void Theme_delete(struct Theme *theme)
{
  android::Theme *themePtr = reinterpret_cast<android::Theme *>(theme);
  delete themePtr;
}

extern "C" void Theme_applyStyle(struct Theme *theme, uint32_t style, bool force)
{
  android::Theme *themePtr = reinterpret_cast<android::Theme *>(theme);
  themePtr->ApplyStyle(style, force);
}

extern "C" ApkAssetsCookie Theme_getAttribute(const struct Theme *theme, uint32_t resID, struct Res_value *out_value, uint32_t *out_flags)
{
  const android::Theme *themePtr = reinterpret_cast<const android::Theme *>(theme);
  android::Res_value *outValue = reinterpret_cast<android::Res_value *>(out_value);
  auto ret = themePtr->GetAttribute(resID);

  if(!ret.has_value())
    return android::kInvalidCookie;

  GuardedAssetManager2::SelectedValue sel_value = ret.value();

  outValue->size = sizeof(struct Res_value);
  outValue->dataType = sel_value.type;
  outValue->data = sel_value.data;
  *out_flags = sel_value.flags;

  return sel_value.cookie;
}

extern "C" void Theme_setTo(struct Theme *theme, const struct Theme *other)
{
  android::Theme *themePtr = reinterpret_cast<android::Theme *>(theme);
  themePtr->SetTo(*reinterpret_cast<const android::Theme *>(other));
}

extern "C" struct Asset *AssetManager_openNonAsset(struct AssetManager *asset_manager, const char* filename, enum AccessMode mode)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  android::Asset *asset = assetManager->OpenNonAsset(filename, (android::Asset::AccessMode)mode).release();
  return reinterpret_cast<struct Asset *>(asset);
}

extern "C" struct AssetDir *AssetManager_openDir(struct AssetManager *asset_manager, const char* dirName)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  return reinterpret_cast<struct AssetDir *>(assetManager->OpenDir(dirName).release());
}

extern "C" char **AssetManager_getLocales(struct AssetManager *asset_manager, bool exclude_system, bool merge_equivalent_languages)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  std::set<std::string> locales = assetManager->GetResourceLocales(exclude_system, merge_equivalent_languages);
  size_t size = locales.size();
  char **array = new char*[size+1];
  int i = 0;
  for (std::string locale : locales) {
    array[i++] = strdup(locale.c_str());
  }
  array[size] = NULL;
  return array;
}

extern "C" int Asset_openFileDescriptor(struct Asset *asset, off64_t* outStart, off64_t* outLength)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  return assetPtr->openFileDescriptor(outStart, outLength);
}

extern "C" const void *Asset_getBuffer(struct Asset *asset, bool wordAligned)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  return assetPtr->getBuffer(wordAligned);
}

extern "C" int Asset_read(struct Asset *asset, void *buf, size_t count)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  return assetPtr->read(buf, count);
}

extern "C" off64_t Asset_seek(struct Asset *asset, off_t offset, int whence)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  return assetPtr->seek(offset, whence);
}

extern "C" size_t Asset_getLength(struct Asset *asset)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  return assetPtr->getLength();
}

extern "C" size_t Asset_getRemainingLength(struct Asset *asset)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  return assetPtr->getRemainingLength();
}

extern "C" void Asset_delete(struct Asset *asset)
{
  android::Asset *assetPtr = reinterpret_cast<android::Asset *>(asset);
  assetPtr->close();
  delete assetPtr;
}

extern "C" size_t AssetDir_getFileCount(struct AssetDir *assetDir)
{
  android::AssetDir *assetDirPtr = reinterpret_cast<android::AssetDir *>(assetDir);
  return assetDirPtr->getFileCount();
}

extern "C" enum file_type AssetDir_getFileType(struct AssetDir *assetDir, int index)
{
  android::AssetDir *assetDirPtr = reinterpret_cast<android::AssetDir *>(assetDir);
  return (enum file_type)assetDirPtr->getFileType(index);
}

extern "C" const char *AssetDir_getFileName(struct AssetDir *assetDir, int index)
{
  android::AssetDir *assetDirPtr = reinterpret_cast<android::AssetDir *>(assetDir);
  return assetDirPtr->getFileName(index).c_str();
}

extern "C" void AssetDir_delete(struct AssetDir *assetDir)
{
  android::AssetDir *assetDirPtr = reinterpret_cast<android::AssetDir *>(assetDir);
  delete assetDirPtr;
}

extern "C" struct ResXMLTree *ResXMLTree_new(void)
{
  android::ResXMLTree *tree = new android::ResXMLTree();
  return reinterpret_cast<struct ResXMLTree *>(tree);
}

extern "C" void ResXMLTree_setTo(struct ResXMLTree *tree, const void *data, size_t size, bool copyData)
{
  android::ResXMLTree *treePtr = reinterpret_cast<android::ResXMLTree *>(tree);
  treePtr->setTo(data, size, copyData);
}

extern "C" void ResXMLTree_delete(struct ResXMLTree *tree)
{
  android::ResXMLTree *treePtr = reinterpret_cast<android::ResXMLTree *>(tree);
  delete treePtr;
}

extern "C" const struct ResStringPool* ResXMLTree_getStrings(struct ResXMLTree *tree){
  android::ResXMLTree *treePtr = reinterpret_cast<android::ResXMLTree *>(tree);
  return reinterpret_cast<const struct ResStringPool*>(&treePtr->getStrings());
}

extern "C" struct ResXMLParser *ResXMLParser_new(const struct ResXMLTree *tree)
{
  const android::ResXMLTree *treePtr = reinterpret_cast<const android::ResXMLTree *>(tree);
  android::ResXMLParser *parser = new android::ResXMLParser(*treePtr);
  return reinterpret_cast<struct ResXMLParser *>(parser);
}

extern "C" void ResXMLParser_restart(struct ResXMLParser *parser)
{
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(parser);
  parserPtr->restart();
}

extern "C" enum event_code_t ResXMLParser_next(struct ResXMLParser *parser)
{
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(parser);
  return (enum event_code_t)parserPtr->next();
}

extern "C" int ResXMLParser_getElementNameID(struct ResXMLParser *parser)
{
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(parser);
  return parserPtr->getElementNameID();
}

extern "C" const struct ResStringPool *ResXMLParser_getStrings(struct ResXMLParser *parser)
{
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(parser);
  const android::ResStringPool &pool = parserPtr->getStrings();
  return reinterpret_cast<const struct ResStringPool *>(&pool);
}

extern "C" size_t ResXMLParser_getAttributeCount(const struct ResXMLParser *parser)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getAttributeCount();
}

extern "C" uint32_t ResXMLParser_getAttributeNameID(const struct ResXMLParser *parser, size_t idx)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getAttributeNameID(idx);
}

extern "C" uint32_t ResXMLParser_getAttributeNameResID(const struct ResXMLParser *parser, size_t idx)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getAttributeNameResID(idx);
}

extern "C" ssize_t ResXMLParser_indexOfAttribute(const struct ResXMLParser *parser, const char16_t* ns, size_t nsLen, const char16_t* attr, size_t attrLen)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->indexOfAttribute(ns, nsLen, attr, attrLen);
}

extern "C" int32_t ResXMLParser_getAttributeValueStringID(const struct ResXMLParser *parser, size_t idx)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getAttributeValueStringID(idx);
}

extern "C" uint32_t ResXMLParser_getLineNumber(const struct ResXMLParser *parser)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getLineNumber();
}

extern "C" int32_t ResXMLParser_getAttributeDataType(const struct ResXMLParser *parser, size_t idx)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getAttributeDataType(idx);
}

extern "C" int32_t ResXMLParser_getAttributeData(const struct ResXMLParser *parser, size_t idx)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getAttributeData(idx);
}

extern "C" uint32_t ResXMLParser_getSourceResId(const struct ResXMLParser *parser)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->getSourceResourceId();
}

extern "C" void ResXMLParser_delete(struct ResXMLParser *parser)
{
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(parser);
  delete parserPtr;
}

extern"C" ssize_t ResXMLParser_indexOfID(const struct ResXMLParser *parser)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->indexOfID();
}

extern "C" ssize_t ResXMLParser_indexOfClass(const struct ResXMLParser *parser)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->indexOfClass();
}

extern "C" ssize_t ResXMLParser_indexOfStyle(const struct ResXMLParser *parser)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  return parserPtr->indexOfStyle();
}

extern "C" void ResXMLParser_getAttributeValue(const struct ResXMLParser *parser, size_t idx, struct Res_value *outValue)
{
  const android::ResXMLParser *parserPtr = reinterpret_cast<const android::ResXMLParser *>(parser);
  parserPtr->getAttributeValue(idx, reinterpret_cast<android::Res_value *>(outValue));
}

extern "C" struct ResStringPool *ResStringPool_new(const void* data, size_t size, bool copyData)
{
  android::ResStringPool *stringPool = new android::ResStringPool(data, size, copyData);
  return reinterpret_cast<struct ResStringPool*>(stringPool);
}

extern "C" size_t ResStringPool_getSize(const struct ResStringPool *res_string_pool)
{
  const android::ResStringPool *stringPool = reinterpret_cast<const android::ResStringPool *>(res_string_pool);
  return stringPool->size();
}

extern "C" const char* ResStringPool_string8At(const struct ResStringPool *res_string_pool, size_t idx, size_t *outLen)
{
  const android::ResStringPool *stringPool = reinterpret_cast<const android::ResStringPool *>(res_string_pool);
  auto ret = stringPool->string8At(idx);
  if(!ret.has_value())
    return NULL;

  std::basic_string_view<char> string = ret.value();

  *outLen = string.length();
  return string.data();
}

extern "C" const char16_t* ResStringPool_stringAt(const struct ResStringPool *res_string_pool, size_t idx, size_t *outLen)
{
  const android::ResStringPool *stringPool = reinterpret_cast<const android::ResStringPool *>(res_string_pool);
  auto ret = stringPool->stringAt(idx);

  if(!ret.has_value())
    return NULL;

  std::basic_string_view<char16_t> string = ret.value();

  *outLen = string.length();
  return string.data();
}

extern "C" android::status_t ResStringPool_getErrors(const struct ResStringPool *res_string_pool){
  const android::ResStringPool *stringPool = reinterpret_cast<const android::ResStringPool *>(res_string_pool);
  return stringPool->getError();
}

extern "C" void ResStringPool_delete(const struct ResStringPool *res_string_pool)
{
  const android::ResStringPool *stringPool = reinterpret_cast<const android::ResStringPool *>(res_string_pool);
  delete stringPool;
}

extern "C" bool ResolveAttrs(struct Theme *theme, uint32_t def_style_attr, uint32_t def_style_res,
                             uint32_t* src_values, size_t src_values_length,
                             uint32_t *attrs, size_t attrs_length,
                             uint32_t *out_values, uint32_t* out_indices)
{
  android::Theme *themePtr = reinterpret_cast<android::Theme *>(theme);
  auto ret = android::ResolveAttrs(themePtr, def_style_attr, def_style_res,
                               src_values, src_values_length,
                               attrs, attrs_length,
                               out_values, out_indices);
  return ret.has_value() ? true : false;
}

extern "C" void ApplyStyle(struct Theme *theme, struct ResXMLParser *xml_parser,
                           uint32_t def_style_attr, uint32_t def_style_resid,
                           const uint32_t *attrs, size_t attrs_length,
                           uint32_t *out_values, uint32_t *out_indices)
{
  android::Theme *themePtr = reinterpret_cast<android::Theme *>(theme);
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(xml_parser);
  android::ApplyStyle(themePtr, parserPtr, def_style_attr, def_style_resid, attrs, attrs_length, out_values, out_indices);
}

extern "C" bool RetrieveAttributes(struct AssetManager* asset_manager, struct ResXMLParser *xml_parser,
                                   uint32_t *attrs, size_t attrs_length,
                                   uint32_t *out_values, uint32_t *out_indices)
{
  GuardedAssetManager2 *assetManager = reinterpret_cast<GuardedAssetManager2 *>(asset_manager);
  android::ResXMLParser *parserPtr = reinterpret_cast<android::ResXMLParser *>(xml_parser);
  auto ret = android::RetrieveAttributes(assetManager, parserPtr, attrs, attrs_length, out_values, out_indices);

  return ret.has_value() ? true : false;
}
