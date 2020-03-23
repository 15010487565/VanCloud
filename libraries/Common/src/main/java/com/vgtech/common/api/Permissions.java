package com.vgtech.common.api;

/**
 * Created by zhangshaofang on 2015/10/29.
 */
public enum  Permissions {
    system,hr,notice,approve,user,admin,recruit,finance,investigation,unknow;

   public static Permissions getPermissions(String permission)
   {
       Permissions[] permissions =  Permissions.values();
       for(Permissions per:permissions)
       {
           if(per.toString().equals(permission))
           {
               return per;
           }
       }
       return unknow;
   }
}
