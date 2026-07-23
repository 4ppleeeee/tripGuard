import { ConfigurationConstant } from '@kit.AbilityKit';

/**
 * 深色模式监听器接口
 */
export interface IDarkModeListener {
  onDarkModeChanged: (isDark: boolean) => void;
}

/**
 * 字体缩放监听器接口
 */
export interface IFontScaleListener {
  onFontScaleChanged: (scale: number) => void;
}

/**
 * 字体家族监听器接口
 */
export interface IFontFamilyListener {
  onFontFamilyChanged: (fontFamily: string) => void;
}

/**
 * 应用主题管理器（单例）
 * 仿照腾讯新闻 DarkStateController / TextSizeHelper，管理深色模式、字体缩放、字体家族监听器列表。
 * 由 EntryAbility.onConfigurationUpdate 驱动通知。
 */
export class AppThemeManager {
  private static instance: AppThemeManager;
  private darkModeListeners: IDarkModeListener[] = [];
  private fontScaleListeners: IFontScaleListener[] = [];
  private fontFamilyListeners: IFontFamilyListener[] = [];
  private currentColorMode: ConfigurationConstant.ColorMode = ConfigurationConstant.ColorMode.COLOR_MODE_NOT_SET;
  private currentFontScale: number = 1.0;

  private constructor() {
  }

  public static getInstance(): AppThemeManager {
    if (!AppThemeManager.instance) {
      AppThemeManager.instance = new AppThemeManager();
    }
    return AppThemeManager.instance;
  }

  // region 深色模式

  /**
   * 添加深色模式监听器（仅注册，不立即回调）
   */
  public addListener(listener: IDarkModeListener): void {
    this.darkModeListeners.push(listener);
  }

  /**
   * 获取当前是否为深色模式
   */
  public isDarkMode(): boolean {
    return this.currentColorMode === ConfigurationConstant.ColorMode.COLOR_MODE_DARK;
  }

  /**
   * 当系统颜色模式变化时调用（由 EntryAbility.onConfigurationUpdate 触发）
   */
  public onColorModeChanged(colorMode: ConfigurationConstant.ColorMode): void {
    if (this.currentColorMode === colorMode) {
      return;
    }
    this.currentColorMode = colorMode;
    const isDark = colorMode === ConfigurationConstant.ColorMode.COLOR_MODE_DARK;
    for (const listener of this.darkModeListeners) {
      listener.onDarkModeChanged(isDark);
    }
  }

  // endregion

  // region 字体缩放

  /**
   * 添加字体缩放监听器（仅注册，不立即回调）
   */
  public addFontScaleListener(listener: IFontScaleListener): void {
    this.fontScaleListeners.push(listener);
  }

  /**
   * 获取当前字体缩放比例
   */
  public getFontScale(): number {
    return this.currentFontScale;
  }

  /**
   * 当系统字体缩放变化时调用（由 EntryAbility.onConfigurationUpdate 触发）
   */
  public onFontScaleChanged(fontSizeScale: number): void {
    if (this.currentFontScale === fontSizeScale) {
      return;
    }
    this.currentFontScale = fontSizeScale;
    for (const listener of this.fontScaleListeners) {
      listener.onFontScaleChanged(fontSizeScale);
    }
  }

  // endregion

  // region 字体家族

  /**
   * 添加字体家族监听器（仅注册，不立即回调）
   */
  public addFontFamilyListener(listener: IFontFamilyListener): void {
    this.fontFamilyListeners.push(listener);
  }

  /**
   * 当字体家族变化时调用
   */
  public onFontFamilyChanged(fontFamily: string): void {
    for (const listener of this.fontFamilyListeners) {
      listener.onFontFamilyChanged(fontFamily);
    }
  }

  // endregion
}
