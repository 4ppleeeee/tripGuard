import { geoLocationManager } from '@kit.LocationKit';

/**
 * 鸿蒙端 Location 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppLocation。
 *
 * 使用 @kit.LocationKit 的 geoLocationManager.getCurrentLocation 发起单次定位。
 * 鸿蒙 Location 对象仅包含经纬度、海拔、精度等字段，不直接提供国家统计局 adCode，
 * 因此 adCode 参数当前恒传空串。如后续需要 adCode，可在此基础上调用
 * geoLocationManager.getAddressesFromLocation 拿到行政区划文字，
 * 再接入腾讯/高德地图 SDK 做转换。
 *
 * 【使用前提】
 *  - ArkTS 侧必须在调用前已经通过 OhosAppPermissionCallback 拿到定位权限，
 *    否则 getCurrentLocation 会直接 reject 权限错误；
 *  - KMP 侧 requestLocation 默认会先经过 appPermission.hasLocationPermission /
 *    requestLocationPermission 拦截，权限流与定位流已经解耦。
 */
export class OhosAppLocationCallback {

  /**
   * 发起单次定位请求。
   * @param scene 场景标识，当前仅用于日志
   * @param legacyCode 旧 int 场景值。0 表示无旧值。
   * @param onResult 定位结果回调：
   *     - success：是否定位成功
   *     - latitude：纬度（失败时为 0）
   *     - longitude：经度（失败时为 0）
   *     - adCode：行政区划编码（鸿蒙端恒为空串，预留扩展）
   */
  requestLocation(
    scene: string,
    legacyCode: number,
    onResult: (success: boolean, latitude: number, longitude: number, adCode: string) => void,
  ): void {
    try {
      const request: geoLocationManager.SingleLocationRequest = {
        // 优先级设为精确定位，与腾讯新闻/业务一致
        locatingPriority: geoLocationManager.LocatingPriority.PRIORITY_LOCATING_SPEED,
        locatingTimeoutMs: 10000,
      };
      geoLocationManager.getCurrentLocation(request)
        .then((location: geoLocationManager.Location) => {
          onResult(true, location.latitude, location.longitude, '');
        })
        .catch((err: Error) => {
          console.error(`[OhosAppLocation] getCurrentLocation failed, scene=${scene}, legacyCode=${legacyCode}: ${JSON.stringify(err)}`);
          onResult(false, 0, 0, '');
        });
    } catch (e) {
      console.error(`[OhosAppLocation] requestLocation error: ${e}`);
      onResult(false, 0, 0, '');
    }
  }
}
