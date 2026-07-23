import { hilog } from '@kit.PerformanceAnalysisKit';
import { Context } from '@kit.AbilityKit';
import { TuringShield } from 'turingantifraud';

const TAG = 'TuringInit';

export class TuringManager {
  private static instance: TuringManager;
  private isInitialized: boolean = false;
  private openIdTicket: string = '';
  private aidTicket: string = '';
  private taidTicket: string = '';
  private oaidTicket: string = '';

  static getInstance(): TuringManager {
    if (!TuringManager.instance) {
      TuringManager.instance = new TuringManager();
    }
    return TuringManager.instance;
  }

  initTuring(
    context: Context,
    appId: string,
    channelId: number,
    userId: string,
    isDebug: boolean,
    callback: Function,
  ): void {
    if (this.isInitialized) {
      callback(this.openIdTicket, this.aidTicket, this.taidTicket, this.oaidTicket);
      return;
    }

    void this.initTuringAsync(context, appId, channelId, userId, isDebug, callback);
  }

  private async initTuringAsync(
    context: Context,
    appId: string,
    channelId: number,
    userId: string,
    isDebug: boolean,
    callback: Function,
  ): Promise<void> {
    try {
      hilog.info(
        0x0000,
        TAG,
        `initTuring start, appId=${appId}, channelId=${channelId}, userId=${userId ? 'non-empty' : 'empty'}, isDebug=${isDebug}`,
      );
      const config: TuringShield.Config = {
        ctx: context,
        channel: channelId,
      };
      const code = await TuringShield.init(config);
      this.isInitialized = true;
      if (code !== 0) {
        hilog.error(0x0000, TAG, `TuringShield.init failed, code=${code}`);
        callback('', '', '', '');
        return;
      }

      const ticketResult = await TuringShield.getDeviceTicket();
      if (ticketResult.errCode !== 0) {
        hilog.error(0x0000, TAG, `getDeviceTicket failed, errCode=${ticketResult.errCode}`);
        callback('', '', '', '');
        return;
      }

      this.openIdTicket = ticketResult.tdidTicket ?? '';
      this.taidTicket = ticketResult.taidTicket ?? '';
      this.oaidTicket = ticketResult.oaidTicket ?? '';

      const tokenResult = await TuringShield.getDeviceToken();
      if (tokenResult.errCode === 0) {
        this.aidTicket = tokenResult.deviceToken ?? '';
      } else {
        hilog.error(0x0000, TAG, `getDeviceToken failed, errCode=${tokenResult.errCode}`);
      }

      hilog.info(
        0x0000,
        TAG,
        `initTuring success, openId=${this.openIdTicket ? 'non-empty' : 'empty'}, aid=${this.aidTicket ? 'non-empty' : 'empty'}, taid=${this.taidTicket ? 'non-empty' : 'empty'}, oaid=${this.oaidTicket ? 'non-empty' : 'empty'}`,
      );
      callback(this.openIdTicket, this.aidTicket, this.taidTicket, this.oaidTicket);
    } catch (error) {
      hilog.error(0x0000, TAG, `initTuring failed: ${JSON.stringify(error)}`);
      callback('', '', '', '');
    }
  }
}
