import { OhosAppStatus } from '../../kniogen/callback';
import {
  AppThemeManager,
  IDarkModeListener,
  IFontScaleListener,
  IFontFamilyListener
} from '../../utils/AppThemeManager';
import { OhosNetworkServiceImpl } from '../service/OhosNetworkServiceImpl';

export class OhosAppStatusCallback implements OhosAppStatus {
  private readonly networkService: OhosNetworkServiceImpl = new OhosNetworkServiceImpl();

  subscribeTheme(onUiModeChanged: Function): void {
    // 注册深色模式变化监听（仅注册，不同步回调，避免 knoi 桥接层崩溃）
    const listener: IDarkModeListener = {
      onDarkModeChanged: (isDark: boolean): void => {
        onUiModeChanged(isDark);
      }
    };
    AppThemeManager.getInstance().addListener(listener);
  }

  subscribeTextScaleRatio(onTextScaleRatioChanged: Function): void {
    // 注册字体缩放变化监听（仅注册，不同步回调）
    // 由 EntryAbility.onConfigurationUpdate 中的 fontSizeScale 变化驱动
    const listener: IFontScaleListener = {
      onFontScaleChanged: (scale: number): void => {
        onTextScaleRatioChanged(scale);
      }
    };
    AppThemeManager.getInstance().addFontScaleListener(listener);
  }

  subscribeFontFamily(onFontFamilyChanged: Function): void {
    // 注册字体家族变化监听（仅注册，不同步回调）
    const listener: IFontFamilyListener = {
      onFontFamilyChanged: (fontFamily: string): void => {
        onFontFamilyChanged(fontFamily);
      }
    };
    AppThemeManager.getInstance().addFontFamilyListener(listener);
  }

  subscribeNetState(onChange: Function): void {
    // 委托给 OhosNetworkServiceImpl，复用已有的网络状态订阅逻辑
    // onChange 由 knoi 桥接层传入，参数为 Array，取第一个元素作为 state 字符串
    this.networkService.subscribeNetState((state: string) => {
      onChange([state]);
    });
  }
}
