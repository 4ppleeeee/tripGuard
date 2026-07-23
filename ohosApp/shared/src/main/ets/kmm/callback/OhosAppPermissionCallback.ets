import { abilityAccessCtrl, bundleManager, common, Permissions } from '@kit.AbilityKit';

/**
 * 鸿蒙端 AppPermission 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppPermission。
 *
 * 使用 @kit.AbilityKit 的 abilityAccessCtrl.createAtManager() 实现：
 *  - 同步查询定位权限授权态（APPROXIMATELY_LOCATION / LOCATION 任一授予即算有权限）；
 *  - 异步拉起系统权限弹窗 requestPermissionsFromUser；
 *  - 当用户选择 "不再提示" 且 isForceRequestPermission=true 时，跳转系统设置页引导。
 *
 * 【常量说明】
 *  - GRANT_STATUS_GRANTED = 0（abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED）
 *  - GRANT_STATUS_DENIED = -1
 *  - authResults 值：0=已授权，-1=拒绝/本次未弹窗，2=用户勾选了"不再提示"
 */
export class OhosAppPermissionCallback {

  private readonly context: common.UIAbilityContext;

  constructor(context: common.UIAbilityContext) {
    this.context = context;
  }

  private static readonly LOCATION_PERMISSIONS: Permissions[] = [
    'ohos.permission.APPROXIMATELY_LOCATION',
    'ohos.permission.LOCATION',
  ];

  private static readonly GRANT_STATUS_GRANTED = 0;
  private static readonly GRANT_STATUS_DENIED = -1;

  /** 同步查询当前应用是否已授予定位权限（精确或模糊任一） */
  hasLocationPermission(): boolean {
    try {
      const atManager = abilityAccessCtrl.createAtManager();
      const tokenId = this.getSelfTokenId();
      if (tokenId === 0) {
        return false;
      }
      // 精确或模糊任一授予即算有权限
      for (let i = 0; i < OhosAppPermissionCallback.LOCATION_PERMISSIONS.length; i++) {
        const perm = OhosAppPermissionCallback.LOCATION_PERMISSIONS[i];
        const status = atManager.checkAccessTokenSync(tokenId, perm);
        if (status === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
          return true;
        }
      }
      return false;
    } catch (e) {
      console.error(`[OhosAppPermission] hasLocationPermission error: ${e}`);
      return false;
    }
  }

  /**
   * 异步申请定位权限。
   * @param isForceRequestPermission 被用户勾选"不再提示"后是否跳设置页引导
   * @param onResult 结果回调：grantStatus（0=授权，-1=拒绝），reason（拒绝原因）
   */
  requestLocationPermission(
    isForceRequestPermission: boolean,
    onResult: (grantStatus: number, reason: string) => void,
  ): void {
    try {
      const atManager = abilityAccessCtrl.createAtManager();
      atManager.requestPermissionsFromUser(this.context, OhosAppPermissionCallback.LOCATION_PERMISSIONS)
        .then((data) => {
          const authResults = data.authResults ?? [];
          const granted = authResults.some((r: number) => r === OhosAppPermissionCallback.GRANT_STATUS_GRANTED);
          if (granted) {
            onResult(OhosAppPermissionCallback.GRANT_STATUS_GRANTED, '');
            return;
          }
          // 用户勾选了"不再提示"（authResults 为 2）且业务方希望强制引导
          const neverAskAgain = authResults.some((r: number) => r === 2);
          if (neverAskAgain && isForceRequestPermission) {
            this.openSettings(this.context);
            onResult(OhosAppPermissionCallback.GRANT_STATUS_DENIED, 'user selected never ask again, jump to settings');
          } else {
            onResult(OhosAppPermissionCallback.GRANT_STATUS_DENIED, 'permission denied by user');
          }
        })
        .catch((err: Error) => {
          console.error(`[OhosAppPermission] requestPermissionsFromUser failed: ${JSON.stringify(err)}`);
          onResult(OhosAppPermissionCallback.GRANT_STATUS_DENIED, err.message ?? 'request error');
        });
    } catch (e) {
      console.error(`[OhosAppPermission] requestLocationPermission error: ${e}`);
      onResult(OhosAppPermissionCallback.GRANT_STATUS_DENIED, `${e}`);
    }
  }

  /** 获取当前应用的 accessTokenId（用于 checkAccessTokenSync） */
  private getSelfTokenId(): number {
    try {
      const flag = bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_APPLICATION;
      const bundleInfo = bundleManager.getBundleInfoForSelfSync(flag);
      return bundleInfo.appInfo.accessTokenId;
    } catch (e) {
      console.error(`[OhosAppPermission] getSelfTokenId error: ${e}`);
      return 0;
    }
  }

  /** 跳转到应用权限设置页 */
  private openSettings(context: common.UIAbilityContext): void {
    try {
      context.startAbility({
        bundleName: 'com.huawei.hmos.settings',
        abilityName: 'com.huawei.hmos.settings.MainAbility',
        uri: 'application_info_entry',
        parameters: {
          pushParams: context.abilityInfo.bundleName,
        },
      }).catch((err: Error) => {
        console.error(`[OhosAppPermission] openSettings failed: ${JSON.stringify(err)}`);
      });
    } catch (e) {
      console.error(`[OhosAppPermission] openSettings error: ${e}`);
    }
  }
}
