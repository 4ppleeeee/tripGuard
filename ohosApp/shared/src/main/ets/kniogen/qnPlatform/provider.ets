/***
*    !!!  GEN CODE DO NOT EDIT  !!!
***/
import { getService } from "knoi"

export interface HarmonyPageStackManager {
            
   onPageCreated(contxt: object): void;

   onPageDestroyed(contxt: object): void;

}

export function getHarmonyPageStackManager(): HarmonyPageStackManager {
   return getService<HarmonyPageStackManager>("HarmonyPageStackManager")
}

