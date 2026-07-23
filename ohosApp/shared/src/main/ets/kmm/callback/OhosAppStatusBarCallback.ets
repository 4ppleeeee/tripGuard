import { window } from '@kit.ArkUI';
import { common } from '@kit.AbilityKit';
import { AppThemeManager } from '../../utils/AppThemeManager';

/**
 * 鸿蒙端 StatusBar 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppStatusBar。
 *
 * 使用 @kit.ArkUI 的 window.setWindowSystemBarProperties 控制系统状态栏的
 * 文字颜色与背景色。主窗口通过 window.getLastWindow(getContext()) 动态获取，
 * 避免页面栈变化后窗口句柄失效。
 *
 * 颜色语义说明：
 *  - setWhiteBar：statusBarContentColor = '#FFFFFF'（白色文字，适合深色背景）
 *  - setBlackBar：statusBarContentColor = '#000000'（黑色文字，适合浅色背景）
 *  - resetStatusBar：清除业务自定义背景色，文字色按当前主题自动选择（深色模式白色，浅色模式黑色）
 *  - setStatusBarVisibility：控制状态栏显示/隐藏
 *  - setCustomBar：业务自定义文字/背景色
 */
export class OhosAppStatusBarCallback {

  /** 设置为白色文字（用于深色背景页面，如沉浸式视频） */
  setWhiteBar(): void {
    this.applyProperties('#FFFFFF', undefined);
  }

  /** 设置为黑色文字（用于浅色背景页面） */
  setBlackBar(): void {
    this.applyProperties('#000000', undefined);
  }

  /**
   * 恢复系统默认状态栏（清除自定义背景，文字色按当前主题自动选择）。
   * 深色模式下使用白色文字，浅色模式下使用黑色文字，避免状态栏图标与背景对比不足。
   */
  resetStatusBar(): void {
    const isDark = AppThemeManager.getInstance().isDarkMode();
    this.applyProperties(isDark ? '#FFFFFF' : '#000000', undefined);
  }

  /**
   * 自定义状态栏
   * @param textColor 状态栏文字/图标颜色（#RRGGBB）
   * @param backgroundColor 状态栏背景色（#RRGGBB）
   */
  setCustomBar(textColor: string, backgroundColor: string): void {
    this.applyProperties(textColor, backgroundColor);
  }

  /**
   * 设置状态栏显示/隐藏
   * @param visible true 显示状态栏，false 隐藏状态栏
   */
  setStatusBarVisibility(visible: boolean): void {
    try {
      const context = getContext(this) as common.UIAbilityContext;
      window.getLastWindow(context).then((win: window.Window) => {
        const names: Array<'status' | 'navigation'> = visible
          ? ['status', 'navigation']
          : ['navigation'];
        win.setWindowSystemBarEnable(names).catch((err: Error) => {
          console.error(`[OhosAppStatusBar] setWindowSystemBarEnable failed: ${JSON.stringify(err)}`);
        });
      }).catch((err: Error) => {
        console.error(`[OhosAppStatusBar] getLastWindow failed: ${JSON.stringify(err)}`);
      });
    } catch (e) {
      console.error(`[OhosAppStatusBar] setStatusBarVisibility error: ${e}`);
    }
  }

  /**
   * 统一应用 SystemBar 属性。
   * statusBarContentColor 传入 '#000000' 对应浅色图标，'#FFFFFF' 对应深色图标——
   * 鸿蒙 SystemBarProperties 文档约定：内容颜色仅区分 "深/浅"，不支持任意颜色，
   * 但字段接受 #RRGGBB 字符串并按亮度映射。
   */
  private applyProperties(statusBarContentColor: string, statusBarColor?: string): void {
    try {
      const context = getContext(this) as common.UIAbilityContext;
      window.getLastWindow(context).then((win: window.Window) => {
        const props: window.SystemBarProperties = {
          statusBarContentColor: statusBarContentColor,
        };
        if (statusBarColor) {
          props.statusBarColor = statusBarColor;
        }
        win.setWindowSystemBarProperties(props).catch((err: Error) => {
          console.error(`[OhosAppStatusBar] setWindowSystemBarProperties failed: ${JSON.stringify(err)}`);
        });
      }).catch((err: Error) => {
        console.error(`[OhosAppStatusBar] getLastWindow failed: ${JSON.stringify(err)}`);
      });
    } catch (e) {
      console.error(`[OhosAppStatusBar] applyProperties error: ${e}`);
    }
  }
}
