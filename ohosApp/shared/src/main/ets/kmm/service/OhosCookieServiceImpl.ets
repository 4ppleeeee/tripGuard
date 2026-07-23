import { registerServiceProvider } from 'knoi';
import { webview } from '@kit.ArkWeb';
import { OhosCookieService } from '../../kniogen/consumer';

const TAG = 'OhosCookieService';

export function registerOhosCookieService() {
  registerServiceProvider('OhosCookieService', true, new OhosCookieServiceImpl());
}

class OhosCookieServiceImpl implements OhosCookieService {
  getCookie(url: string): string {
    try {
      return webview.WebCookieManager.fetchCookieSync(url) ?? '';
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      console.error(`[${TAG}] getCookie failed with error message=${message}`);
      return '';
    }
  }

  setCookie(url: string, value: string): void {
    try {
      webview.WebCookieManager.configCookieSync(url, value);
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      console.error(`[${TAG}] setCookie failed with error message=${message}`);
    }
  }

  removeAllCookies(): void {
    try {
      webview.WebCookieManager.clearAllCookiesSync();
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      console.error(`[${TAG}] removeAllCookies failed with error message=${message}`);
    }
  }
}
