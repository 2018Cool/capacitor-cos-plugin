import { WebPlugin } from '@capacitor/core';
import { CosPluginPlugin } from './definitions';

export class CosPluginWeb extends WebPlugin implements CosPluginPlugin {
  constructor() {
    super({
      name: 'CosPlugin',
      platforms: ['web']
    });
  }

  async upload(options: { 
    signOptions:{
      region:string,
      secretId:string,
      secretKey:string,
      sessionToken:string,
      startTime:number,
      expiredTime:number,
      sliceSize:number,
      isHttps:boolean
    },
    bucket:string,
    cosUrl:string,
    cosPath:string,
    localPath:string
   }): Promise<any> {
    return options;
  }
}

const CosPlugin = new CosPluginWeb();

export { CosPlugin };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(CosPlugin);
