//
// CDVUID.m
// CordovaLib
//
// Created by ivan.dwijaya on 22/02/2016.
//
//

#import <Foundation/Foundation.h>
#import <Cordova/CDV.h>
#import "CDVUID.h"

@implementation CDVUID

-(void)getUID:(CDVInvokedUrlCommand *)command {
 CTTelephonyNetworkInfo* telInfo = [[CTTelephonyNetworkInfo alloc] init];
 CTCarrier* carrier = [telInfo subscriberCellularProvider];

 NSString* carrierName = [carrier carrierName];
 NSString* countryCode = [carrier mobileCountryCode];
 NSString* networkCode = [carrier mobileNetworkCode];

 if (!carrierName) {
 carrierName = @"";
 }

 if (!countryCode) {
 countryCode = @"";
 }

 if (!networkCode) {
 networkCode = @"";
 }

 NSString* result = [[carrierName stringByAppendingString:countryCode] stringByAppendingString:networkCode];

 NSDictionary *simData = [NSDictionary dictionaryWithObjectsAndKeys: result, @"iccid", nil];

 CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:simData];

 [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
