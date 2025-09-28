package org.gaseumlabs.uhcplugin

import java.io.File

object DataPack {
	fun load(worldFile: File) {
		val packDirectory = File(worldFile, "datapacks/uhcplugin")
		packDirectory.mkdirs()

		val packMcmeta = File(packDirectory, "pack.mcmeta")
		packMcmeta.writeText("""
			{
			    "pack": {
			        "description": "UHC Plugin Datapack",
			        "pack_format": 81
			    }
			}
		""".trimIndent())

		createJsonFile(packDirectory, "worldgen/structure/nether.json", """
			{
			  "type": "minecraft:fortress",
			  "biomes": "#minecraft:is_overworld",
			  "step": "surface_structures",
			  "spawn_overrides": {}
			}
		""".trimIndent())

		createJsonFile(packDirectory, "worldgen/structure_set/nether.json", """
			{
			  "structures":[
			    {
			      "structure": "uhcplugin:nether",
			      "weight": 1
			    }
			  ],
			  "placement": {
			    "salt": 12334,
			    "frequency": 1.0,
			    "locate_offset": [0, 0, 0],
			    "type": "minecraft:concentric_rings",
			    "distance": 0,
			    "count": 2,
			    "preferred_biomes": "#minecraft:is_overworld",
			    "spread": 2
			  }
			}
		""".trimIndent())

		createJsonFile(packDirectory, "worldgen/configured_carver/caves.json", """
			{
				"type": "cave",
				"yScale": 8.0,
				"horizontal_radius_multiplier": 8.0,
				"vertical_radius_multiplier": 8.0,
				"floor_level": 0.0,
				"config": {
					"probability": 1.0,
					"y": {
						"type": "uniform",
						"min_inclusive": {
							"absolute": -30							
						},
						"max_inclusive": {
							"absolute": 30
						}
					},
					"lava_level": {
						"absolute": -49
					},
					"replaceable": "#minecraft:overworld_carver_replaceables"
				}
			}
		""".trimIndent())
	}

	fun createJsonFile(packDirectory: File, filename: String, contents: String) {
		val jsonFile = File(packDirectory, filename)
		jsonFile.parentFile.mkdirs()
		jsonFile.writeText(contents)

	}
}