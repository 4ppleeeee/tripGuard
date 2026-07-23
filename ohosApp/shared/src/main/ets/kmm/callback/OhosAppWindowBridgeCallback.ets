import { window, display } from '@kit.ArkUI';
import { common } from '@kit.AbilityKit';
import { sensor } from '@kit.SensorServiceKit';
import { Callback } from '@ohos.base';

/**
 * 鸿蒙端窗口管理回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppWindowBridge。
 *
 * 使用 @kit.ArkUI 的 window API 控制屏幕方向和全屏模式。
 * 主窗口通过 window.getLastWindow(getContext()) 动态获取，
 * 避免页面栈变化后窗口句柄失效。
 */
export class OhosAppWindowBridgeCallback {
  private orientationChangeCallback: Callback<sensor.AccelerometerResponse> | null = null;
  private latestDeviceOrientation: string | null = null;

  /**
   * 设置屏幕方向
   * @param orientation 方向标识："portrait" / "landscape" / "auto"
   */
  setScreenOrientation(orientation: string): void {
    try {
      const context = getContext(this) as common.UIAbilityContext;
      window.getLastWindow(context).then((win: window.Window) => {
        let orientationValue: window.Orientation;
        switch (orientation) {
          case 'portrait':
            orientationValue = window.Orientation.PORTRAIT;
            break;
          case 'landscape':
            orientationValue = window.Orientation.LANDSCAPE;
            break;
          case 'auto':
          default:
            orientationValue = window.Orientation.UNSPECIFIED;
            break;
        }
        win.setPreferredOrientation(orientationValue).catch((err: Error) => {
          console.error(`[OhosAppWindowBridge] setPreferredOrientation failed: ${JSON.stringify(err)}`);
        });
      }).catch((err: Error) => {
        console.error(`[OhosAppWindowBridge] getLastWindow failed: ${JSON.stringify(err)}`);
      });
    } catch (e) {
      console.error(`[OhosAppWindowBridge] setScreenOrientation error: ${e}`);
    }
  }

  /**
   * 进入全屏模式：隐藏状态栏和导航栏
   *
   * 注意：不需要调用 setWindowLayoutFullScreen(true)，因为应用启动时在 EntryAbility 中
   * 已设置为全屏布局模式，此处只需隐藏系统栏即可。
   */
  enterFullScreen(): void {
    try {
      const context = getContext(this) as common.UIAbilityContext;
      window.getLastWindow(context).then((win: window.Window) => {
        // 隐藏所有系统栏
        win.setWindowSystemBarEnable([]).catch((err: Error) => {
          console.error(`[OhosAppWindowBridge] enterFullScreen setWindowSystemBarEnable failed: ${JSON.stringify(err)}`);
        });
      }).catch((err: Error) => {
        console.error(`[OhosAppWindowBridge] getLastWindow failed: ${JSON.stringify(err)}`);
      });
    } catch (e) {
      console.error(`[OhosAppWindowBridge] enterFullScreen error: ${e}`);
    }
  }

  /**
   * 退出全屏模式：恢复状态栏和导航栏的显示
   *
   * 注意：不调用 setWindowLayoutFullScreen(false)，因为应用启动时在 EntryAbility 中
   * 已设置 setWindowLayoutFullScreen(true)（沉浸式布局），退出全屏时只需恢复系统栏显示，
   * 保持全屏布局模式不变，否则内容将不再延伸到状态栏下方。
   */
  exitFullScreen(): void {
    try {
      const context = getContext(this) as common.UIAbilityContext;
      window.getLastWindow(context).then((win: window.Window) => {
        // 恢复系统栏显示（保持 setWindowLayoutFullScreen(true) 不变）
        win.setWindowSystemBarEnable(['status', 'navigation']).catch((err: Error) => {
          console.error(`[OhosAppWindowBridge] exitFullScreen setWindowSystemBarEnable failed: ${JSON.stringify(err)}`);
        });
      }).catch((err: Error) => {
        console.error(`[OhosAppWindowBridge] getLastWindow failed: ${JSON.stringify(err)}`);
      });
    } catch (e) {
      console.error(`[OhosAppWindowBridge] exitFullScreen error: ${e}`);
    }
  }

  /**
   * 获取当前屏幕方向
   * 参考：https://developer.huawei.com/consumer/cn/doc/harmonyos-faqs/faqs-arkui-369
   * 使用 display.getDefaultDisplaySync().orientation 获取当前屏幕物理方向
   * @returns 方向标识："portrait" / "landscape"
   */
  getScreenOrientation(): string {
    try {
      const defaultDisplay = display.getDefaultDisplaySync();
      const orientation = defaultDisplay.orientation;
      // display.Orientation 枚举：
      // PORTRAIT = 0 (竖屏)
      // LANDSCAPE = 1 (横屏)
      // PORTRAIT_INVERTED = 2 (反向竖屏)
      // LANDSCAPE_INVERTED = 3 (反向横屏)
      if (orientation === display.Orientation.LANDSCAPE ||
          orientation === display.Orientation.LANDSCAPE_INVERTED) {
        return 'landscape';
      }
      return 'portrait';
    } catch (e) {
      console.error(`[OhosAppWindowBridge] getScreenOrientation error: ${e}`);
      return 'portrait';
    }
  }

  /**
   * 开始监听手机重力方向变化。
   * @param onOrientationChanged 方向标识回调："portrait" / "landscape"
   */
  startDeviceOrientationListening(onOrientationChanged: (orientation: string) => void): boolean {
    try {
      this.stopDeviceOrientationListening();
      const callback: Callback<sensor.AccelerometerResponse> = (data: sensor.AccelerometerResponse): void => {
        const orientation = this.toDeviceGravityOrientation(data.x, data.y);
        if (orientation === null || orientation === this.latestDeviceOrientation) {
          return;
        }
        this.latestDeviceOrientation = orientation;
        onOrientationChanged(orientation);
      };
      this.orientationChangeCallback = callback;
      sensor.on(sensor.SensorId.ACCELEROMETER, callback, { interval: 'ui' });
      return true;
    } catch (e) {
      console.error(`[OhosAppWindowBridge] startDeviceOrientationListening error: ${e}`);
      this.orientationChangeCallback = null;
      return false;
    }
  }

  /**
   * 停止监听系统显示方向变化。
   */
  stopDeviceOrientationListening(): void {
    if (this.orientationChangeCallback === null) {
      return;
    }
    try {
      sensor.off(sensor.SensorId.ACCELEROMETER, this.orientationChangeCallback);
    } catch (e) {
      console.error(`[OhosAppWindowBridge] stopDeviceOrientationListening error: ${e}`);
    } finally {
      this.orientationChangeCallback = null;
      this.latestDeviceOrientation = null;
    }
  }

  /**
   * 根据加速度计 x/y 轴重力分量判断手机物理横竖姿态。
   * 设备接近平放时 x/y 分量较小，忽略该帧，避免误触发横竖屏切换。
   */
  private toDeviceGravityOrientation(x: number, y: number): string | null {
    const absX = Math.abs(x);
    const absY = Math.abs(y);
    if (Math.max(absX, absY) < 3) {
      return null;
    }
    return absX > absY ? 'landscape' : 'portrait';
  }
}
