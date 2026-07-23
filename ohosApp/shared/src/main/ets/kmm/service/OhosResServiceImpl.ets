import { registerServiceProvider } from 'knoi';
import { OhosResService } from '../../kniogen/consumer';
import { Context } from '@kit.AbilityKit';
import { util } from '@kit.ArkTS';

export function registerOhosResService(context: Context) {
  registerServiceProvider("OhosResService", true, new OhosResServiceImpl(context))
}

class OhosResServiceImpl implements OhosResService {
  private context: Context;
  private cachedMapping: Record<string, string> | null = null;

  constructor(context: Context) {
    this.context = context;
  }

  getIconFontMapping(): object {
    if (this.cachedMapping !== null) {
      return this.cachedMapping;
    }
    try {
      let rawFile: Uint8Array = this.context.resourceManager.getRawFileContentSync('iconfont/iconfont_new_style.json');
      let textDecoder: util.TextDecoder = util.TextDecoder.create('utf-8');
      let jsonStr: string = textDecoder.decodeToString(rawFile);
      let mapping: Record<string, string> = JSON.parse(jsonStr) as Record<string, string>;
      this.cachedMapping = mapping;
      return mapping;
    } catch (e) {
      console.error(`[OhosResService] getIconFontMapping failed: ${e}`);
      let emptyMap: Record<string, string> = {};
      return emptyMap;
    }
  }
}
