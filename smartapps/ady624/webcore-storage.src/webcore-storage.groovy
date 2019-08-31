/*
 *  webCoRE - Community's own Rule Engine - Web Edition
 *
 *  Copyright 2016 Adrian Caramaliu <ady624("at" sign goes here)gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Last update August 30, 2019 for Hubitat
 */
public static String version() { return "v0.3.10f.20190822" }
public static String HEversion() { return "v0.3.10f.20190830" }
/******************************************************************************/
/*** webCoRE DEFINITION														***/
/******************************************************************************/
private static String handle() { return "webCoRE" }
definition(
	name: "${handle()} Storage",
	namespace: "ady624",
	author: "Adrian Caramaliu",
	description: "Do not install this directly, use webCoRE instead",
	parent: "ady624:${handle()}",
	category: "Convenience",
	/* icons courtesy of @chauger - thank you */
	iconUrl: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE.png",
	iconX2Url: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE@2x.png",
	iconX3Url: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE@3x.png",
	importUrl: "https://raw.githubusercontent.com/imnotbob/webCoRE/hubitat-patches/smartapps/ady624/webcore-storage.src/webcore-storage.groovy"
)

preferences {
	//UI pages
	page(name: "pageSettings")
	page(name: "pageSelectDevices")
}

import groovy.transform.Field

/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/
def pageSettings() {
	//clear devices cache
	if (!parent || !parent.isInstalled()) {
		return dynamicPage(name: "pageSettings", title: "", install: false, uninstall: false) {
			section() {
				paragraph "Sorry, you cannot install a piston directly from the Dashboard, please use the webCoRE App instead."
			}
			section(sectionTitleStr("Installing webCoRE")) {
				paragraph "If you are trying to install webCoRE, please go back one step and choose webCoRE, not webCoRE Piston. You can also visit wiki.webcore.co for more information on how to install and use webCoRE"
				if (parent) {
					def t0 = parent.getWikiUrl()
					href "", title: imgTitle("https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png", inputTitleStr("More information")), description: t0, style: "external", url: t0, required: false
				}
			}
		}
	}
	dynamicPage(name: "pageSettings", title: "", install: true, uninstall: false) {
/*
		section("Available devices") {
			href "pageSelectDevices", title: "Available devices", description: "Tap here to select which devices are available to pistons"
		}
		section(sectionTitleStr('enable \$weather via ApiXU.com')) {
			input "apixuKey", "text", title: "ApiXU key?", description: "ApiXU key", required: false
			input "zipCode", "text", title: "Override Zip code or set city name or latitude,longitude? (Default: ${location.zipCode})", defaultValue: null, required: false
		}
*/
		section() {
			paragraph "Under Construction, managed by webCoRE App."
		}
	}
}

private pageSelectDevices() {
	parent.refreshDevices()
	dynamicPage(name: "pageSelectDevices", title: "") {
		section() {
			paragraph "Select the devices you want ${handle()} to have access to."
			paragraph "It is a good idea to only select the devices you plan on using with ${handle()} pistons. Pistons will only have access to the devices you selected."
		}

		section ('Select devices by type') {
			paragraph "Most devices should fall into one of these two categories"
				input "dev:actuator", "capability.actuator", multiple: true, title: "Which actuators", required: false, submitOnChange: true
				input "dev:sensor", "capability.sensor", multiple: true, title: "Which sensors", required: false, submitOnChange: true
				input "dev:all", "capability.*", multiple: true, title: "Devices", required: false
			}

		section ('Select devices by capability') {
			paragraph "If you cannot find a device by type, you may try looking for it by category below"
			def d
			for (capability in parent.capabilities().findAll{ (!(it.value.d in [null, 'actuators', 'sensors'])) }.sort{ it.value.d }) {
				if (capability.value.d != d) input "dev:${capability.key}", "capability.${capability.key}", multiple: true, title: "Which ${capability.value.d}", required: false, submitOnChange: true
				d = capability.value.d
			}
		}
	}
}

private String sectionTitleStr(title)	{ return "<h3>$title</h3>" }
private String inputTitleStr(title)	{ return "<u>$title</u>" }
private String pageTitleStr(title)	{ return "<h1>$title</h1>" }
private String paraTitleStr(title)	{ return "<b>$title</b>" }

private String imgTitle(String imgSrc, String titleStr, String color=(String)null, imgWidth=30, imgHeight=null) {
	def imgStyle = ""
	imgStyle += imgWidth ? "width: ${imgWidth}px !important;" : ""
	imgStyle += imgHeight ? "${imgWidth ? " " : ""}height: ${imgHeight}px !important;" : ""
	if(color) { return """<div style="color: ${color}; font-weight: bold;"><img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img></div>""" }
	else { return """<img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img>""" }
}

/******************************************************************************/
/*** 																		***/
/*** INITIALIZATION ROUTINES												***/
/*** 																		***/
/******************************************************************************/


void installed() {
	initialize()
	return// true
}

void updated() {
	unsubscribe()
	unschedule()
	initialize()
	startWeather()
	return// true
}

public void startWeather() {
	String myKey = state.apixuKey ?: null
	String myZip = state.zipCode ?: location.zipCode
	if(myKey && myZip) {
		unschedule()
		runEvery30Minutes(updateAPIXUdata)
		updateAPIXUdata()
	}
}

public void stopWeather() {
	state.apixuKey = null
	unschedule()
	stateRemove("obs")
}

private void initialize() {
	//update parent if this is managed devices.
	//parent.refreshDevices()
	stateRemove("obs")
}


public void updateAPIXUdata() {
	String myKey = state.apixuKey ?: null
	String myZip = state.zipCode ?: location.zipCode
	if(myKey && myZip) {
		String myUri = "https://api.apixu.com/v1/forecast.json?key=${myKey}&q=${myZip}&days=7"
		def params = [ uri: myUri ]
		try {
			asynchttpGet('ahttpRequestHandler', params, [tt: 'finishPoll'])
		} catch (e) {
			log.error "http call failed for ApiXU weather api: $e"
			return //false
		}
		return //true
	}
	return //false
}

@Field static Map theObsFLD

public void ahttpRequestHandler(resp, callbackData) {
	def json = [:]
	def obs = [:]
//	def err
	if ((resp.status == 200) && resp.data) {
		try {
			json = resp.getJson()
		} catch (all) {
			json = [:]
			return
		}

		if(!json) return
		if(json.forecast && json.forecast.forecastday) {
			for(int i = 0; i <= 6; i++) {
				def t0 = json.forecast.forecastday[i]?.day?.condition?.code
				if(!t0) continue
				String t1 = getWUIconName(t0,1)
				json.forecast.forecastday[i].day.condition.wuicon_name = t1
				String t2 = getWUIconNum(t0)
				json.forecast.forecastday[i].day.condition.wuicon = t2
			}
		}
		int tt0 = json.current.condition.code
		String tt1 = getWUIconName(tt0,1)
		json.current.condition.wuicon_name = tt1
		String tt2 = getWUIconNum(tt0)
		json.current.condition.wuicon = tt2

	} else {
		if(resp.hasError()) {
			log.error "apixu http Response Status: ${resp.status}   error Message: ${resp.getErrorMessage()}"
			return
		}
		log.error "apixu no data: ${resp.status}   resp.data: ${resp.data} resp.json: ${resp.json}"
		return
	}
	theObsFLD = json
//	state.obs = json
	return //null
	//log.debug "$json"
}

public Map getWData() {
	def obs = [:]
	//if(state.obs) {
	if(theObsFLD) {
		obs = theObsFLD //state.obs
		String t0 = "${obs.current.last_updated}"
		def t1 = formatDt(Date.parse("yyyy-MM-dd HH:mm", t0))
		int s = GetTimeDiffSeconds(t1, null, "getApiXUData").toInteger()
		if(s > (60*60*6)) { // if really old
			//stateRemove("obs")
			log.warn "removing very old weather data $t0   $s"
			theObsFLD = null
			obs = [:]
		} else return obs
	}
	return obs
}

def getTimeZone() {
	def tz = null
	if(location?.timeZone) { tz = location?.timeZone }
	if(!tz) { LogAction("getTimeZone: Hub or Nest TimeZone not found", "warn", true) }
	return tz
}

String getDtNow() {
	def now = new Date()
	return formatDt(now)
}

import java.text.SimpleDateFormat
//import groovy.time.*

String formatDt(dt) {
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) { tf.setTimeZone(getTimeZone()) }
	else {
		LogAction("HE TimeZone is not set; Please open your location and Press Save", "warn", true)
	}
	return tf.format(dt)
}

def GetTimeDiffSeconds(String strtDate, String stpDate=null, String methName=null) {
	//LogTrace("[GetTimeDiffSeconds] StartDate: $strtDate | StopDate: ${stpDate ?: "Not Sent"} | MethodName: ${methName ?: "Not Sent"})")
	if((strtDate && !stpDate) || (strtDate && stpDate)) {
		//if(strtDate?.contains("dtNow")) { return 10000 }
		def now = new Date()
		String stopVal = stpDate ? stpDate.toString() : formatDt(now)
		long start = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate).getTime()
		long stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
		long diff = (int) (long) (stop - start) / 1000
//		LogTrace("[GetTimeDiffSeconds] Results for '$methName': ($diff seconds)")
		return diff
	} else { return null }
}

public void settingsToState(myKey, setval) {
	if(setval) {
		atomicState."${myKey}" = setval
		state."${myKey}" = setval
	} else state.remove("${myKey}" as String)
}

void stateRemove(key) {
	state.remove(key?.toString())
	return //true
}

/******************************************************************************/
/*** 																		***/
/*** PUBLIC METHODS															***/
/*** 																		***/
/******************************************************************************/

public getStorageSettings(){
 	settings   
}

public void initData(devices, contacts) {
	if (devices) {
		for(item in devices) {
			if (item) {
				def deviceType = item.key.replace('dev:', 'capability.')
				def deviceIdList = item.value.collect{ it.id }
				app.updateSetting(item.key, [type: deviceType, value: deviceIdList])
			}
		}
	}
}

public Map listAvailableDevices(boolean raw = false) {
	def overrides = commandOverrides()
	if (raw) {
		return settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id)): dev]}
   	} else {
		return settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id)): dev]}.collectEntries{ id, dev -> [ (id): [ n: dev.getDisplayName(), cn: dev.getCapabilities()*.name, a: dev.getSupportedAttributes().unique{ it.name }.collect{def x = [n: it.name, t: it.getDataType(), o: it.getValues()]; try {x.v = dev.currentValue(x.n);} catch(all) {}; x}, c: dev.getSupportedCommands().unique{ transformCommand(it, overrides) }.collect{[n: transformCommand(it, overrides), p: it.getArguments()]} ]]}
	}
}

private def transformCommand(command, overrides){
	def override = overrides[command.getName()]
	if(override && override.s == command.getArguments()?.toString()){
		return override.r
	}
	return command.getName()
}

public Map getDashboardData() {
	def value
//	def start = now()
	return settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id)): dev]}.collectEntries{ id, dev ->
		[ (id): dev.getSupportedAttributes().collect{ it.name }.unique().collectEntries{
			try { value = dev.currentValue(it); } catch (all) { value = null};
			return [ (it) : value]
		}]
	}
}

public String mem(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

/* Push command has multiple overloads in hubitat */
private Map commandOverrides(){
	return (isHubitat() ? [
//		push : [c: "push", s: null , r: "pushMomentary"],
		flash : [c: "flash", s: null , r: "flashNative"],//s: command signature
	] : [:])
}

/******************************************************************************/
/***																		***/
/*** SECURITY METHODS														***/
/***																		***/
/******************************************************************************/
private String md5(String md5) {
	try {
		java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5")
		byte[] array = md.digest(md5.getBytes())
		String result = ""
		for (int i = 0; i < array.length; ++i) {
			result += Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3)
		}
		return result
	} catch (java.security.NoSuchAlgorithmException e) {
	}
	return null;
}

private String hashId(id) {
	//enabled hash caching for faster processing
	String result = state.hash ? state.hash[id] : null
	if (!result) {
		result = ":${md5("core." + id)}:"
		def hash = state.hash ?: [:]
		hash[id] = result
		state.hash = hash
		}
	return result
}

private isHubitat(){
 	return hubUID != null   
}


String getWUIconName(condition_code, int is_day=0)	 {
	def cC = condition_code
	String wuIcon = (conditionFactor[cC] ? conditionFactor[cC][2] : '')
	if (is_day != 1 && wuIcon)	wuIcon = 'nt_' + wuIcon;
	return wuIcon
}

String getWUIconNum(int wCode)	 {
	def imgItem = imgNames.find{ it.code == wCode }
	return (imgItem ? imgItem.img : '44')
}

@Field final Map	conditionFactor = [
	1000: ['Sunny', 1, 'sunny'],						1003: ['Partly cloudy', 0.8, 'partlycloudy'],
	1006: ['Cloudy', 0.6, 'cloudy'],					1009: ['Overcast', 0.5, 'cloudy'],
	1030: ['Mist', 0.5, 'fog'],						1063: ['Patchy rain possible', 0.8, 'chancerain'],
	1066: ['Patchy snow possible', 0.6, 'chancesnow'],			1069: ['Patchy sleet possible', 0.6, 'chancesleet'],
	1072: ['Patchy freezing drizzle possible', 0.4, 'chancesleet'],		1087: ['Thundery outbreaks possible', 0.2, 'chancetstorms'],
	1114: ['Blowing snow', 0.3, 'snow'],					1117: ['Blizzard', 0.1, 'snow'],
	1135: ['Fog', 0.2, 'fog'],						1147: ['Freezing fog', 0.1, 'fog'],
	1150: ['Patchy light drizzle', 0.8, 'rain'],				1153: ['Light drizzle', 0.7, 'rain'],
	1168: ['Freezing drizzle', 0.5, 'sleet'],				1171: ['Heavy freezing drizzle', 0.2, 'sleet'],
	1180: ['Patchy light rain', 0.8, 'rain'],				1183: ['Light rain', 0.7, 'rain'],
	1186: ['Moderate rain at times', 0.5, 'rain'],				1189: ['Moderate rain', 0.4, 'rain'],
	1192: ['Heavy rain at times', 0.3, 'rain'],				1195: ['Heavy rain', 0.2, 'rain'],
	1198: ['Light freezing rain', 0.7, 'sleet'],				1201: ['Moderate or heavy freezing rain', 0.3, 'sleet'],
	1204: ['Light sleet', 0.5, 'sleet'],					1207: ['Moderate or heavy sleet', 0.3, 'sleet'],
	1210: ['Patchy light snow', 0.8, 'flurries'],				1213: ['Light snow', 0.7, 'snow'],
	1216: ['Patchy moderate snow', 0.6, 'snow'],				1219: ['Moderate snow', 0.5, 'snow'],
	1222: ['Patchy heavy snow', 0.4, 'snow'],				1225: ['Heavy snow', 0.3, 'snow'],
	1237: ['Ice pellets', 0.5, 'sleet'],					1240: ['Light rain shower', 0.8, 'rain'],
	1243: ['Moderate or heavy rain shower', 0.3, 'rain'],			1246: ['Torrential rain shower', 0.1, 'rain'],
	1249: ['Light sleet showers', 0.7, 'sleet'],				1252: ['Moderate or heavy sleet showers', 0.5, 'sleet'],
	1255: ['Light snow showers', 0.7, 'snow'],				1258: ['Moderate or heavy snow showers', 0.5, 'snow'],
	1261: ['Light showers of ice pellets', 0.7, 'sleet'],			1264: ['Moderate or heavy showers of ice pellets',0.3, 'sleet'],
	1273: ['Patchy light rain with thunder', 0.5, 'tstorms'],		1276: ['Moderate or heavy rain with thunder', 0.3, 'tstorms'],
	1279: ['Patchy light snow with thunder', 0.5, 'tstorms'],		1282: ['Moderate or heavy snow with thunder', 0.3, 'tstorms']
]

private getImgName(wCode, is_day) {
	def url = "https://cdn.rawgit.com/adey/bangali/master/resources/icons/weather/"
	def imgItem = imgNames.find{ it.code == wCode && it.day == is_day }
	return (url + (imgItem ? imgItem.img : 'na') + '.png')
}

@Field final List imgNames = [
	[code: 1000, day: 1, img: '32', ],	// DAY - Sunny
	[code: 1003, day: 1, img: '30', ],	// DAY - Partly cloudy
	[code: 1006, day: 1, img: '28', ],	// DAY - Cloudy
	[code: 1009, day: 1, img: '26', ],	// DAY - Overcast
	[code: 1030, day: 1, img: '20', ],	// DAY - Mist
	[code: 1063, day: 1, img: '39', ],	// DAY - Patchy rain possible
	[code: 1066, day: 1, img: '41', ],	// DAY - Patchy snow possible
	[code: 1069, day: 1, img: '41', ],	// DAY - Patchy sleet possible
	[code: 1072, day: 1, img: '39', ],	// DAY - Patchy freezing drizzle possible
	[code: 1087, day: 1, img: '38', ],	// DAY - Thundery outbreaks possible
	[code: 1114, day: 1, img: '15', ],	// DAY - Blowing snow
	[code: 1117, day: 1, img: '16', ],	// DAY - Blizzard
	[code: 1135, day: 1, img: '21', ],	// DAY - Fog
	[code: 1147, day: 1, img: '21', ],	// DAY - Freezing fog
	[code: 1150, day: 1, img: '39', ],	// DAY - Patchy light drizzle
	[code: 1153, day: 1, img: '11', ],	// DAY - Light drizzle
	[code: 1168, day: 1, img: '8', ],	// DAY - Freezing drizzle
	[code: 1171, day: 1, img: '10', ],	// DAY - Heavy freezing drizzle
	[code: 1180, day: 1, img: '39', ],	// DAY - Patchy light rain
	[code: 1183, day: 1, img: '11', ],	// DAY - Light rain
	[code: 1186, day: 1, img: '39', ],	// DAY - Moderate rain at times
	[code: 1189, day: 1, img: '12', ],	// DAY - Moderate rain
	[code: 1192, day: 1, img: '39', ],	// DAY - Heavy rain at times
	[code: 1195, day: 1, img: '12', ],	// DAY - Heavy rain
	[code: 1198, day: 1, img: '8', ],	// DAY - Light freezing rain
	[code: 1201, day: 1, img: '10', ],	// DAY - Moderate or heavy freezing rain
	[code: 1204, day: 1, img: '5', ],	// DAY - Light sleet
	[code: 1207, day: 1, img: '6', ],	// DAY - Moderate or heavy sleet
	[code: 1210, day: 1, img: '41', ],	// DAY - Patchy light snow
	[code: 1213, day: 1, img: '18', ],	// DAY - Light snow
	[code: 1216, day: 1, img: '41', ],	// DAY - Patchy moderate snow
	[code: 1219, day: 1, img: '16', ],	// DAY - Moderate snow
	[code: 1222, day: 1, img: '41', ],	// DAY - Patchy heavy snow
	[code: 1225, day: 1, img: '16', ],	// DAY - Heavy snow
	[code: 1237, day: 1, img: '18', ],	// DAY - Ice pellets
	[code: 1240, day: 1, img: '11', ],	// DAY - Light rain shower
	[code: 1243, day: 1, img: '12', ],	// DAY - Moderate or heavy rain shower
	[code: 1246, day: 1, img: '12', ],	// DAY - Torrential rain shower
	[code: 1249, day: 1, img: '5', ],	// DAY - Light sleet showers
	[code: 1252, day: 1, img: '6', ],	// DAY - Moderate or heavy sleet showers
	[code: 1255, day: 1, img: '16', ],	// DAY - Light snow showers
	[code: 1258, day: 1, img: '16', ],	// DAY - Moderate or heavy snow showers
	[code: 1261, day: 1, img: '8', ],	// DAY - Light showers of ice pellets
	[code: 1264, day: 1, img: '10', ],	// DAY - Moderate or heavy showers of ice pellets
	[code: 1273, day: 1, img: '38', ],	// DAY - Patchy light rain with thunder
	[code: 1276, day: 1, img: '35', ],	// DAY - Moderate or heavy rain with thunder
	[code: 1279, day: 1, img: '41', ],	// DAY - Patchy light snow with thunder
	[code: 1282, day: 1, img: '18', ],	// DAY - Moderate or heavy snow with thunder
	[code: 1000, day: 0, img: '31', ],	// NIGHT - Clear
	[code: 1003, day: 0, img: '29', ],	// NIGHT - Partly cloudy
	[code: 1006, day: 0, img: '27', ],	// NIGHT - Cloudy
	[code: 1009, day: 0, img: '26', ],	// NIGHT - Overcast
	[code: 1030, day: 0, img: '20', ],	// NIGHT - Mist
	[code: 1063, day: 0, img: '45', ],	// NIGHT - Patchy rain possible
	[code: 1066, day: 0, img: '46', ],	// NIGHT - Patchy snow possible
	[code: 1069, day: 0, img: '46', ],	// NIGHT - Patchy sleet possible
	[code: 1072, day: 0, img: '45', ],	// NIGHT - Patchy freezing drizzle possible
	[code: 1087, day: 0, img: '47', ],	// NIGHT - Thundery outbreaks possible
	[code: 1114, day: 0, img: '15', ],	// NIGHT - Blowing snow
	[code: 1117, day: 0, img: '16', ],	// NIGHT - Blizzard
	[code: 1135, day: 0, img: '21', ],	// NIGHT - Fog
	[code: 1147, day: 0, img: '21', ],	// NIGHT - Freezing fog
	[code: 1150, day: 0, img: '45', ],	// NIGHT - Patchy light drizzle
	[code: 1153, day: 0, img: '11', ],	// NIGHT - Light drizzle
	[code: 1168, day: 0, img: '8', ],	// NIGHT - Freezing drizzle
	[code: 1171, day: 0, img: '10', ],	// NIGHT - Heavy freezing drizzle
	[code: 1180, day: 0, img: '45', ],	// NIGHT - Patchy light rain
	[code: 1183, day: 0, img: '11', ],	// NIGHT - Light rain
	[code: 1186, day: 0, img: '45', ],	// NIGHT - Moderate rain at times
	[code: 1189, day: 0, img: '12', ],	// NIGHT - Moderate rain
	[code: 1192, day: 0, img: '45', ],	// NIGHT - Heavy rain at times
	[code: 1195, day: 0, img: '12', ],	// NIGHT - Heavy rain
	[code: 1198, day: 0, img: '8', ],	// NIGHT - Light freezing rain
	[code: 1201, day: 0, img: '10', ],	// NIGHT - Moderate or heavy freezing rain
	[code: 1204, day: 0, img: '5', ],	// NIGHT - Light sleet
	[code: 1207, day: 0, img: '6', ],	// NIGHT - Moderate or heavy sleet
	[code: 1210, day: 0, img: '41', ],	// NIGHT - Patchy light snow
	[code: 1213, day: 0, img: '18', ],	// NIGHT - Light snow
	[code: 1216, day: 0, img: '41', ],	// NIGHT - Patchy moderate snow
	[code: 1219, day: 0, img: '16', ],	// NIGHT - Moderate snow
	[code: 1222, day: 0, img: '41', ],	// NIGHT - Patchy heavy snow
	[code: 1225, day: 0, img: '16', ],	// NIGHT - Heavy snow
	[code: 1237, day: 0, img: '18', ],	// NIGHT - Ice pellets
	[code: 1240, day: 0, img: '11', ],	// NIGHT - Light rain shower
	[code: 1243, day: 0, img: '12', ],	// NIGHT - Moderate or heavy rain shower
	[code: 1246, day: 0, img: '12', ],	// NIGHT - Torrential rain shower
	[code: 1249, day: 0, img: '5', ],	// NIGHT - Light sleet showers
	[code: 1252, day: 0, img: '6', ],	// NIGHT - Moderate or heavy sleet showers
	[code: 1255, day: 0, img: '16', ],	// NIGHT - Light snow showers
	[code: 1258, day: 0, img: '16', ],	// NIGHT - Moderate or heavy snow showers
	[code: 1261, day: 0, img: '8', ],	// NIGHT - Light showers of ice pellets
	[code: 1264, day: 0, img: '10', ],	// NIGHT - Moderate or heavy showers of ice pellets
	[code: 1273, day: 0, img: '47', ],	// NIGHT - Patchy light rain with thunder
	[code: 1276, day: 0, img: '35', ],	// NIGHT - Moderate or heavy rain with thunder
	[code: 1279, day: 0, img: '46', ],	// NIGHT - Patchy light snow with thunder
	[code: 1282, day: 0, img: '18', ]	// NIGHT - Moderate or heavy snow with thunder
]

/******************************************************************************/
/***																		***/
/*** END OF CODE															***/
/***																		***/
/******************************************************************************/
