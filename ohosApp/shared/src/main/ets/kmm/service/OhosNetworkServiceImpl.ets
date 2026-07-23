import { registerServiceProvider } from 'knoi';
import { OhosNetworkService } from '../../kniogen/consumer';
import { process } from '@kit.ArkTS';
import { connection, statistics } from '@kit.NetworkKit';
import { BusinessError } from '@kit.BasicServicesKit';

// 网络状态字符串常量，与 KMM 侧 NetState.nameStr 保持一致
const NET_STATE_WIFI = 'wifi';
const NET_STATE_WWAN = 'wwan';
const NET_STATE_INAVAILABLE = 'inavailable';

export function registerOhosNetworkService() {
  registerServiceProvider('OhosNetworkService', true, new OhosNetworkServiceImpl())
}

export class OhosNetworkServiceImpl implements OhosNetworkService {
  private readonly TAG = 'OhosNetworkServiceImpl';
  private netConnection: connection.NetConnection | null = null;
  private netStateCallback: Function | null = null;

  subscribeNetState(onChange: (state: string) => void): void {
    this.netStateCallback = onChange;

    // 先同步获取当前网络状态并回调
    const currentNetState = this.getNetState();
    this.notifyNetStateChanged(currentNetState);

    this.netConnection = connection.createNetConnection();
    this.netConnection.on('netCapabilitiesChange', (data: connection.NetCapabilityInfo) => {
      const netState = this.getNetStateByBearerTypes(data.netCap?.bearerTypes ?? []);
      this.notifyNetStateChanged(netState);
    });
    this.netConnection.on('netLost', () => {
      const netState = this.getNetState();
      this.notifyNetStateChanged(netState);
    });
    this.netConnection.register((error: BusinessError) => {
      if (error) {
        console.error(`[${this.TAG}] register net state failed with error message=${error.message}`);
      }
    });
  }

  getTotalRxBytes(): object {
    return statistics.getUidRxBytes(process.uid);
  }

  getTotalTxBytes(): object {
    return statistics.getUidTxBytes(process.uid);
  }

  private getNetState(): string {
    try {
      const netHandle = connection.getDefaultNetSync();
      const bearerTypes = connection.getNetCapabilitiesSync(netHandle).bearerTypes;
      return this.getNetStateByBearerTypes(bearerTypes);
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      console.error(`[${this.TAG}] getNetState failed with error message=${message}`);
    }
    return NET_STATE_INAVAILABLE;
  }

  private getNetStateByBearerTypes(bearerTypes: connection.NetBearType[]): string {
    if (bearerTypes.includes(connection.NetBearType.BEARER_WIFI)) {
      return NET_STATE_WIFI;
    }
    if (bearerTypes.length > 0) {
      return NET_STATE_WWAN;
    }
    return NET_STATE_INAVAILABLE;
  }

  private notifyNetStateChanged(netState: string): void {
    try {
      this.netStateCallback?.(netState);
    } catch (error) {
      const message = error instanceof Error ? error.message : `${error}`;
      console.error(`[${this.TAG}] notifyNetStateChanged failed with error message=${message}`);
    }
  }
}
