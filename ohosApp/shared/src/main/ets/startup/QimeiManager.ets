import { IAsyncQimeiListener, IQimeiSDK, Qimei, QimeiSDK, UsKeySdk } from 'qimei';

/**
 * QIMEI管理类，用于初始化和获取q36
 */
export class QimeiManager {
  private static instance: QimeiManager;
  private static readonly TAG: string = 'QimeiInit';
  private static qimei36: string = '';
  private listeners: Array<(q36: string) => void> = [];

  private constructor() {
  }

  public static getInstance(): QimeiManager {
    if (!QimeiManager.instance) {
      QimeiManager.instance = new QimeiManager();
    }
    return QimeiManager.instance;
  }

  /**
   * 初始化q36 sdk，并将q36存储到内存缓存中
   * @param context 上下文
   */
  public initQimei(context: Context, appKey: string, channelId: string, isDebug: boolean, callback: Function) {
    let qimeiSdk: IQimeiSDK = QimeiSDK.getInstance(appKey);
    qimeiSdk.setChannelID(channelId)// 传入渠道号
      .setLogAble(isDebug)// 调试模式下打开日志
      .init(context); // 建议传入 ApplicationContext

    const listener: IAsyncQimeiListener = {
      onQimeiDispatch: (qimei: Qimei): void => {
        QimeiManager.qimei36 = qimei.qimei36;
        // KMP 侧 callback 签名为 (qimei36, qimei16)，需传入两个参数
        // 鸿蒙端目前只稳定提供 qimei36，qimei16 暂时也传 qimei36
        callback(qimei.qimei36, qimei.qimei36)
      }
    }
    qimeiSdk.getQimei(listener);
  }

  public static getQimei36(): string {
    return QimeiManager.qimei36;
  }

  /**
   * 复用 qimei SDK 暴露的 UsKeySdk，异常透传给 KMP Registry 统一记录并回空串。
   */
  public static getUskey(
    appKey: string,
    appVersion: string,
    businessId: string,
    qimei36: string,
    busInfo: string,
  ): string {
    return UsKeySdk.getUskey(appKey, appVersion, businessId, qimei36, busInfo);
  }

  public static isInitialized(): boolean {
    return QimeiManager.qimei36 ? true : false;
  }
}
