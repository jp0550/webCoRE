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
 * Last update October 26, 2020 for Hubitat
 */
public static String version(){ return "v0.3.110.20191009" }
public static String HEversion(){ return "v0.3.110.20200906_HE" }
/******************************************************************************/
/*** webCoRE DEFINITION														***/
/******************************************************************************/
private static String handle(){ return "webCoRE" }
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
	page(name: "pageDumpWeather")
}

import groovy.transform.Field

@Field static final String sBLK=''
@Field static final String sSPC=' '
@Field static final String sCOLON=':'

/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/
def pageSettings(){
	//clear devices cache
	if(!parent || !parent.isInstalled()){
		return dynamicPage(name: "pageSettings", title: "", install: false, uninstall: false){
			section(){
				paragraph "Sorry, you cannot install a piston directly from the Dashboard, please use the webCoRE App instead."
			}
			section(sectionTitleStr("Installing webCoRE")){
				paragraph "If you are trying to install webCoRE, please go back one step and choose webCoRE, not webCoRE Piston. You can also visit wiki.webcore.co for more information on how to install and use webCoRE"
				if(parent){
					def t0 = parent.getWikiUrl()
					href "", title: imgTitle("https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png", inputTitleStr("More information")), description: t0, style: "external", url: t0, required: false
				}
			}
		}
	}
	dynamicPage(name: "pageSettings", title: "", install: true, uninstall: false){
/*
		section("Available devices"){
			href "pageSelectDevices", title: "Available devices", description: "Tap here to select which devices are available to pistons"
		}
		section(sectionTitleStr('enable \$weather via ApiXU.com')){
			input "apixuKey", "text", title: "ApiXU key?", description: "ApiXU key", required: false
			input "zipCode", "text", title: "Override Zip code or set city name or latitude,longitude? (Default: ${location.zipCode})", defaultValue: null, required: false
		}
*/
		section(){
			href 'pageDumpWeather', title:'Dump weather structure', description:''
//			paragraph "Under Construction, managed by webCoRE App."
		}
	}
}

private pageSelectDevices(){
	parent.refreshDevices()
	dynamicPage(name: "pageSelectDevices", title: ""){
		section(){
			paragraph "Select the devices you want ${handle()} to have access to."
			paragraph "It is a good idea to only select the devices you plan on using with ${handle()} pistons. Pistons will only have access to the devices you selected."
		}

		section ('Select devices by type'){
			paragraph "Most devices should fall into one of these two categories"
				input "dev:actuator", "capability.actuator", multiple: true, title: "Which actuators", required: false, submitOnChange: true
				input "dev:sensor", "capability.sensor", multiple: true, title: "Which sensors", required: false, submitOnChange: true
				input "dev:all", "capability.*", multiple: true, title: "Devices", required: false
			}

		section ('Select devices by capability'){
			paragraph "If you cannot find a device by type, you may try looking for it by category below"
			def d
			for (capability in parent.capabilities().findAll{ (!(it.value.d in [null, 'actuators', 'sensors'])) }.sort{ it.value.d }){
				if(capability.value.d != d) input "dev:${capability.key}", "capability.${capability.key}", multiple: true, title: "Which ${capability.value.d}", required: false, submitOnChange: true
				d = capability.value.d
			}
		}
	}
}

private static String sectionTitleStr(String title)	{ return '<h3>'+title+'</h3>' }
private static String inputTitleStr(String title)	{ return '<u>'+title+'</u>' }
//private static String pageTitleStr(String title)	{ return '<h1>'+title+'</h1>' }
//private static String paraTitleStr(String title)	{ return '<b>'+title+'</b>' }

private static String imgTitle(String imgSrc, String titleStr, String color=(String)null, Integer imgWidth=30, Integer imgHeight=0){
	String imgStyle = sBLK
	imgStyle += imgWidth ? "width: ${imgWidth}px !important;" : ""
	imgStyle += imgHeight ? "${imgWidth ? " " : ""}height: ${imgHeight}px !important;" : ""
	if(color!=(String)null){ return """<div style="color: ${color}; font-weight: bold;"><img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img></div>""".toString()
	}
	else { return """<img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img>""".toString()
	}
}

/******************************************************************************/
/*** 																		***/
/*** INITIALIZATION ROUTINES												***/
/*** 																		***/
/******************************************************************************/


void installed(){
	initialize()
}

void updated(){
	unsubscribe()
	unschedule()
	initialize()
	startWeather()
}

public void startWeather(){
	String myKey = (String)state.apixuKey ?: (String)null
	String weatherType = (String)state.weatherType ?: (String)null
	if(myKey && weatherType){
		unschedule()
		runEvery30Minutes(updateWeatherD)
		updateWeatherD()
	}
}

public void stopWeather(){
	state.apixuKey = (String)null
	unschedule()
	stateRemove("obs")
}

private void initialize(){
	//update parent if this is managed devices.
	//parent.refreshDevices()
	stateRemove("obs")
	stateRemove('hash')
}


public void updateWeatherD(){
	String myKey = (String)state.apixuKey ?: (String)null
	String weatherType = (String)state.weatherType ?: (String)null
	String myZip = state.zipCode
	String myZip1 = state.zipCode1
	if((String)state.zipCode==(String)null || (String)state.zipCode == sBLK){
		switch(weatherType){
		case 'apiXU':
			myZip = location.zipCode
			break
		case 'DarkSky':
			myZip = location.latitude.toString()+','+location.longitude.toString()
			break
		case 'OpenWeatherMap':
			myZip = location.latitude.toString().replace(sSPC, sBLK)
			myZip1 = location.longitude.toString().replace(sSPC, sBLK)
		}
	}
	if(myKey && myZip && weatherType){
		String myUri
		switch(weatherType){
		case 'apiXU':
			myUri = 'https://api.apixu.com/v1/forecast.json?key=' +myKey+ '&q=' +myZip+ '&days=7'
			break
		case 'DarkSky':
			myUri = 'https://api.darksky.net/forecast/'+myKey+'/' + myZip + '?units=us&exclude=minutely,flags'
			break
		case 'OpenWeatherMap':
			myUri = 'https://api.openweathermap.org/data/2.5/onecall?lat=' + myZip + '&lon=' + myZip1 + '&exclude=minutely,hourly&mode=json&units=imperial&appid=' + myKey 
		}
		if(myUri){
			Map params = [ uri: myUri ]
			try {
				asynchttpGet('ahttpRequestHandler', params, [tt: 'finishPoll'])
			} catch (e){
				log.error "http call failed for $weatherType weather api: $e"
			}
		}else{ log.error "no weather URI found $weatherType" }
	}else{ log.error "missing some parameter" }
}

@Field static Map theObsFLD

public void ahttpRequestHandler(resp, callbackData){
	def json = [:]
	def obs = [:]
//	def err
	String weatherType = (String)state.weatherType ?: (String)null
	if((resp.status == 200) && resp.data){
		try {
			json = resp.getJson()
		} catch (all){
			json = [:]
			return
		}

		if(!json) return
		json.weatherType=weatherType

		if(weatherType == 'apiXU'){
			if(json.forecast && json.forecast.forecastday){
				List<Map> lt0=(List)json.forecast.forecastday
				for(Integer i = 0; i <= 6; i++){
					Integer t0 = lt0[i]?.day?.condition?.code
					if(!t0) continue
					String t1 = getWUIconName(t0,1)
					lt0[i].day.condition.wuicon_name = t1
					String t2 = getWUIconNum(t0)
					lt0[i].day.condition.wuicon = t2
				}
				json.forecast.forecastday=lt0
			}
			Integer tt0 = json.current.condition.code
			String tt1 = getWUIconName(tt0,1)
			json.current.condition.wuicon_name = tt1
			String tt2 = getWUIconNum(tt0)
			json.current.condition.wuicon = tt2
		} else if(weatherType == 'DarkSky'){

			def sunTimes = app.getSunriseAndSunset()
			Long sunrise = sunTimes.sunrise.time
			Long sunset = sunTimes.sunset.time
			Long time = now()

			Boolean is_day = true
			if(sunrise <= time && sunset >= time){
				;
			}else{
				is_day = false
			}

			json.name = location.name
			json.zipCode = location.zipCode
			if(json.currently){
				Map t0 = (Map)json.currently
				String c_code = getdsIconCode((String)t0.icon, (String)t0.summary, !is_day)
				json.currently.condition_code = c_code
				json.currently.condition_text = getcondText(c_code)

				c_code = getdsIconCode((String)t0.icon, (String)t0.summary)
				String c1 = getStdIcon(c_code)
				Integer wuCode = getWUConditionCode(c1)
				String tt2 = getWUIconNum(wuCode)
				json.currently.code = wuCode
				json.currently.wuicon = tt2

				List<Map> lt0=(List)json?.daily?.data
				t0 = lt0 ? (Map)lt0[0] : [:]
				String f_code = getdsIconCode((String)t0?.icon, (String)t0?.summary, !is_day)
				json.currently.forecast_code = f_code
				json.currently.forecast_text = getcondText(f_code)

				f_code = getdsIconCode((String)t0.icon, (String)t0.summary)
				String f1 = getStdIcon(f_code)
				wuCode = getWUConditionCode(f1)
				//String tt1 = getWUIconName(wuCode,1)
				tt2 = getWUIconNum(wuCode)
				json.currently.fcode = wuCode
				//json.currently.wuicon_name = tt1
				json.currently.fwuicon = tt2
			}
			if(json.hourly && json.hourly.data){
				List<Map> lt0=(List)json?.hourly?.data
				List<Map> lt1=(List)json?.daily?.data
				Integer hr = new Date(now()).hours
				Integer indx = 0
				for(Integer i = 0; i <= 50; i++){
					Map t0 = (Map)lt0[i]
					if(!t0) continue

					Map t1 = lt1 ? (Map)lt1[indx] : [:]

					sunrise = (Long)t1.sunriseTime
					sunset = (Long)t1.sunsetTime
					time = (Long)t0.time.toLong()
					is_day = true
					if(sunrise <= time && sunset >= time){
						;
					}else{
						is_day = false
					}

					String c_code = getdsIconCode((String)t0.icon, (String)t0.summary, !is_day)
					lt0[i].condition_code = c_code
					lt0[i].condition_text = getcondText(c_code)

					c_code = getdsIconCode((String)t0.icon, (String)t0.summary)
					String c1 = getStdIcon(c_code)
					Integer wuCode = getWUConditionCode(c1)
					String tt2 = getWUIconNum(wuCode)
					lt0[i].code = wuCode
					lt0[i].wuicon = tt2

					String f_code = getdsIconCode((String)t1?.icon, (String)t1?.summary)
					lt0[i].forecast_code = f_code
					lt0[i].forecast_text = getcondText(f_code)

					f_code = getdsIconCode((String)t1.icon, (String)t1.summary)
					String f1 = getStdIcon(f_code)
					wuCode = getWUConditionCode(f1)
					tt2 = getWUIconNum(wuCode)
					lt0[i].fcode = wuCode
					lt0[i].fwuicon = tt2

					hr+=1
					if(hr != hr%24){
						hr %= 24
						indx += 1
					}
				}
				json.hourly.data=lt0
			}
			if(json.daily && json.daily.data){
				List<Map> lt0=(List)json?.daily?.data
				for(Integer i = 0; i <= 31; i++){
					Map t0 = lt0 ? (Map)lt0[i] : [:]
					if(!t0) continue
					String c_code = getdsIconCode((String)t0.icon, (String)t0.summary)
					lt0[i].condition_code = c_code
					lt0[i].condition_text = getcondText(c_code)

					String c1 = getStdIcon(c_code)
					Integer wuCode = getWUConditionCode(c1)
					String tt2 = getWUIconNum(wuCode)
					lt0[i].code = wuCode
					lt0[i].wuicon = tt2
				}
				json.daily.data=lt0
			}
//			String jsonData = groovy.json.JsonOutput.toJson(json)
//log.debug jsonData
		} else if(weatherType == 'OpenWeatherMap'){
//			String jsonData = groovy.json.JsonOutput.toJson(json)
//log.debug jsonData
		}
	}else{
		if(resp.hasError()){
			log.error "$weatherType http Response Status: ${resp.status}  error Message: ${resp.getErrorMessage()}"
			return
		}
		log.error "$weatherType no data: ${resp.status}  resp.data: ${resp.data} resp.json: ${resp.json}"
		return
	}
	theObsFLD = json
	//log.debug "$json"
}

public Map getWData(){
	Map obs = [:]
	String weatherType = (String)state.weatherType ?: (String)null
	if(theObsFLD){
		if(weatherType == 'apiXU'){
			obs = theObsFLD
			String t0 = "${obs.current.last_updated}".toString()
			String t1 = formatDt(Date.parse("yyyy-MM-dd HH:mm", t0))
			Integer s = GetTimeDiffSeconds(t1, (String)null, "getApiXUData").toInteger()
			if(s > (60*60*6)){ // if really old
				log.warn "removing very old weather data $t0   $s"
				theObsFLD = null
				obs = [:]
			}
		}
		if(weatherType == 'DarkSky' || weatherType == 'OpenWeatherMap'){
			obs = theObsFLD
		}
	}
	return obs
}

static String dumpListDesc(data, Integer level, List lastLevel, String listLabel, Boolean html=false){
	String str=sBLK
	Integer cnt=1
	List newLevel=lastLevel

	List list1=data?.collect{it}
	Integer sz=(Integer)list1.size()
	list1?.each{ par ->
		Integer t0=cnt-1
		String myStr="${listLabel}[${t0}]".toString()
		if(par instanceof Map){
			Map newmap=[:]
			newmap[myStr]=(Map)par
			Boolean t1= cnt==sz
			newLevel[level]=t1
			str += dumpMapDesc(newmap, level, newLevel, !t1, html)
		}else if(par instanceof List || par instanceof ArrayList){
			Map newmap=[:]
			newmap[myStr]=par
			Boolean t1= cnt==sz
			newLevel[level]=t1
			str += dumpMapDesc(newmap, level, newLevel, !t1, html)
		}else{
			String lineStrt='\n'
			for(Integer i=0; i<level; i++){
				lineStrt += (i+1<level)? (!lastLevel[i] ? '     │' : '      '):'      '
			}
			lineStrt += (cnt==1 && sz>1)? '┌─ ':(cnt<sz ? '├─ ' : '└─ ')
			if(html)str += '<span>'
			str += "${lineStrt}${listLabel}[${t0}]: ${par} (${getObjType(par)})".toString()
			if(html)str += '</span>'
		}
		cnt=cnt+1
	}
	return str
}

static String dumpMapDesc(Map data, Integer level, List lastLevel, Boolean listCall=false, Boolean html=false){
	String str=sBLK
	Integer cnt=1
	Integer sz=data?.size()
	data?.each{ par ->
		String lineStrt
		List newLevel=lastLevel
		Boolean thisIsLast= cnt==sz && !listCall
		if(level>0){
			newLevel[(level-1)]=thisIsLast
		}
		Boolean theLast=thisIsLast
		if(level==0){
			lineStrt='\n\n • '
		}else{
			theLast= theLast && thisIsLast
			lineStrt='\n'
			for(Integer i=0; i<level; i++){
				lineStrt += (i+1<level)? (!newLevel[i] ? '     │' : '      '):'      '
			}
			lineStrt += ((cnt<sz || listCall) && !thisIsLast) ? '├─ ' : '└─ '
		}
		String objType=getObjType(par.value)
		if(par.value instanceof Map){
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: (${objType})".toString()
			if(html)str += '</span>'
			newLevel[(level+1)]=theLast
			str += dumpMapDesc((Map)par.value, level+1, newLevel, false, html)
		}
		else if(par.value instanceof List || par.value instanceof ArrayList){
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: [${objType}]".toString()
			if(html)str += '</span>'
			newLevel[(level+1)]=theLast
			str += dumpListDesc(par.value, level+1, newLevel, sBLK, html)
		}
		else{
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: (${par.value}) (${objType})".toString()
			if(html)str += '</span>'
		}
		cnt=cnt+1
	}
	return str
}

static String myObj(obj){
	if(obj instanceof String){return 'String'}
	else if(obj instanceof Map){return 'Map'}
	else if(obj instanceof List){return 'List'}
	else if(obj instanceof ArrayList){return 'ArrayList'}
	else if(obj instanceof Integer){return 'Int'}
	else if(obj instanceof BigInteger){return 'BigInt'}
	else if(obj instanceof Long){return 'Long'}
	else if(obj instanceof Boolean){return 'Bool'}
	else if(obj instanceof BigDecimal){return 'BigDec'}
	else if(obj instanceof Float){return 'Float'}
	else if(obj instanceof Byte){return 'Byte'}
	else{ return 'unknown'}
}

static String getObjType(obj){
	return "<span style='color:orange'>"+myObj(obj)+"</span>"
}

static String getMapDescStr(Map data){
	String str
	List lastLevel=[true]
	str=dumpMapDesc(data, 0, lastLevel, false, true)
	return str!=sBLK ? str:'No Data was returned'
}

def pageDumpWeather(){
	Map obs = theObsFLD
	String message=getMapDescStr(obs)
	return dynamicPage(name:'pageDumpWeather', title:sBLK, uninstall:false){
		section('Weather Data dump'){
			paragraph message
		}
	}
}

def getTimeZone(){
	def tz = null
	if(location?.timeZone){ tz = location?.timeZone }
	if(!tz){ log.error "getTimeZone: Hub or Nest TimeZone not found" }
	return tz
}

String getDtNow(){
	Date now = new Date()
	return formatDt(now)
}

import java.text.SimpleDateFormat
//import groovy.time.*

String formatDt(dt){
	def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()){ tf.setTimeZone(getTimeZone()) }
	else { log.error "HE TimeZone is not set; Please open your location and Press Save" }
	return tf.format(dt)
}

Long GetTimeDiffSeconds(String strtDate, String stpDate=(String)null, String methName=(String)null){
	if((strtDate && !stpDate) || (strtDate && stpDate)){
		//if(strtDate?.contains("dtNow")){ return 10000 }
		Date now = new Date()
		String stopVal = stpDate ? stpDate.toString() : formatDt(now)
		Long start = Date.parse("E MMM dd HH:mm:ss z yyyy", strtDate).getTime()
		Long stop = Date.parse("E MMM dd HH:mm:ss z yyyy", stopVal).getTime()
		Long diff = (stop - start) / 1000L
		return diff
	}else{ return null }
}

public void settingsToState(myKey, setval){
	if(setval){
		atomicState."${myKey}" = setval
		state."${myKey}" = setval
	} else state.remove("${myKey}" as String)
}

void stateRemove(key){
	state.remove(key?.toString())
}

/******************************************************************************/
/*** 																		***/
/*** PUBLIC METHODS															***/
/*** 																		***/
/******************************************************************************/

public getStorageSettings(){
 	settings
}

public void initData(devices, contacts){
	if(devices){
		for(item in devices){
			if(item){
				def deviceType = item.key.replace('dev:', 'capability.')
				def deviceIdList = item.value.collect{ it.id }
				app.updateSetting(item.key, [type: deviceType, value: deviceIdList])
			}
		}
	}
}

public Map listAvailableDevices(Boolean raw = false, Integer offset = 0){
	Long time = now()
	Map response = [:]
	def myDevices = settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().sort{ it.getDisplayName() }
	def devices = myDevices.unique{ it.id }
	if(raw){
		response = devices.collectEntries{ dev -> [(hashId(dev.id)): dev]}
	}else{
		Integer deviceCount = devices.size()
		Map<String,Map> overrides = commandOverrides()
		response.devices = [:]
		if(devices){
		devices = devices[offset..-1]
		response.complete = !devices.indexed().find{ idx, dev ->
//			log.debug "Loaded device at ${idx} after ${now() - time}ms. Data size is ${response.toString().size()}"
			response.devices[hashId(dev.id)] = [
				n: dev.getDisplayName(),
				cn: dev.getCapabilities()*.name,
				a: dev.getSupportedAttributes().unique{ it.name }.collect{[
					n: it.name,
					t: it.getDataType(),
					o: it.getValues()
				]},
				c: dev.getSupportedCommands().unique{ transformCommand(it, overrides) }.collect{[
					n: transformCommand(it, overrides),
					p: it.getArguments()
				]}
			]
			Boolean stop = false
			def jsonData = groovy.json.JsonOutput.toJson(response)
			Integer responseLength = jsonData.getBytes("UTF-8").length
			if(responseLength > (50 * 1024)){
				stop = true // Stop if large
			}
			if(now() - time > 4000) stop = true
			if(idx < devices.size() - 1 && stop){
				response.nextOffset = offset + idx + 1
				return true
			}
			false
		}
		} else response.complete=true
		log.debug "Generated list of ${offset}-${offset + devices.size()} of ${deviceCount} devices in ${now() - time}ms. Data size is ${response.toString().size()}"
	}
	return response
}

private static String transformCommand(command, Map<String,Map> overrides){
	Map override = overrides[(String)command.getName()]
	if(override && (String)override.s == command.getArguments()?.toString()){
		return (String)override.r
	}
	return (String)command.getName()
}

public Map getDashboardData(){
	def value
//	def start = now()
	return settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id)): dev]}.collectEntries{ id, dev ->
		[ (id): dev.getSupportedAttributes().collect{ it.name }.unique().collectEntries{
			try { value = dev.currentValue(it) } catch (all){ value = null}
			return [ (it) : value]
		}]
	}
}

public String mem(Boolean showBytes = true){
	Integer bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

/* Push command has multiple overloads in hubitat */
private static Map<String, Map> commandOverrides(){
	return ( [ //s: command signature
		push    : [c: "push",   s: null , r: "pushMomentary"],
		flash   : [c: "flash",  s: null , r: "flashNative"] //flash native command conflicts with flash emulated command. Also needs "o" option on command described later
	] ) as HashMap
}

/******************************************************************************/
/***																		***/
/*** SECURITY METHODS														***/
/***																		***/
/******************************************************************************/
private String md5(String md5){
		java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5")
		byte[] array = md.digest(md5.getBytes())
		String result = sBLK
		for (Integer i = 0; i < array.length; ++i){
			result += Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3)
		}
		return result
}

@Field static Map theHashMapFLD=[:]

private String hashId(id){
	//enabled hash caching for faster processing
	String myId=id.toString()
	String result = (String)theHashMapFLD[myId]
	if(result==(String)null){
		result=sCOLON+md5('core.'+myId)+sCOLON
		theHashMapFLD[myId]=result
	}
	return result
}

/*private isHubitat(){
 	return hubUID != null
}*/

String getWUIconName(condition_code, Integer is_day=0)	 {
	def cC = condition_code
	String wuIcon = (conditionFactor[cC] ? (String)conditionFactor[cC][2] : sBLK)
	if(is_day != 1 && wuIcon) wuIcon = 'nt_' + wuIcon
	return wuIcon
}

Integer getWUConditionCode(String code){
	for (myMap in conditionFactor){
		if((String)myMap.value[2] == code) return myMap.key
	}
	return 0
}

String getWUIconNum(Integer wCode)	 {
	Map imgItem = imgNames.find{ (Integer)it.code == wCode }
	return (imgItem ? (String)imgItem.img : '44')
}

@Field final Map<Integer,List>	conditionFactor = [
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

private String getImgName(Integer wCode, is_day){
	String url = "https://cdn.rawgit.com/adey/bangali/master/resources/icons/weather/"
	Map imgItem = imgNames.find{ (Integer)it.code == wCode && (Integer)it.day == is_day }
	return (url + (imgItem ? (String)imgItem.img : 'na') + '.png')
}

@Field final List<Map> imgNames = [
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

// From Darksky.net driver for HE https://community.hubitat.com/t/release-darksky-net-weather-driver-no-pws-required/22699
static String getdsIconCode(String icon='unknown', String dcs='unknown', Boolean isNight=false){
	String unk='unknown'
	if(dcs==null) dcs=unk
	if(icon==null) icon=unk
	switch(icon){
		case 'rain':
		// rain=[Possible Light Rain, Light Rain, Rain, Heavy Rain, Drizzle, Light Rain and Breezy, Light Rain and Windy,
		//       Rain and Breezy, Rain and Windy, Heavy Rain and Breezy, Rain and Dangerously Windy, Light Rain and Dangerously Windy],
			if(dcs == 'Drizzle'){
				icon = 'drizzle'
			} else if       (dcs.startsWith('Light Rain')){
				icon = 'lightrain'
				if(dcs.contains('Breezy')) icon += 'breezy'
				else if(dcs.contains('Windy')) icon += 'windy'
			} else if       (dcs.startsWith('Heavy Rain')){
				icon = 'heavyrain'
				if	(dcs.contains('Breezy')) icon += 'breezy'
				else if(dcs.contains('Windy')) icon += 'windy'
			} else if       (dcs == 'Possible Light Rain'){
				icon = 'chancelightrain'
			} else if       (dcs.startsWith('Possible')){
				icon = 'chancerain'
			} else if       (dcs.startsWith('Rain')){
				if	(dcs.contains('Breezy')) icon += 'breezy'
				else if(dcs.contains('Windy')) icon += 'windy'
			}
			break
		case 'snow':
			if      (dcs == 'Light Snow') icon = 'lightsnow'
			else if(dcs == 'Flurries') icon = 'flurries'
			else if(dcs == 'Possible Light Snow') icon = 'chancelightsnow'
			else if(dcs.startsWith('Possible Light Snow')){
				if      (dcs.contains('Breezy')) icon = 'chancelightsnowbreezy'
				else if(dcs.contains('Windy')) icon = 'chancelightsnowwindy'
			} else if(dcs.startsWith('Possible')) icon = 'chancesnow'
			break
		case 'sleet':
			if(dcs.startsWith('Possible')) icon = 'chancesleet'
			else if(dcs.startsWith('Light')) icon = 'lightsleet'
			break
		case 'thunderstorm':
			if(dcs.startsWith('Possible')) icon = 'chancetstorms'
			break
		case 'partly-cloudy-night':
			if(dcs.contains('Mostly Cloudy')) icon = 'mostlycloudy'
			else icon = 'partlycloudy'
			break
		case 'partly-cloudy-day':
			if(dcs.contains('Mostly Cloudy')) icon = 'mostlycloudy'
			else icon = 'partlycloudy'
			break
		case 'cloudy-night':
			icon = 'cloudy'
			break
		case 'cloudy':
		case 'cloudy-day':
			icon = 'cloudy'
			break
		case 'clear-night':
			icon = 'clear'
			break
		case 'clear':
		case 'clear-day':
			icon = 'clear'
			break
		case 'fog':
		case 'wind':
			// wind=[Windy and Overcast, Windy and Mostly Cloudy, Windy and Partly Cloudy, Breezy and Mostly Cloudy, Breezy and Partly Cloudy,
			// Breezy and Overcast, Breezy, Windy, Dangerously Windy and Overcast, Windy and Foggy, Dangerously Windy and Partly Cloudy, Breezy and Foggy]}
			if(dcs.contains('Windy')){
				// icon = 'wind'
				if	(dcs.contains('Overcast'))	icon = 'windovercast'
				else if(dcs.contains('Mostly Cloudy')) icon = 'windmostlycloudy'
				else if(dcs.contains('Partly Cloudy')) icon = 'windpartlycloudy'
				else if(dcs.contains('Foggy'))	   icon = 'windfoggy'
			} else if(dcs.contains('Breezy')){
				icon = 'breezy'
				if	(dcs.contains('Overcast'))	icon = 'breezyovercast'
				else if(dcs.summary.contains('Mostly Cloudy')) icon = 'breezymostlycloudy'
				else if(dcs.contains('Partly Cloudy')) icon = 'breezypartlycloudy'
				else if(dcs.contains('Foggy'))		icon = 'breezyfoggy'
			}
			break
		case '':
			icon = unk
			break
		default:
			icon = unk
	}
	if(isNight) icon = 'nt_' + icon
	return icon
}

String getcondText(String wCode){
	String code = wCode.contains('nt_') ? wCode.substring(3, wCode.size()-1) : wCode
	//log.info("getImgName Input: wCode: " + code)
	Map LUitem = LUTable.find{ (String)it.ccode == code }
	return (LUitem ? (String)LUitem.ctext : sBLK)
}

String getStdIcon(String code){
	Map LUitem = LUTable.find{ (String) it.ccode == code }
	return (LUitem ? (String)LUitem.stdIcon : sBLK)
}

@Field final List<Map> LUTable = [
[ ccode: 'breezy', altIcon: '23.png', ctext: 'Breezy', owmIcon: '50d', stdIcon: 'partlycloudy', luxpercent: 0.8 ],
[ ccode: 'breezyfoggy', altIcon: '48.png', ctext: 'Breezy and Foggy', owmIcon: '50d', stdIcon: 'fog', luxpercent: 0.2 ],
[ ccode: 'breezymostlycloudy', altIcon: '51.png', ctext: 'Breezy and Mostly Cloudy', owmIcon: '04d', stdIcon: 'cloudy', luxpercent: 0.6 ],
[ ccode: 'breezyovercast', altIcon: '49.png', ctext: 'Breezy and Overcast', owmIcon: '04d', stdIcon: 'cloudy', luxpercent: 0.6 ],
[ ccode: 'breezypartlycloudy', altIcon: '53.png', ctext: 'Breezy and Partly Cloudy', owmIcon: '03d', stdIcon: 'partlycloudy', luxpercent: 0.8 ],
[ ccode: 'chancelightrain', altIcon: '39.png', ctext: 'Chance of Light Rain', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'chancelightsnow', altIcon: '41.png', ctext: 'Possible Light Snow', owmIcon: '13d', stdIcon: 'snow', luxpercent: 0.3 ],
[ ccode: 'chancelightsnowbreezy', altIcon: '54.png', ctext: 'Possible Light Snow and Breezy', owmIcon: '13d', stdIcon: 'snow', luxpercent: 0.3 ],
[ ccode: 'chancerain', altIcon: '39.png', ctext: 'Chance of Rain', owmIcon: '10d', stdIcon: 'chancerain', luxpercent: 0.7 ],
[ ccode: 'chancesleet', altIcon: '41.png', ctext: 'Chance of Sleet', owmIcon: '13d', stdIcon: 'chancesleet', luxpercent: 0.7 ],
[ ccode: 'chancesnow', altIcon: '41.png', ctext: 'Chance of Snow', owmIcon: '13d', stdIcon: 'chancesnow', luxpercent: 0.3 ],
[ ccode: 'chancetstorms', altIcon: '38.png', ctext: 'Chance of Thunderstorms', owmIcon: '11d', stdIcon: 'chancetstorms', luxpercent: 0.2 ],
[ ccode: 'chancelightsnowwindy', altIcon: '54.png', ctext: 'Possible Light Snow and Windy', owmIcon: '13d', stdIcon: 'chancesnow', luxpercent: 0.3 ],
[ ccode: 'clear', altIcon: '32.png', ctext: 'Clear', owmIcon: '01d', stdIcon: 'sunny', luxpercent: 1 ],
[ ccode: 'cloudy', altIcon: '26.png', ctext: 'Overcast', owmIcon: '04d', stdIcon: 'cloudy', luxpercent: 0.6 ],
[ ccode: 'drizzle', altIcon: '9.png', ctext: 'Drizzle', owmIcon: '09d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'flurries', altIcon: '13.png', ctext: 'Snow Flurries', owmIcon: '13d', stdIcon: 'flurries', luxpercent: 0.4 ],
[ ccode: 'fog', altIcon: '19.png', ctext: 'Foggy', owmIcon: '50d', stdIcon: 'fog', luxpercent: 0.2 ],
[ ccode: 'heavyrain', altIcon: '12.png', ctext: 'Heavy Rain', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'heavyrainbreezy', altIcon: '1.png', ctext: 'Heavy Rain and Breezy', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'heavyrainwindy', altIcon: '1.png', ctext: 'Heavy Rain and Windy', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'lightrain', altIcon: '11.png', ctext: 'Light Rain', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'lightrainbreezy', altIcon: '2.png', ctext: 'Light Rain and Breezy', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'lightrainwindy', altIcon: '2.png', ctext: 'Light Rain and Windy', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'lightsleet', altIcon: '8.png', ctext: 'Light Sleet', owmIcon: '13d', stdIcon: 'sleet', luxpercent: 0.5 ],
[ ccode: 'lightsnow', altIcon: '14.png', ctext: 'Light Snow', owmIcon: '13d', stdIcon: 'snow', luxpercent: 0.3 ],
[ ccode: 'mostlycloudy', altIcon: '28.png', ctext: 'Mostly Cloudy', owmIcon: '04d', stdIcon: 'cloudy', luxpercent: 0.6 ],
[ ccode: 'partlycloudy', altIcon: '30.png', ctext: 'Partly Cloudy', owmIcon: '03d', stdIcon: 'partlycloudy', luxpercent: 0.8 ],
[ ccode: 'rain', altIcon: '12.png', ctext: 'Rain', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'rainbreezy', altIcon: '1.png', ctext: 'Rain and Breezy', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'rainwindy', altIcon: '1.png', ctext: 'Rain and Windy', owmIcon: '10d', stdIcon: 'rain', luxpercent: 0.5 ],
[ ccode: 'sleet', altIcon: '10.png', ctext: 'Sleet', owmIcon: '13d', stdIcon: 'sleet', luxpercent: 0.5 ],
[ ccode: 'snow', altIcon: '15.png', ctext: 'Snow', owmIcon: '13d', stdIcon: 'snow', luxpercent: 0.3 ],
[ ccode: 'sunny', altIcon: '36.png', ctext: 'Sunny', owmIcon: '01d', stdIcon: 'sunny', luxpercent: 1 ],
[ ccode: 'thunderstorm', altIcon: '0.png', ctext: 'Thunderstorm', owmIcon: '11d', stdIcon: 'tstorms', luxpercent: 0.3 ],
[ ccode: 'wind', altIcon: '23.png', ctext: 'Windy', owmIcon: '50d', stdIcon: 'partlycloudy', luxpercent: 0.8 ],
[ ccode: 'windfoggy', altIcon: '23.png', ctext: 'Windy and Foggy', owmIcon: '50d', stdIcon: 'fog', luxpercent: 0.2 ],
[ ccode: 'windmostlycloudy', altIcon: '51.png', ctext: 'Windy and Mostly Cloudy', owmIcon: '50d', stdIcon: 'cloudy', luxpercent: 0.6 ],
[ ccode: 'windovercast', altIcon: '49.png', ctext: 'Windy and Overcast', owmIcon: '50d', stdIcon: 'cloudy', luxpercent: 0.6 ],
[ ccode: 'windpartlycloudy', altIcon: '53.png', ctext: 'Windy and Partly Cloudy', owmIcon: '50d', stdIcon: 'partlycloudy', luxpercent: 0.8 ],
[ ccode: 'nt_breezy', altIcon: '23.png', ctext: 'Breezy', owmIcon: '50n', stdIcon: 'nt_partlycloudy', luxpercent: 0 ],
[ ccode: 'nt_breezyfoggy', altIcon: '48.png', ctext: 'Breezy and Foggy', owmIcon: '50n', stdIcon: 'nt_fog', luxpercent: 0 ],
[ ccode: 'nt_breezymostlycloudy', altIcon: '50.png', ctext: 'Breezy and Mostly Cloudy', owmIcon: '04n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
[ ccode: 'nt_breezyovercast', altIcon: '49.png', ctext: 'Breezy and Overcast', owmIcon: '04n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
[ ccode: 'nt_breezypartlycloudy', altIcon: '52.png', ctext: 'Breezy and Partly Cloudy', owmIcon: '03n', stdIcon: 'nt_partlycloudy', luxpercent: 0 ],
[ ccode: 'nt_chancelightrain', altIcon: '45.png', ctext: 'Chance of Light Rain', owmIcon: '09n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_chancelightsnow', altIcon: '46.png', ctext: 'Possible Light Snow', owmIcon: '13n', stdIcon: 'nt_snow', luxpercent: 0 ],
[ ccode: 'nt_chancelightsnowbreezy', altIcon: '55.png', ctext: 'Possible Light Snow and Breezy', owmIcon: '13n', stdIcon: 'nt_snow', luxpercent: 0 ],
[ ccode: 'nt_chancerain', altIcon: '39.png', ctext: 'Chance of Rain', owmIcon: '09n', stdIcon: 'nt_chancerain', luxpercent: 0 ],
[ ccode: 'nt_chancesleet', altIcon: '46.png', ctext: 'Chance of Sleet', owmIcon: '13n', stdIcon: 'nt_chancesleet', luxpercent: 0 ],
[ ccode: 'nt_chancesnow', altIcon: '46.png', ctext: 'Chance of Snow', owmIcon: '13n', stdIcon: 'nt_chancesnow', luxpercent: 0 ],
[ ccode: 'nt_chancetstorms', altIcon: '47.png', ctext: 'Chance of Thunderstorms', owmIcon: '11n', stdIcon: 'nt_chancetstorms', luxpercent: 0 ],
[ ccode: 'nt_chancelightsnowwindy', altIcon: '55.png', ctext: 'Possible Light Snow and Windy', owmIcon: '13n', stdIcon: 'nt_chancesnow', luxpercent: 0 ],
[ ccode: 'nt_clear', altIcon: '31.png', ctext: 'Clear', owmIcon: '01n', stdIcon: 'nt_sunny', luxpercent: 0 ],
[ ccode: 'nt_cloudy', altIcon: '26.png', ctext: 'Overcast', owmIcon: '04n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
[ ccode: 'nt_drizzle', altIcon: '9.png', ctext: 'Drizzle', owmIcon: '09n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_flurries', altIcon: '13.png', ctext: 'Flurries', owmIcon: '13n', stdIcon: 'nt_flurries', luxpercent: 0 ],
[ ccode: 'nt_fog', altIcon: '22.png', ctext: 'Foggy', owmIcon: '50n', stdIcon: 'nt_fog', luxpercent: 0 ],
[ ccode: 'nt_heavyrain', altIcon: '12.png', ctext: 'Heavy Rain', owmIcon: '10n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_heavyrainbreezy', altIcon: '1.png', ctext: 'Heavy Rain and Breezy', owmIcon: '10n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_heavyrainwindy', altIcon: '1.png', ctext: 'Heavy Rain and Windy', owmIcon: '10n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_lightrain', altIcon: '11.png', ctext: 'Light Rain', owmIcon: '09n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_lightrainbreezy', altIcon: '11.png', ctext: 'Light Rain and Breezy', owmIcon: '09n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_lightrainwindy', altIcon: '11.png', ctext: 'Light Rain and Windy', owmIcon: '09n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_lightsleet', altIcon: '46.png', ctext: 'Sleet', owmIcon: '13n', stdIcon: 'nt_sleet', luxpercent: 0 ],
[ ccode: 'nt_lightsnow', altIcon: '14.png', ctext: 'Light Snow', owmIcon: '13n', stdIcon: 'nt_snow', luxpercent: 0 ],
[ ccode: 'nt_mostlycloudy', altIcon: '27.png', ctext: 'Mostly Cloudy', owmIcon: '04n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
[ ccode: 'nt_partlycloudy', altIcon: '29.png', ctext: 'Partly Cloudy', owmIcon: '03n', stdIcon: 'nt_partlycloudy', luxpercent: 0 ],
[ ccode: 'nt_rain', altIcon: '11.png', ctext: 'Rain', owmIcon: '10n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_rainbreezy', altIcon: '2.png', ctext: 'Rain and Breezy', owmIcon: '10n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_rainwindy', altIcon: '2.png', ctext: 'Rain and Windy', owmIcon: '10n', stdIcon: 'nt_rain', luxpercent: 0 ],
[ ccode: 'nt_sleet', altIcon: '46.png', ctext: 'Sleet', owmIcon: '13n', stdIcon: 'nt_sleet', luxpercent: 0 ],
[ ccode: 'nt_snow', altIcon: '46.png', ctext: 'Snow', owmIcon: '13n', stdIcon: 'nt_snow', luxpercent: 0 ],
[ ccode: 'nt_thunderstorm', altIcon: '0.png', ctext: 'Thunderstorm', owmIcon: '11n', stdIcon: 'nt_tstorms', luxpercent: 0 ],
[ ccode: 'nt_wind', altIcon: '23.png', ctext: 'Windy', owmIcon: '50n', stdIcon: 'nt_tstorms', luxpercent: 0 ],
[ ccode: 'nt_windfoggy', altIcon: '48.png', ctext: 'Windy and Foggy', owmIcon: '50n', stdIcon: 'nt_fog', luxpercent: 0 ],
[ ccode: 'nt_windmostlycloudy', altIcon: '50.png', ctext: 'Windy and Mostly Cloudy', owmIcon: '50n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
[ ccode: 'nt_windovercast', altIcon: '49.png', ctext: 'Windy and Overcast', owmIcon: '50n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
[ ccode: 'nt_windpartlycloudy', altIcon: '52.png', ctext: 'Windy and Partly Cloudy', owmIcon: '50n', stdIcon: 'nt_cloudy', luxpercent: 0 ],
]


/******************************************************************************/
/***																		***/
/*** END OF CODE															***/
/***																		***/
/******************************************************************************/
