#ifndef CONFIG_H
#define CONFIG_H

#include <unistd.h>

struct override {
	char *from;
	char *to;
};

struct override_map {
	struct override *overrides;
	size_t len;
	size_t size;
};

void read_cfg_dir(struct override_map *map, char *cfg_dir_path);
#endif
