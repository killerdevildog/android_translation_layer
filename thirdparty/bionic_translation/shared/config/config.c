#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>

#include "config.h"

#define OVERRIDE_MAP_DEFAULT_SIZE 8

static void override_map_append(struct override_map *map, char *from, char *to)
{
	if(!map->overrides) {
		size_t map_size = OVERRIDE_MAP_DEFAULT_SIZE;
		map->overrides = malloc(map_size * sizeof(struct override));
		map->len = 0;
		map->size = map_size;
	}

	if(map->len == map->size) {
		map->size *= 2;
		map->overrides = realloc(map->overrides, map->size * sizeof(struct override));
	}

	map->overrides[map->len].from = from;
	map->overrides[map->len].to = to;
	map->len += 1;
}

static void process_cfg_line(struct override_map *map, char *line, size_t len, char *path, int linenum)
{
	char *from;
	char *to;
	int ret;

	// skip empty lines and comments
	if(line[0] == '#' || line[0] == '\n')
		return;

	ret = sscanf(line, "%ms %ms", &from, &to);
	if(ret != 2) {
		printf("error reading cfg: %s:%d\n", path, linenum);
		exit(1);
	}

	override_map_append(map, from, to);
}

static void read_cfg_file(struct override_map *map, char *path)
{
	char *line = NULL;
	size_t line_len;
	int linenum = 1;

	FILE *cfg = fopen(path, "r");
	if(!cfg) {
		printf("failed to open %s (%m)\n", path);
		exit(1);
	}

	while(getline(&line, &line_len, cfg) > 0) {
		process_cfg_line(map, line, line_len, path, linenum++);
		free(line);
		line = NULL;
	}

	fclose(cfg);
}

void read_cfg_dir(struct override_map *map, char *cfg_dir_path)
{
	struct dirent *entry;

	DIR *cfg_dir = opendir(cfg_dir_path);
	if(!cfg_dir)
		return;

	while(entry = readdir(cfg_dir)) {
		if(!strcmp(entry->d_name, ".") || !strcmp(entry->d_name, ".."))
			continue;

		char *full_path = malloc(strlen(cfg_dir_path) + 1 + strlen(entry->d_name) + 1); // +1 for /, +1 for NUL
		sprintf(full_path, "%s/%s", cfg_dir_path, entry->d_name);
		read_cfg_file(map, full_path);
	}
}
