### dependencies

debian based distros:
`sudo apt install default-jdk python-is-python3 meson libegl-dev pkg-config libbsd-dev libunwind-dev libelf-dev valgrind libicu-dev libssl-dev libexpat1-dev zip liblz4-dev liblzma-dev libcap-dev`
The libwolfssl needs to be installed from source with the --enable-jni configuration, because this option is disabled in debian package. A known working configuration for libwolfssl is:
`./configure --enable-shared --disable-opensslall --disable-opensslextra --enable-aescbc-length-checks --enable-curve25519 --enable-ed25519 --enable-ed25519-stream --enable-oldtls --enable-base64encode --enable-tlsx --enable-scrypt --disable-examples --enable-crl --with-rsa --enable-certs --enable-session-certs --enable-encrypted-keys --enable-cert-gen --enable-cert-ext --enable-clr-monitor --enable-jni`

### compile

`make` or `make ____LIBDIR=[XXX]` for custom install location (see Makefile for documentation on other options which you might want to override)
`ARCH=x86` may be needed to build for 32-bit x86, since the automatic architecture detection uses `uname`

after compilation, the output is in `out/host/linux-x86/`; `out/host/linux-x86/{gen,obj}` can and should be discarded, as it only contains intermediates  
aarch64 build currently also places the build output in `out/host/linux-x86/`;

### install

`make install` or `make ____PREFIX=[XXX] ____LIBDIR=[XXX] install` (see above)

after installation, it should be possible to execute dalvikvm as such: `[ANDROID_DATA=/tmp/dalvik-data] dalvikvm[64] --help`, resp. `[ANDROID_DATA=/tmp/dalvik-data] dalvikvm[64] -cp path-to-dex class-that-implements-a-main-method`

### other stuff in this repo

While not technically part of art, because of using the same build system and depending on some of
the same libraries, we also have `libandroidfw` and `adbd` in this repo. `libandroidfw` is built
and installed by default, while `adbd` can be built with `make adbd` and installed with `make install_adbd`.

### history

This was created by frankenstaining art (and dependencies) onto the dalvik branch. Most shoehorned stuff was taken from the `android-6.0.1_r46` tag.  
Some things (notably art and libcore) have been updated since, `git log <directory>` is your friend.  
There are also many patches that were not part of the dalvik branch, making this nicer to use
(like having a default bootclasspath instead of having to specify it)

Only self-hosted builds are supported (though the build system wasn't completely cleaned of target rules).

UPDATE: `bionic_translation` is now a separate repo; `meson install` it before building this (TODO: make `libdl_bio` an optional dependency)  

NOTE: most of the .git folders that should be present in the subdirs were removed in order to make this repository several gigabytes leaner that it would be with them included.  
also, it really fucks with git if you leave them in  
this shouldn't be *that* much of an inconvenience, and is far from the most notable issue with being a downstream of a random subset of AOSP repositories.

### alternatives

In theory, it would be nice to not be dependent on things that are a massive PITA to fix up for use outside AOSP.  
There was some work done on running dex files on GraalVM with Truffle, but sadly this was never completed
or even released publicly. The information that it even exists comes from a publicly avaliable dissertation
paper: https://studentnet.cs.manchester.ac.uk/resources/library/thesis_abstracts/MSc16/FullText/Salim-Salim-diss.pdf,
which says to "contact these professors \[names, not contact information\] for source code access"

If art continues to only see a release every year even with "project mainline", keeping up with it might actually
be less work than implementing and keeping up with changes to the dex format, not speaking about the likely
performance penalty considering art's maturity.
