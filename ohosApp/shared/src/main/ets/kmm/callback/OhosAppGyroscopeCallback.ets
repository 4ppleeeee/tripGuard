import { sensor } from '@kit.SensorServiceKit';
import { Callback } from '@ohos.base';

/**
 * 鸿蒙端 Gyroscope 回调实现。
 * 对应 KMP 侧 @KNCallback 接口 OhosAppGyroscope。
 *
 * 使用 @kit.SensorServiceKit 的 sensor API（API 9+ 新版）：
 *  - sensor.getSingleSensorSync(SensorId.GYROSCOPE) 判断设备是否支持；
 *  - sensor.on(SensorId.GYROSCOPE, cb, options) 注册监听；
 *  - sensor.off(SensorId.GYROSCOPE, cb) 取消监听。
 *
 * 【ArkTS strict 模式下的关键类型约束】
 *  - sensor.on 重载签名：function on(type: SensorId.GYROSCOPE,
 *    callback: Callback<GyroscopeResponse>, options?: Options): void;
 *  - Options.interval 类型为 `number | SensorFrequency`，
 *    其中 SensorFrequency = 'game' | 'ui' | 'normal' 是字面量联合类型；
 *  - callback 参数必须使用 Callback<sensor.GyroscopeResponse>（来自 @ohos.base）标注；
 *  - interval 必须返回 sensor.SensorFrequency（字面量类型），不能是普通 string，
 *    否则 Options 类型检查失败，sensor.on 重载解析会 fall through 到
 *    旧版 SensorType.SENSOR_TYPE_ID_WEAR_DETECTION 重载并报类型不匹配错误。
 *  - 旧版 SensorType.SENSOR_TYPE_ID_GYROSCOPE 常量自 API 9 起已 deprecated，
 *    当前 SDK ArkTS strict 校验不接受，因此必须使用新版 SensorId.GYROSCOPE。
 *
 * 注意：
 *  - KMP 侧 OhosAppGyroscopeProvider 已经实现了多 listener 分发、monitorAxes 过滤、
 *    latestData 缓存等逻辑，ArkTS 侧只需要"启停 + 转发数据"这最小职责，同一时刻只持有
 *    一个 sensor.on 的注册。
 *  - timestampNanos 直接透传 sensor 回调里的 timestamp（鸿蒙 sensor API 的 timestamp 为纳秒）。
 */
export class OhosAppGyroscopeCallback {

  // 当前生效的 sensor 回调，非空表示 sensor.on 已注册
  // 必须使用 Callback<sensor.GyroscopeResponse>（来自 @ohos.base）以匹配 SDK 重载签名。
  private currentCallback: Callback<sensor.GyroscopeResponse> | null = null;

  /**
   * 判断设备是否支持陀螺仪传感器。
   */
  isAvailable(): boolean {
    try {
      const instance = sensor.getSingleSensorSync(sensor.SensorId.GYROSCOPE);
      return !!instance;
    } catch (e) {
      console.error(`[OhosAppGyroscope] isAvailable error: ${e}`);
      return false;
    }
  }

  /**
   * 启动陀螺仪监听。
   * @param samplingRateName 采样率名称（NORMAL/UI/GAME/FASTEST）
   * @param onData 每次传感器回调时调用，参数为 (x, y, z, timestampNanos)
   */
  startListening(
    samplingRateName: string,
    onData: (x: number, y: number, z: number, timestampNanos: number) => void,
  ): void {
    // 幂等：如已注册，先停掉老的再注册
    this.stopListening();
    try {
      const interval = this.toSensorInterval(samplingRateName);
      const callback: Callback<sensor.GyroscopeResponse> = (data: sensor.GyroscopeResponse): void => {
        try {
          onData(data.x, data.y, data.z, data.timestamp);
        } catch (cbErr) {
          console.error(`[OhosAppGyroscope] onData callback error: ${cbErr}`);
        }
      };
      this.currentCallback = callback;
      // 新版 API（API 9+）：sensor.on(SensorId.GYROSCOPE, Callback<GyroscopeResponse>, Options?)
      sensor.on(sensor.SensorId.GYROSCOPE, callback, { interval: interval });
    } catch (e) {
      console.error(`[OhosAppGyroscope] startListening error: ${e}`);
      this.currentCallback = null;
    }
  }

  /**
   * 停止陀螺仪监听。
   */
  stopListening(): void {
    if (!this.currentCallback) return;
    try {
      sensor.off(sensor.SensorId.GYROSCOPE, this.currentCallback);
    } catch (e) {
      console.error(`[OhosAppGyroscope] stopListening error: ${e}`);
    } finally {
      this.currentCallback = null;
    }
  }

  /**
   * 将 KMP 侧的 SensorSamplingRate 名称映射为鸿蒙 sensor interval。
   *
   * 鸿蒙 sensor.Options.interval 类型为 `number | SensorFrequency`，
   * 其中 SensorFrequency = 'game' | 'ui' | 'normal' 是字面量联合类型。
   * 因此本方法必须返回 sensor.SensorFrequency（字面量类型），
   * 不能返回普通 string，否则 ArkTS strict 下无法通过 Options 类型检查，
   * 进而导致 sensor.on 重载解析失败，fall through 到 WEAR_DETECTION 重载并报错。
   *
   *  - FASTEST → 'game'（大概 20ms 一次）
   *  - GAME    → 'game'
   *  - UI      → 'ui'    （大概 60ms 一次）
   *  - NORMAL  → 'normal'（大概 200ms 一次）
   */
  private toSensorInterval(samplingRateName: string): sensor.SensorFrequency {
    switch (samplingRateName) {
      case 'FASTEST':
      case 'GAME':
        return 'game';
      case 'UI':
        return 'ui';
      case 'NORMAL':
      default:
        return 'normal';
    }
  }
}
