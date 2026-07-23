import { IKmmContext } from './kniogen/callback';

export class WsKmmContext implements IKmmContext {
  windowId: number

  constructor(windowId: number) {
    this.windowId = windowId;
  }

  getWindowId(): number {
    return this.windowId;
  }
}