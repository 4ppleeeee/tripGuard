import { AbilityConstant, Want, common } from '@kit.AbilityKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { IQQOpenApi, OpenApiConfig, QQOpenApiFactory } from '@tencent/qq-open-sdk';
import {
  BaseReq,
  BaseResp,
  ErrCode,
  SendAuthResp,
  WXApi,
  WXApiEventHandler,
  WXAPIFactory
} from '@tencent/wechat_open_sdk';

const TAG = 'LoginSDKManager';
const WX_AUTH_SCENE_LOGIN = 'login';
const WX_AUTH_SCENE_FRIEND_CHAIN = 'friend_chain';

/**
 * 登录 SDK 运行时管理：
 * 1. startup 阶段完成初始化；
 * 2. EntryAbility.onNewWant 统一分发 QQ/微信回调；
 * 3. 暴露 SDK 实例供三方回调分发使用。
 */
export class LoginSDKManager {
  private static instance: LoginSDKManager;
  private qqOpenApi?: IQQOpenApi;
  private wxApi?: WXApi;
  private uiContext?: UIContext;
  private abilityContext?: common.UIAbilityContext;

  // 微信登录序列号（发起 wxLogin 时记录，onResp 时回传给 KMP 层用于匹配本次请求）
  private wxLoginSerialNo: number = 0;
  private wxAuthScene: string = WX_AUTH_SCENE_LOGIN;

  private constructor() {
  }

  static getInstance(): LoginSDKManager {
    if (!LoginSDKManager.instance) {
      LoginSDKManager.instance = new LoginSDKManager();
    }
    return LoginSDKManager.instance;
  }

  initQQ(appId: string, callback: Function): void {
    const config = new OpenApiConfig();
    config.forceEnableWeb = true;
    // QQ OpenSDK 1.0.4 统一通过 handleResult 处理登录/分享路由回调。
    this.qqOpenApi = QQOpenApiFactory.createApi(parseInt(appId), config);
    hilog.info(
      0x0000,
      TAG,
      `initQQ: appId=${appId}, forceEnableWeb=${config.forceEnableWeb}`
    );
    callback();
  }

  initWX(appId: string, callback: Function): void {
    this.wxApi = WXAPIFactory.createWXAPI(appId);
    callback();
  }

  /** 设置 UIContext，供 QQ 登录使用 */
  setUIContext(uiContext: UIContext): void {
    this.uiContext = uiContext;
  }

  /** 获取 UIContext */
  getUIContext(): UIContext | undefined {
    return this.uiContext;
  }

  /** 设置 AbilityContext，供微信登录使用 */
  setAbilityContext(context: common.UIAbilityContext): void {
    this.abilityContext = context;
  }

  /** 获取 AbilityContext */
  getAbilityContext(): common.UIAbilityContext | undefined {
    return this.abilityContext;
  }

  /** 获取 QQ Open API 实例 */
  getQQApi(): IQQOpenApi | undefined {
    return this.qqOpenApi;
  }

  /** 获取微信 API 实例 */
  getWXApi(): WXApi | undefined {
    return this.wxApi;
  }

  /** 记录本次微信登录的序列号，onResp 时回传给 KMP 层用于匹配 pending callback */
  setWXLoginSerialNo(serialNo: number): void {
    this.wxLoginSerialNo = serialNo;
    this.wxAuthScene = WX_AUTH_SCENE_LOGIN;
  }

  setWXFriendChainAuthSerialNo(serialNo: number): void {
    this.wxLoginSerialNo = serialNo;
    this.wxAuthScene = WX_AUTH_SCENE_FRIEND_CHAIN;
  }

  clearWXFriendChainAuthSerialNo(serialNo: number): void {
    if (this.wxLoginSerialNo === serialNo && this.wxAuthScene === WX_AUTH_SCENE_FRIEND_CHAIN) {
      this.wxLoginSerialNo = 0;
      this.wxAuthScene = WX_AUTH_SCENE_LOGIN;
    }
  }

  onNewWant(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    hilog.info(
      0x0000,
      TAG,
      `onNewWant: hasQQ=${!!this.qqOpenApi}, hasWX=${!!this.wxApi}`
    );
    if (this.qqOpenApi) {
      try {
        this.qqOpenApi.handleResult(want);
        hilog.info(0x0000, TAG, 'qq handleResult done');
      } catch (error) {
        hilog.error(0x0000, TAG, `qq handleResult failed: ${JSON.stringify(error)}`);
      }
    }

    if (this.wxApi) {
      try {
        // 使用带回调的 handler 处理微信回调
        this.wxApi.handleWant(want, new WXLoginCallbackHandler(this));
      } catch (error) {
        hilog.error(0x0000, TAG, `wx handleWant failed: ${JSON.stringify(error)}`);
      }
    }
  }

  /** 处理微信授权回调结果。当前 demo 壳不接入 KMM 登录业务，仅消费并记录回调。 */
  handleWXAuthResp(resp: BaseResp): void {
    const serialNo = this.wxLoginSerialNo;
    if (serialNo === 0) {
      hilog.warn(0x0000, TAG, 'wx auth response ignored: no pending serialNo');
      return;
    }

    const authResp = resp as SendAuthResp;
    const expectedState = this.wxAuthState(serialNo);
    const actualState = authResp.state ?? '';
    if (actualState !== expectedState) {
      hilog.warn(0x0000, TAG, `wx auth response ignored: expectedState=${expectedState}, actualState=${actualState}`);
      return;
    }

    if (resp.errCode == ErrCode.ERR_OK) {
      const code = authResp.code ?? '';
      const scene = this.wxAuthScene;
      this.wxLoginSerialNo = 0;
      this.wxAuthScene = WX_AUTH_SCENE_LOGIN;
      hilog.info(0x0000, TAG, `wx auth response ignored: scene=${scene}, serialNo=${serialNo}, codeLen=${code.length}`);
    } else if (resp.errCode == ErrCode.ERR_USER_CANCEL) {
      const scene = this.wxAuthScene;
      this.wxLoginSerialNo = 0;
      this.wxAuthScene = WX_AUTH_SCENE_LOGIN;
      hilog.info(0x0000, TAG, `wx auth canceled and ignored: scene=${scene}, serialNo=${serialNo}`);
    } else {
      const scene = this.wxAuthScene;
      this.wxLoginSerialNo = 0;
      this.wxAuthScene = WX_AUTH_SCENE_LOGIN;
      hilog.warn(
        0x0000,
        TAG,
        `wx auth failed and ignored: scene=${scene}, serialNo=${serialNo}, errCode=${resp.errCode}, ` +
          `errStr=${resp.errStr ?? 'unknown error'}`
      );
    }
  }

  private wxAuthState(serialNo: number): string {
    return `kmm_login_${serialNo}`;
  }
}

/**
 * 微信登录回调处理器
 * 将微信 SDK 的 onResp 回调转发给 LoginSDKManager
 */
class WXLoginCallbackHandler implements WXApiEventHandler {
  private manager: LoginSDKManager;

  constructor(manager: LoginSDKManager) {
    this.manager = manager;
  }

  onReq(req: BaseReq): void {
    hilog.info(0x0000, TAG, `wx onReq: ${JSON.stringify(req)}`);
  }

  onResp(resp: BaseResp): void {
    hilog.info(0x0000, TAG, `wx onResp: ${JSON.stringify(resp)}`);
    this.manager.handleWXAuthResp(resp);
  }
}
