# Dependency Matrix: Minecraft 1.21.1

| Dependency | Mod id | Scope | Build coordinate | Notes |
| --- | --- | --- | --- | --- |
| Minecraft | `minecraft` | Required | `1.21.1` | Target game version. |
| NeoForge | `neoforge` | Required | `net.neoforged:neoforge:21.1.233` | Loader and API. |
| Immersive Engineering | `immersiveengineering` | Required | `blusunrize.immersiveengineering:ImmersiveEngineering:1.21.1-12.4.2-194` | Required by merged farming and cooking multiblock APIs, recipes, screens, and runtime behavior. |
| DualCodecs | n/a | Compile only | `malte0811:DualCodecs:0.1.2` | Needed by Immersive Engineering recipe serializers. |
| JEI | `jei` | Optional | `mezz.jei:jei-1.21.1-neoforge-api:19.27.0.340`, `mezz.jei:jei-1.21.1-common-api:19.27.0.340` | Recipe categories and transfer handlers. |
| Farmer's Delight | `farmersdelight` | Optional | `curse.maven:farmers-delight-398521:7596366` | NeoForge 1.21.1 file, used by cookpot compatibility provider. |
| Farm & Charm | `farm_and_charm` | Optional | `curse.maven:lets-do-farm-charm-1038103:7959521` | NeoForge 1.21.1 file, used by cookpot/roaster/stove providers. |
| Architectury API | `architectury` | Optional transitive/runtime support | `curse.maven:architectury-api-419699:5786327` | NeoForge 1.21.1 API needed by some optional integrations. |
| Vinery | `vinery` | Optional data compatibility | Not compile-linked | Optional recipe data only. |

Do not add the original Immersive Cooking jar as a runtime dependency. Its code and resources are merged into this jar.
