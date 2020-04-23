declare module "@capacitor/core" {
  interface PluginRegistry {
    CosPlugin: CosPluginPlugin;
  }
}

export interface CosPluginPlugin {
  upload(options: { 
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
   }): Promise<any> ;
}
