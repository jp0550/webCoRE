/*
 *  webCoRE - Community's own Rule Engine - Web Edition
 *
 *  Copyright 2016 Adrian Caramaliu <ady624("at" sign goes here)gmail.com>
 *
 *  webCoRE Piston
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
 * Last update March 31, 2020 for Hubitat
*/
private static String version(){ return 'v0.3.110.20191009' }
private static String HEversion(){ return 'v0.3.110.20200210_HE' }

/** webCoRE DEFINITION					**/

private static String handle(){ return 'webCoRE' }

import groovy.json.*
import hubitat.helper.RMUtils
import groovy.transform.Field

definition(
	name:handle()+' Piston',
	namespace:'ady624',
	author:'Adrian Caramaliu',
	description:'Do not install this directly, use webCoRE instead',
	category:'Convenience',
	parent:'ady624:'+handle(),
	iconUrl:'https://raw.githubusercontent.com/ady624/webCoRE/master/resources/icons/app-CoRE.png',
	iconX2Url:'https://raw.githubusercontent.com/ady624/webCoRE/master/resources/icons/app-CoRE@2x.png',
	iconX3Url:'https://raw.githubusercontent.com/ady624/webCoRE/master/resources/icons/app-CoRE@3x.png',
	importUrl:'https://raw.githubusercontent.com/imnotbob/webCoRE/hubitat-patches/smartapps/ady624/webcore-piston.src/webcore-piston.groovy'
)

preferences{
	page(name:'pageMain')
	page(name:'pageRun')
	page(name:'pageClear')
	page(name:'pageClearAll')
	page(name:'pageDumpPiston')
}

private static Boolean eric(){ return false }

/** CONFIGURATION PAGES				**/

def pageMain(){
	return dynamicPage(name:'pageMain', title:'', install:true, uninstall: !!state.build){
		if(parent==null || !(Boolean)parent.isInstalled()){
			section(){
				paragraph 'Sorry you cannot install a piston directly from the HE console; please use the webCoRE dashboard (dashboard.webcore.co) instead.'
			}
			section(sectionTitleStr('Installing webCoRE')){
				paragraph 'If you are trying to install webCoRE please go back one step and choose webCoRE, not webCoRE Piston. You can also visit wiki.webcore.co for more information on how to install and use webCoRE'
				if(parent!=null){
					String t0=(String)parent.getWikiUrl()
					href '', title:imgTitle('https://raw.githubusercontent.com/ady624/webCoRE/master/resources/icons/app-CoRE.png', inputTitleStr('More information')), description:t0, style:'external', url:t0, required:false
				}
			}
		}else{
			section(sectionTitleStr('General')){
				label name:'name', title:'Name', required:true, state:(name ? 'complete':(String)null), defaultValue:(String)parent.generatePistonName(), submitOnChange:true
			}

			section(sectionTitleStr('Dashboard')){
				String dashboardUrl=(String)parent.getDashboardUrl()
				if(dashboardUrl!=(String)null){
					dashboardUrl=dashboardUrl+'piston/'+hashId(app.id)
					href '', title:imgTitle('https://raw.githubusercontent.com/ady624/webCoRE/master/resources/icons/dashboard.png', inputTitleStr('View piston in dashboard')), style:'external', url:dashboardUrl, required:false
				}else paragraph 'Sorry your webCoRE dashboard does not seem to be enabled; please go to the parent app and enable the dashboard.'
			}

			section(sectionTitleStr('Application Info')){
				Map rtD=getTemporaryRunTimeData(now())
				if((Boolean)rtD.disabled) paragraph 'Piston is disabled by webCoRE'
				if(!(Boolean)rtD.active) paragraph 'Piston is paused'
				if(rtD.bin!=null){
					paragraph 'Automatic backup bin code: '+(String)rtD.bin
				}
				paragraph 'Version: '+version()
				paragraph 'VersionH: '+HEversion()
				paragraph 'Memory Usage: '+mem()
				paragraph 'RunTime History: '+runTimeHis(rtD)

			}

			section(sectionTitleStr('Recovery')){
				href 'pageRun', title:'Force-run this piston'
				href 'pageClear', title:'Clear logs', description:'This will remove all logs but no variables'
				href 'pageClearAll', title:'Clear all data', description:'This will remove all data stored in local variables'
			}

			section(){
				input 'dev', "capability.*", title:'Devices', description:'Piston devices', multiple:true
				input 'logging', 'enum', title:'Logging Level', options:[0:"None", 1:"Minimal", 2:"Medium", 3:"Full"], description:'Piston logging', defaultValue:state.logging?:0
				input 'logsToHE', 'bool', title:'Piston logs are also displayed in HE console logs?', description:"Logs are available in webCoRE console; also display in HE console 'Logs'?", defaultValue:false
				input 'maxStats', 'number', title:'Max number of timing history stats', description:'Max number of stats', range: '25..300', defaultValue:100
				input 'maxLogs', 'number', title:'Max number of history logs', description:'Max number of logs', range: '25..300', defaultValue:100
			}
			if(eric()){
				section('Debug'){
					href 'pageDumpPiston', title:'Dump piston structure', description:''
				}
			}
		}
	}
}

def pageRun(){
	test()
	return dynamicPage(name:'pageRun', title:'', uninstall:false){
		section('Run'){
			paragraph 'Piston tested'
			Map t0=(Map)parent.getWCendpoints()
			String t1="/execute/${hashId(app.id)}?access_token=${t0.at}".toString()
			paragraph "Cloud Execute endpoint ${t0.ep}${t1}".toString()
			paragraph "Local Execute endpoint ${t0.epl}${t1}".toString()
		}
	}
}

private static String sectionTitleStr(String title)	{ return '<h3>'+title+'</h3>' }
private static String inputTitleStr(String title)	{ return '<u>'+title+'</u>' }
private static String pageTitleStr(String title)	{ return '<h1>'+title+'</h1>' }
private static String paraTitleStr(String title)	{ return '<b>'+title+'</b>' }

private static String imgTitle(String imgSrc, String titleStr, String color=(String)null, Integer imgWidth=30, Integer imgHeight=0){
	String imgStyle=''
	imgStyle += imgWidth>0 ? 'width: '+imgWidth.toString()+'}px !important;':''
	imgStyle += imgHeight>0 ? imgWidth!=0 ? ' ':''+'height: '+imgHeight.toString()+'px !important;':''
	if(color!=(String)null){ return """<div style="color: ${color}; font-weight: bold;"><img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img></div>""" }
	else{ return """<img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img>""" }
}

def pageClear(){
	clear1()
	return dynamicPage(name:'pageClear', title:'', uninstall:false){
		section('Clear'){
			paragraph 'All non-essential data has been cleared.'
		}
	}
}

private void clear1(Boolean most=false, Boolean all=false){
	String meth='clear1'
	state.logs=[]
	if(most){
		state.stats=[:]
		state.trace=[:]
	}
	if(all){
		state.cache=[:]
		state.vars=[:]
		state.store=[:]
		state.pauses=0L
		clearMyPiston(meth)
		Map tRtData=getTemporaryRunTimeData(now())
		Boolean act=(Boolean)tRtData.active
		Boolean dis=(Boolean)tRtData.disabled
		if(act && !dis) Map rtD=getRunTimeData(tRtData, null, true, true) //reinitializes cache variables; caches piston
	}

	String appStr=(app.id).toString()
	String tsemaphoreName='sph'+appStr
	if(theSempahoresFLD!=null)theSemaphoresFLD[tsemaphoreName]=0L
	String queueName='aevQ'+appStr
	if(theQueuesFLD!=null)theQueuesFLD[queueName]=[]

	cleanState()
	clearMyCache(meth)
}

def pageClearAll(){
	clear1(true, true)
	return dynamicPage(name:'pageClearAll', title:'', uninstall:false){
		section('Clear All'){
			paragraph 'All local data has been cleared.'
		}
	}
}

private String dumpListDesc(data, Integer level, List lastLevel, String listLabel, Boolean html=false){
	String str=''
	Integer cnt=1
	List newLevel=lastLevel

	List list1=data?.collect{it}
	list1?.each{ par ->
		Integer t0=cnt-1
		String myStr="${listLabel}[${t0}]".toString()
		if(par instanceof Map){
			Map newmap=[:]
			newmap[myStr]=par
			Boolean t1= cnt==(Integer)list1.size()
			newLevel[level]=t1
			str += dumpMapDesc(newmap, level, newLevel, !t1)
		}else if(par instanceof List || par instanceof ArrayList){
			Map newmap=[:]
			newmap[myStr]=par
			Boolean t1= cnt==(Integer)list1.size()
			newLevel[level]=t1
			str += dumpMapDesc(newmap, level, newLevel, !t1)
		}else{
			String lineStrt='\n'
			for(Integer i=0; i<level; i++){
				lineStrt += (i+1<level)? (!lastLevel[i] ? '     │':'      '):'      '
			}
			lineStrt += (cnt==1 && (Integer)list1.size()>1)? '┌─ ':(cnt<(Integer)list1?.size()? '├─ ':'└─ ')
			if(html)str += '<span>'
			str += "${lineStrt}${listLabel}[${t0}]: ${par} (${getObjType(par)})".toString()
			if(html)str += '</span>'
		}
		cnt=cnt+1
	}
	return str
}

private String dumpMapDesc(data, Integer level, List lastLevel, Boolean listCall=false, Boolean html=false){
	String str=''
	Integer cnt=1
	data?.each{ par ->
		String lineStrt=''
		List newLevel=lastLevel
		Boolean thisIsLast= cnt==(Integer)data?.size() && !listCall
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
				lineStrt += (i+1<level)? (!newLevel[i] ? '     │':'      '):'      '
			}
			lineStrt += ((cnt<(Integer)data?.size()|| listCall) && !thisIsLast) ? '├─ ':'└─ '
		}
		String objType=getObjType(par.value)
		if(par.value instanceof Map){
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: (${objType})".toString()
			if(html)str += '</span>'
			newLevel[(level+1)]=theLast
			str += dumpMapDesc(par.value, level+1, newLevel, false, html)
		}
		else if(par.value instanceof List || par.value instanceof ArrayList){
			if(html)str += '<span>'
			str += "${lineStrt}${(String)par.key}: [${objType}]".toString()
			if(html)str += '</span>'
			newLevel[(level+1)]=theLast
			str += dumpListDesc(par.value, level+1, newLevel, '', html)
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

private static String myObj(obj){
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

private static String getObjType(obj){
	return "<span style='color:orange'>"+myObj(obj)+"</span>"
}

private String getMapDescStr(data){
	String str=''
	List lastLevel=[true]
	str=dumpMapDesc(data, 0, lastLevel, false, true)
	return str!='' ? str:'No Data was returned'
}

def pageDumpPiston(){
	Map rtD=getRunTimeData(null, null, false, false)
	String message=getMapDescStr(rtD.piston)
	return dynamicPage(name:'pageDumpPiston', title:'', uninstall:false){
		section('Piston dump'){
			paragraph message
		}
	}
}

void installed(){
	if(app.id==null)return
	state.created=now()
	state.modified=now()
	state.build=0
	state.vars=state.vars ?: [:]
	state.subscriptions=state.subscriptions ?: [:]
	state.logging=0
	initialize()
}

void updated(){
	unsubscribe()
	initialize()
}

void uninstalled(){
	Map a=deletePiston()
}

void initialize(){
	Integer tt1=settings.logging
	Integer tt2=state.logging
	if(tt1==null)Map a=setLoggingLevel(tt2 ? tt2.toString():'0')
	else if(tt1!=tt2)Map a=setLoggingLevel(tt1.toString())
	cleanState()
	clearMyCache('initialize')
	if((Boolean)state.active) Map b=resume()
}

private void cleanState(){
//cleanups between releases
	for(sph in state.findAll{ ((String)it.key).startsWith('sph')}) state.remove(sph.key.toString())
	List data=['hash', 'piston', 'cVersion', 'hVersion', 'disabled', 'logPExec', 'settings', 'svSunT', 'temp', 'debugLevel']
	for(String foo in data) state.remove(foo)
}

/** PUBLIC METHODS					**/

public Boolean isInstalled(){
	return !!state.created
}

public Map get(Boolean minimal=false){ // minimal is backup
	Map rtD=getRunTimeData()
	return [
		meta: [
			id: (String)rtD.id,
			author: (String)rtD.author,
			name: (String)rtD.name,
			created: (Long)rtD.created,
			modified: (Long)rtD.modified,
			build: (Integer)rtD.build,
			bin: (String)rtD.bin,
			active: (Boolean)rtD.active,
			category: rtD.category
		],
		piston: rtD.piston
	]+(minimal ? [:]:[ // use state as getRunTimeData re-initializes these
		systemVars: getSystemVariablesAndValues(rtD),
		subscriptions: state.subscriptions,
		state: state.state,
		logging: state.logging!=null ? (Integer)state.logging:0,
		stats: state.stats,
		logs: state.logs,
		trace: state.trace,
		localVars: state.vars,
		memory: mem(),
		lastExecuted: state.lastExecuted,
		nextSchedule: state.nextSchedule,
		schedules: state.schedules
	])
}

public Map activity(lastLogTimestamp){
	Map t0=getCachedMaps()
	if(t0==null)return [:]
	List logs=[]+(List)t0.logs
	Integer lsz=(Integer)logs.size()
	Long llt=lastLogTimestamp!=null && lastLogTimestamp instanceof String && ((String)lastLogTimestamp).isLong()? (Long)((String)lastLogTimestamp).toLong():0L
	Integer index=(llt!=0L && lsz>0)? logs.findIndexOf{ it?.t==llt }:0
	index=index>0 ? index:(llt!=0L ? 0:lsz)
	return [
		name: (String)t0.name,
		state: (Map)t0.state,
		logs: index>0 ? logs[0..index-1]:[],
		trace: (Map)t0.trace,
		localVars: (Map)t0.vars, // not reporting global or system variable changes
		memory: (String)t0.mem,
		lastExecuted: t0.lastExecuted,
		nextSchedule: t0.nextSchedule,
		schedules: (List)t0.schedules,
		systemVars: t0.cachePersist
	]
}

public Map clearLogs(){
	clear1()
	return [:]
}

private String decodeEmoji(String value){
//	if(value==null)return ''
	return value.replaceAll(/(\:%[0-9A-F]{2}%[0-9A-F]{2}%[0-9A-F]{2}%[0-9A-F]{2}\:)/,{ m -> URLDecoder.decode(m[0].substring(1, 13), 'UTF-8')})
}

@Field static Map thePistonCacheFLD

private void clearMyPiston (String meth=(String)null){
	String pisName=(app.id).toString()
	if((Integer)pisName.length()==0)return
	if(thePistonCacheFLD!=null){
		Map pData=thePistonCacheFLD[pisName]
		if(pData!=null){
			LinkedHashMap t0=(LinkedHashMap)thePistonCacheFLD[pisName].pis
			if(t0){
				List data=t0.collect{ it.key }
				for(item in data)t0.remove((String)item)
				thePistonCacheFLD[pisName].pis=null
				if(eric())log.debug 'clearing piston-code-cache '+meth
			}
		}
	}
}

private LinkedHashMap recreatePiston(Boolean shorten=false, Boolean useCache=true){
	if(shorten && useCache){
		if(thePistonCacheFLD==null)thePistonCacheFLD=[:]
		String pisName=(app.id).toString()
		Map pData=thePistonCacheFLD[pisName]
		if(pData==null || pData.cnt==null){
			pData=[cnt:0, pis:null]
			thePistonCacheFLD[pisName]=[:]+pData
		}
		Integer myCnt=(Integer)pData.cnt+1
		thePistonCacheFLD[pisName].cnt=myCnt
		if(pData.pis!=null) return [cached:true]+(LinkedHashMap)pData.pis
	}

	String sdata=''
	Integer i=0
	while(true){
		String s=(String)settings?."chunk:$i"
		if(s!=null) sdata += s
		else break
		i++
	}
	if(sdata!=''){
		def data=(LinkedHashMap)new groovy.json.JsonSlurper().parseText(decodeEmoji(new String(sdata.decodeBase64(), 'UTF-8')))
		LinkedHashMap piston=[
			o: data.o ?: [:],
			r: data.r ?: [],
			rn: !!data.rn,
			rop: data.rop ?: 'and',
			s: data.s ?: [],
			v: data.v ?: [],
			z: data.z ?: ''
		]
		Integer a=msetIds(shorten, piston)
		return piston
	}
	return [:]
}

public Map setup(LinkedHashMap data, chunks){
	if(data==null){
		log.error 'setup: no data'
		return [:]
	}
	state.modified=now()
	state.build=(Integer)(state.build!=null ? (Integer)state.build+1:1)
	LinkedHashMap piston=[
		o: data.o ?: [:],
		r: data.r ?: [],
		rn: !!data.rn,
		rop: data.rop ?: 'and',
		s: data.s ?: [],
		v: data.v ?: [],
		z: data.z ?: ''
	]
	String meth='setup'
	clearMyPiston(meth)
	clearMsetIds(piston)
	Integer a=msetIds(false, piston)

	for(chunk in settings.findAll{ ((String)it.key).startsWith('chunk:') && !chunks[(String)it.key] }){
		app.clearSetting((String)chunk.key)
	}
	for(chunk in chunks) app.updateSetting((String)chunk.key, [type:'text', value:chunk.value])
	app.updateSetting('bin', [type:'text', value:(String)state.bin ?: ''])
	app.updateSetting('author', [type:'text', value:(String)state.author ?: ''])

	state.pep=piston.o?.pep ? true:false

	if((String)data.n!=(String)null && (Integer)((String)data.n).length()>0){
		if(state.svLabel!=(String)null){
			String res=(String)state.svLabel
			app.updateLabel(res)
		}
		state.svLabel=(String)null
		app.updateLabel((String)data.n)
	}
	state.schedules=[]
	state.vars=state.vars ?: [:]
	state.modifiedVersion=version()

	state.cache=[:]
	clear1(true)

	Map rtD=[:]
	rtD.piston=piston
	if((Integer)state.build==1 || (Boolean)state.active) rtD=resume(piston)
	else checkLabel()
	return [active:(Boolean)state.active, build:(Integer)state.build, modified:(Long)state.modified, state:state.state, rtData:rtD]
}

private void clearMsetIds(node){
	if(item==null)return
	for (list in node.findAll{ it.value instanceof List }){
		for (item in list.value.findAll{ it instanceof Map }) clearMsetIds(item)
	}
	if(node instanceof Map) if(node['$']!=null)node.remove('$')

	for (item in node.findAll{ it.value instanceof Map })clearMsetIds(item)
}

private Integer msetIds(Boolean shorten, node, Integer maxId=0, Map existingIds=[:], List requiringIds=[], Integer level=0){
	String nodeT=node?.t
	if(nodeT in ['if', 'while', 'repeat', 'for', 'each', 'switch', 'action', 'every', 'condition', 'restriction', 'group', 'do', 'on', 'event', 'exit', 'break']){
		Integer id=node['$']!=null ? (Integer)node['$']:0
		if(id==0 || existingIds[id]!=null){
			Boolean a=requiringIds.push(node)
		}else{
			maxId=maxId<id ? id:maxId
			existingIds[id]=id
		}
		if(nodeT=='if' && node.ei){
			Boolean a=node.ei.removeAll{ !it.c && !it.s }
			for (elseIf in node.ei){
				id=elseIf['$']!=null ? (Integer)elseIf['$']:0
				if(id==0 || existingIds[id]!=null){
					Boolean aa=requiringIds.push(elseIf)
				}else{
					maxId=(maxId<id)? id:maxId
					existingIds[id]=id
				}
			}
		}
		if(nodeT=='switch' && node.cs){
			for (Map _case in (List)node.cs){
				id=_case['$']!=null ? (Integer)_case['$']:0
				if(id==0 || existingIds[id]!=null) Boolean a=requiringIds.push(_case)
				else{
					maxId=(maxId<id)? id:maxId
					existingIds[id]=id
				}
			}
		}
		if(nodeT=='action' && node.k){
			for (Map task in (List)node.k){
				id=task['$']!=null ? (Integer)task['$']:0
				if(id==0 || existingIds[id]!=null) Boolean a=requiringIds.push(task)
				else{
					maxId=(maxId<id)? id:maxId
					existingIds[id]=id
				}
			}
		}
	}
	for (list in node.findAll{ it.value instanceof List }){
		for (item in list.value.findAll{ it instanceof Map }) maxId=msetIds(shorten, item, maxId, existingIds, requiringIds, level+1)
	}
	if(level==0){
		for (item in requiringIds){
			maxId += 1
			item['$']=maxId
		}
		if(shorten)cleanCode(node)
	}
	return maxId
}

private void cleanCode(item){
	if(item==null || !(item instanceof Map))return

	if(item.str!=null)item.remove('str')
	if(item.ok!=null)item.remove('ok')
	if(item.z!=null)item.remove('z')
	if(item.zc!=null)item.remove('zc')
	if(item.e!=null && item.e instanceof String)item.remove('e')

	if(item.v!=null)cleanCode(item.v)
	if(item.exp!=null)cleanCode(item.exp)
	if(item.lo!=null)cleanCode(item.lo)
	if(item.lo2!=null)cleanCode(item.lo2)
	if(item.lo3!=null)cleanCode(item.lo3)
	if(item.ro!=null)cleanCode(item.ro)
	if(item.ro2!=null)cleanCode(item.ro2)
	if(item.to!=null)cleanCode(item.to)
	if(item.to2!=null)cleanCode(item.to2)

	for (list in item.findAll{ it.value instanceof List }){
		for (itemA in list.value.findAll{ it instanceof Map }) cleanCode(itemA)
	}
}

public Map deletePiston(){
	String meth='deletePiston'
	if(eric())log.debug meth
	state.active=false
	clear1(true, true)	// calls clearMyCache(meth)
	clearParentCache(meth)
	clearMyPiston(meth)
	return [:]
}

public void settingsToState(String myKey, setval){
	String meth='setting to state '+myKey
	if(eric())log.debug meth
	if(setval) atomicState[myKey.toString()]=setval
	else state.remove([myKey.toString()])
	clearParentCache(meth)
	clearMyCache(meth)
	clearMyPiston(meth)
	runIn(5, 'checkLabel')
}

private void checkLabel(Map rtD=null){
	if(rtD==null)rtD=getTemporaryRunTimeData(now())
	Boolean act=(Boolean)rtD.active
	Boolean dis=(Boolean)rtD.disabled
	String appLbl=(String)app.label
	Boolean found=match(appLbl, '<span')
	String meth='checkLabel'
	String savedLabel=(String)state.svLabel
	if((act && !dis) || (!found && savedLabel!=(String)null)){
		if(savedLabel!=(String)null){
			app.updateLabel(savedLabel)
			appLbl=savedLabel
			rtD.svLabel=state.svLabel=savedLabel=(String)null
			clearMyCache(meth)
		}
	}
	if(!act || dis){
		if(!found && savedLabel==(String)null){
			rtD.svLabel=state.svLabel=appLbl
			String tstr=''
			if(!act) tstr='(Paused)'
			if(dis) tstr='(Disabled) Kill switch is active'
			String res=appLbl+" <span style='color:orange'>"+tstr+"</span>"
			app.updateLabel(res)
			clearMyCache(meth)
		}
	}
}

public void config(Map data){ // creates a new piston
	if(data==null) return
	if((String)data.bin!=(String)null){
		state.bin=(String)data.bin
		app.updateSetting('bin', [type:'text', value:(String)state.bin])
	}
	if((String)data.author!=null){
		state.author=(String)data.author
		app.updateSetting('author', [type:'text', value:(String)state.author])
	}
	if((String)data.initialVersion!=null) state.initialVersion=(String)data.initialVersion
	clearMyCache('config')
}

public Map setBin(String bin){
	if(bin==(String)null || (String)state.bin!=null){
		log.error 'setBin: bad bin'
		return [:]
	}
	state.bin=bin
	app.updateSetting('bin', [type:'text', value:bin])
	String typ='setBin'
	clearParentCache(typ)
	clearMyCache(typ)
	return [:]
}

public Map pausePiston(){
	state.active=false
	clearMyCache('pauseP')

	Map rtD=getRunTimeData()
	Map msg=timer 'Piston successfully stopped', rtD, -1
	if((Integer)rtD.logging>0)info 'Stopping piston...', rtD, 0
	state.schedules=[]
	rtD.stats.nextSchedule=0L
	state.nextSchedule=0L
	unsubscribe()
	unschedule()
	state.trace=[:]
	state.subscriptions=[:]
	checkLabel(rtD)
	if((Integer)rtD.logging>0)info msg, rtD
	updateLogs(rtD)
	state.active=false
	state.remove('lastEvent')
	clear1(true, true)	// calls clearMyCache(meth)
	//app.removeSetting('dev')
	clearMyPiston('pauseP')
	return shortRtd(rtD)
}

public Map resume(LinkedHashMap piston=null){
	state.active=true
	state.subscriptions=[:]
	state.schedules=[]
	clearMyCache('resumeP')

	Map tempRtData=getTemporaryRunTimeData(now())
	Map msg=timer 'Piston successfully started', tempRtData, -1
	if(piston!=null)tempRtData.piston=piston
	Map rtD=getRunTimeData(tempRtData, null, true, false) //performs subscribeAll(rtD); reinitializes cache variables
	if((Integer)rtD.logging>0)info 'Starting piston... ('+HEversion()+')', rtD, 0
	checkVersion(rtD)
	checkLabel(rtD)
	if((Integer)rtD.logging>0)info msg, rtD
	updateLogs(rtD)
	Map nRtd=shortRtd(rtD)
	nRtd.result=[active:true, subscriptions:state.subscriptions]
	state.active=true
	clearMyCache('resumeP1')
	return nRtd
}

Map shortRtd(Map rtD){
	state.state=[:]+rtD.state
	def st=[:]+state.state
	st.remove('old')
	Map myRt=[
		id:(String)rtD.id,
		active:(Boolean)rtD.active,
		category:rtD.category,
		stats:[
			nextSchedule:(Long)rtD.stats.nextSchedule
		],
		piston:[
			z:(String)rtD.piston.z
		],
		state:st
	]
	return myRt
}

public Map setLoggingLevel(String level){
	Integer mlogging=level.isInteger()? level.toInteger():0
	mlogging=Math.min(Math.max(0,mlogging),3)
	app.updateSetting('logging', [type:'enum', value:mlogging])
	state.logging=mlogging
	if(mlogging==0)state.logs=[]
	cleanState()
	clearMyCache('setLoggingLevel')
	return [logging:mlogging]
}

public Map setCategory(String category){
	state.category=category
	cleanState()
	clearMyCache('setCategory')
	return [category:category]
}

public Map test(){
	handleEvents([date:new Date(), device:location, name:'test', value:now()])
	return [:]
}

public Map execute(data, source){
	handleEvents([date:new Date(), device:location, name:'execute', value:source!=null ? source : now(), jsonData:data], false)
	return [:]
}

public Map clickTile(index){
	handleEvents([date:new Date(), device:location, name:'tile', value:index])
	return state.state ?: [:]
}

private Map getCachedAtomicState(){
	Long atomStart=now()
	def atomState
	atomicState.loadState()
	atomState=atomicState.@backingMap
	if(settings.logging>2)log.debug "AtomicState generated in ${now() - atomStart}ms"
	return atomState
}

@Field static Map theQueuesFLD
@Field static Map theSemaphoresFLD

// This can a)lock semaphore, b)wait for semaphore, c)queue event, d)just fall through (no locking, waiting)
private Map lockOrQueueSemaphore(String semaphore, event, Boolean queue, Map rtD){
	Long r_semaphore=0L
	Long semaphoreDelay=0L
	String semaphoreName=(String)null
	String appStr=(app.id).toString()
	String tsemaphoreName='sph'+appStr
	String queueName='aevQ'+appStr
	Boolean waited=false
	Boolean didQ=false
	Long tt1=now()
	Long startTime=tt1
	if(theQueuesFLD==null)theQueuesFLD=[:]
	if(theSemaphoresFLD==null)theSemaphoresFLD=[:]
	if(semaphore!=(String)null){
		Long lastSemaphore
		while (true){
			Long t0=theSemaphoresFLD[tsemaphoreName]
			Long tt0=t0!=null ? t0 : 0L
			lastSemaphore=tt0
			if(lastSemaphore==0L || tt1-lastSemaphore>100000L){
				theSemaphoresFLD[tsemaphoreName]=tt1
				semaphoreName=tsemaphoreName
				semaphoreDelay=waited ? tt1-startTime:0L
				r_semaphore=tt1
				break
			}
			if(queue){
				if(event!=null){
					def myEvent=[
						t:(Long)event.date.getTime(),
						name:(String)event.name,
						value:event.value,
						descriptionText:(String)event.descriptionText,
						unit:event?.unit,
						physical:!!event.physical,
						jsonData:event?.jsonData,
					]+(event instanceof com.hubitat.hub.domain.Event ? [:]:[
						index:event?.index,
						recovery:event?.recovery,
						schedule:event?.schedule,
						contentType:(String)event?.contentType,
						responseData:event?.responseData,
						responseCode:event?.responseCode,
						setRtData:event?.setRtData
					])
					if(event.device!=null){
						myEvent.device=[id:event.device?.id, name:event.device?.name, label:event.device?.label]
						if(event.device?.hubs!=null){
							myEvent.device.hubs=[t:'tt']
						}
					}
					List evtQ=theQueuesFLD[queueName]
					evtQ=evtQ!=null ? evtQ:[]
					Boolean a=evtQ.push(myEvent)
					theQueuesFLD[queueName]=evtQ
					didQ=true

					Integer qsize=(Integer)evtQ.size()
					if(qsize>12){
						log.error "large queue size ${qsize} clearing"
						clear1()
					}
				}
				break
			}else{
				waited=true
				pauseExecution(500L)
				tt1=now()
			}
		}
	}
	return [
		semaphore: r_semaphore,
		semaphoreName: semaphoreName,
		semaphoreDelay: semaphoreDelay,
		waited: waited,
		exitOut: didQ
	]
}

private Map getTemporaryRunTimeData(Long startTime){
	Map rtD=[:]
	Map t0=getDSCache()
	Map t1=getParentCache()
	rtD=[:]+t0+t1
	rtD.temporary=true
	rtD.timestamp=startTime
	rtD.logs=[[t:startTime]]
	rtD.debugLevel=0
	rtD.eric=eric() && (Integer)rtD.logging>2
	return rtD
}

@Field static Map theCacheFLD // each piston has a map in here

private void clearMyCache(String meth=(String)null){
	String myId=hashId(app.id)
	if(!myId)return
	if(theCacheFLD!=null){
		Map t0=theCacheFLD[myId]
		if(t0){
			List data=t0.collect{ it.key }
			for(item in data)t0.remove((String)item)
			theCacheFLD[myId]=null
			if(eric())log.debug 'clearing piston data cache '+meth
		}
	}
}

private Map getCachedMaps(Boolean retry=true){
	Map result=null
	String myId=hashId(app.id)
	if(theCacheFLD!=null && theCacheFLD[myId]!=null){
		result=theCacheFLD[myId]
		if(result.cache instanceof Map && result.state instanceof Map)return (Map)theCacheFLD[myId]
	}
	if(retry){
		Map a=getDSCache()
		return getCachedMaps(false)
	}
	if(eric())log.warn 'cached map nf'
	return null
}

private Map getDSCache(){
	Map result=null
	String appId=hashId(app.id)
	String myId=appId
	if(myId.length()<8){
		log.error 'getDSCache: no id '+myId
		return [:]
	}
	if(theCacheFLD==null){
		Long rP=Math.round(1000.0D*Math.random())
		pauseExecution(rP)
		if(theCacheFLD==null){
			if(thePistonCacheFLD==null)thePistonCacheFLD=[:]
			if(eric())log.debug 'initializing theCacheFLD'
			theCacheFLD=[:]
			Map a=getParentCache()
		}
	}
	def tresult=theCacheFLD[myId]
	if(tresult!=null){ result=(Map)tresult; result.stateAccess=null }

	if(result==null){
		Long stateStart=now()
		if(state.pep==null){ // upgrades of older pistons
			LinkedHashMap piston=recreatePiston()
			state.pep=piston.o?.pep ? true:false
		}
		Map t1=[
			id: appId,
			logging: state.logging!=null ? (Integer)state.logging:0,
			svLabel: (String)state.svLabel,
			name: (String)state.svLabel!=(String)null ? (String)state.svLabel:(String)app.label,
			active: (Boolean)state.active,
			category: state.category ?: 0,
			pep: (Boolean)state.pep,
			created: (Long)state.created,
			modified: (Long)state.modified,
			build: (Integer)state.build,
			author: (String)state.author,
			bin: (String)state.bin,
			logsToHE: (Boolean)settings?.logsToHE ? true:false,
		]
//ERS trying to cache things used on every piston start, read by activity, or frequently updated with atomicState
		Long stateEnd=now()
		t1.stateAccess=stateEnd-stateStart
		t1.runTimeHis=[]
		def atomState=((Boolean)t1.pep)? getCachedAtomicState():state

		def t0=atomState.cache
		t1.cache=t0 ? (Map)t0:[:]
		t0=atomState.store
		t1.store=t0 ? (Map)t0:[:]

		t0=atomState.state
		t1.state=t0 ? (Map)t0:[:]
		t0=atomState.trace
		t1.trace=t0 ? (Map)t0:[:]
		t0=atomState.schedules
		t1.schedules=t0 ? (List)t0:[]
		t1.nextSchedule=atomState.nextSchedule
		t1.lastExecuted=atomState.lastExecuted
		t1.mem=mem()
		t0=atomState.logs
		t1.logs=t0 ? (List)t0:[]
		t0=atomState.vars
		t1.vars=t0 ? [:]+(Map)t0:[:]

		t1.devices= settings.dev && settings.dev instanceof List ? settings.dev.collectEntries{[(hashId(it.id)): it]} : [:]
		result=[:]+t1

		theCacheFLD[myId]=result
		if(eric())log.debug 'creating my piston cache'
	}
	return [:]+result
}

@Field static Map theParentCacheFLD

public void clearParentCache(String meth=(String)null){
	Map t0=theParentCacheFLD
	if(t0){
		List data=t0.collect{ it.key }
		for(item in data)t0.remove((String)item)
		theParentCacheFLD=null
		if(eric())log.debug "clearing parent cache $meth"
	}
}

private Map getParentCache(){
	Map result=null
	String mStr='gathering parent cache'
	if(theParentCacheFLD==null){
		//Long rP=Math.round(1000.0D*Math.random())
		//pauseExecution(rP)
		//if(theParentCacheFLD==null){
			if(eric())log.debug mStr
			Map t0=(Map)parent.getChildPstate()
			Map t1=[
				coreVersion: (String)t0.sCv,
				hcoreVersion: (String)t0.sHv,
				powerSource: (String)t0.powerSource,
				region: (String)t0.region,
				instanceId: (String)t0.instanceId,
				settings: t0.stsettings,
				enabled: !!t0.enabled,
				disabled: !t0.enabled,
				logPExec: !!t0.logPExec,
				locationId: (String)t0.locationId,
				oldLocationId: hashId(location.id+'L'), //backwards compatibility
				incidents: (List)t0.incidents
			]
			result=[:]+t1
			theParentCacheFLD=result
		//}
	}
	result=theParentCacheFLD
	if(result==null){
		log.error 'NO '+mStr
		result=[:]
	}
	return [:]+result
}

private Map getRunTimeData(Map rtD=null, Map retSt=null, Boolean fetchWrappers=false, Boolean shorten=false){
	Long timestamp=now()
	Long started=timestamp
	List logs=[]
	LinkedHashMap piston
	Long lstarted=0L
	Long lended=0L
	Integer dbgLevel=0
	if(rtD!=null){
		timestamp=(Long)rtD.timestamp
		logs=rtD.logs!=null ? (List)rtD.logs:[]
		piston=rtD.piston!=null ? (LinkedHashMap)rtD.piston:null
		lstarted=rtD.lstarted!=null ? (Long)rtD.lstarted:0L
		lended=rtD.lended!=null ? (Long)rtD.lended:0L
		dbgLevel=rtD.debugLevel!=null ? (Integer)rtD.debugLevel:0
	}else rtD=getTemporaryRunTimeData(timestamp)

	if(rtD.temporary!=null) rtD.remove('temporary')

	Map m1=[semaphore:0L, semaphoreName:(String)null, semaphoreDelay:0L, wAtSem:false]
	if(retSt!=null){
		m1.semaphore=(Long)retSt.semaphore
		m1.semaphoreName=(String)retSt.semaphoreName
		m1.semaphoreDelay=(Long)retSt.semaphoreDelay
		m1.wAtSem=(Long)retSt.semaphoreDelay>0L ? true:false
	}
	rtD=rtD+m1

	def mode=location.getCurrentMode()
	rtD.locationModeId=mode!=null ? hashId((Long)mode.getId()):null

	rtD.timestamp=timestamp
	rtD.lstarted=lstarted
	rtD.lended=lended
	rtD.logs=[]
	if(logs!=[] && (Integer)logs.size()>0) rtD.logs=(List)rtD.logs+logs
	else rtD.logs=[[t: timestamp]]
	rtD.debugLevel=dbgLevel

	rtD.trace=[t:timestamp, points:[:]]
	rtD.stats=[nextSchedule:0L]
	rtD.newCache=[:]
	rtD.schedules=[]
	rtD.cancelations=[statements:[], conditions:[], all:false]
	rtD.updateDevices=false
	rtD.systemVars=[:]+getSystemVariables

	Map atomState=getCachedMaps()
	atomState=atomState!=null?atomState:[:]
	Map st=atomState.state
	rtD.state=st!=null && st instanceof Map ? [:]+st : [old:'', new:'']
	rtD.state.old=(String)rtD.state.new

	rtD.pStart=now()
	Boolean doSubScribe=false
	if(piston==null){
		piston=recreatePiston(shorten)
		doSubScribe=!piston.cached
	}
	rtD.piston=piston
	getLocalVariables(rtD, piston.v, atomState)

	if(doSubScribe || fetchWrappers){
		subscribeAll(rtD, fetchWrappers)
		String pisName=(app.id).toString()
		if(thePistonCacheFLD==null)thePistonCacheFLD=[:]
		Map pData=thePistonCacheFLD[pisName]
		if(shorten && pisName!='' && pData!=null && pData.pis==null){
			pData.pis=[:]+(LinkedHashMap)rtD.piston

			thePistonCacheFLD[pisName]=[:]+pData
			if(eric()){
				log.debug 'creating piston-code-cache'
				Map pL=[:]+thePistonCacheFLD
				Integer t0=(Integer)pL.size()
				Integer t1=(Integer)"${pL}".size()
				String mStr=" piston plist is ${t0} elements, and ${t1} bytes".toString()
				log.debug "saving"+mStr
				if(t1>40000000){
					thePistonCacheFLD=[:]
					log.warn "clearing"+mStr
				}
			}
		}
	}
	Long t0=now()
	rtD.pEnd=t0
	rtD.ended=t0
	rtD.generatedIn=t0-started
	return rtD
}

private void checkVersion(Map rtD){
	String ver=HEversion()
	String t0=(String)rtD.hcoreVersion
	if(ver!=t0){
		String tt0="child app's version($ver)".toString()
		String tt1="parent app's version($t0)".toString()
		String tt2=' is newer than the '
		String msg
		if(ver>t0) msg=tt0+tt2+tt1
		else msg=tt1+tt2+tt0
		warn "WARNING: Results may be unreliable because the "+msg+". Please consider updating both apps to the same version.", rtD
	}
	if(location.timeZone==null){
		error 'Your location is not setup correctly - timezone information is missing. Please select your location by placing the pin and radius on the map, then tap Save, and then tap Done. You may encounter error or incorrect timing until this is fixed.', rtD
	}
}

/** EVENT HANDLING								**/

void deviceHandler(event){
	handleEvents(event)
}

void timeHandler(event){
	timeHelper(event, false)
}

void timeHelper(event, Boolean recovery){
	handleEvents([date:new Date((Long)event.t), device:location, name:'time', value:(Long)event.t, schedule:event, recovery:recovery], !recovery)
}

void executeHandler(event){
	pauseExecution(150L)
	handleEvents([date:event.date, device:location, name:'execute', value:event.value, jsonData:event.jsonData])
}

@Field final Map getPistonLimits=[
	schedule: 3000L, // need this or longer remaining execution time to process schedules
	scheduleVariance: 970L,
	executionTime: 40000L, // time we stop this execution
	slTime: 1300L, // time before we start pausing
	useBigDelay: 10000L, // transition from short delay to Long delay
	taskShortDelay: 150L,
	taskLongDelay: 500L,
	taskMaxDelay: 1000L,
	maxStats: 100,
	maxLogs: 100,
]

void handleEvents(event, Boolean queue=true, Boolean callMySelf=false, LinkedHashMap pist=null){
	Long startTime=now()
	Map tempRtData=getTemporaryRunTimeData(startTime)
	Map msg=timer 'Event processed successfully', tempRtData, -1
	String evntName=(String)event.name
	String evntVal="${event.value}".toString()
	Long eventDelay=Math.round(1.0D*startTime-(Long)event.date.getTime())
	if((Integer)tempRtData.logging!=0){
		String devStr="${event?.device?.label ?: event?.device?.name ?: location}".toString()
		String recStr=evntName=='time' && (Boolean)event.recovery ? '/recovery':''
		String valStr=evntVal+(evntName=='hsmAlert' && evntVal=='rule' ? ', '+(String)event.descriptionText:'')
		String mymsg='Received event ['+devStr+'].'+evntName+recStr+' = '+valStr+" with a delay of ${eventDelay}ms, canQueue: ${queue}, calledMyself: ${callMySelf}".toString()
		info mymsg, tempRtData, 0
	}

	Boolean act=(Boolean)tempRtData.active
	Boolean dis=(Boolean)tempRtData.disabled
	if(!act || dis){
		String tstr=' active, aborting piston execution.'
		if(!act){ // this is pause/resume piston
			msg.m='Piston is not'+tstr+' (Paused)'
		}
		if(dis) msg.m='Kill switch is'+tstr

		checkLabel(tempRtData)
		if((Integer)tempRtData.logging!=0)info msg, tempRtData
		updateLogs(tempRtData, startTime)
		return
	}

	Boolean myPep=(Boolean)tempRtData.pep
	tempRtData.piston=pist
	String appId=(String)tempRtData.id
	Boolean serializationOn=true // on / off switch
	Boolean strictSync=true // this could be a setting
	Boolean doSerialization=!myPep && (serializationOn || strictSync)
	String st0=doSerialization ? appId:(String)null

	tempRtData.lstarted=now()
	Map retSt=[ semaphore:0L, semaphoreName:(String)null, semaphoreDelay:0L, wAtSem:false ]
	if(st0!=(String)null && !callMySelf){
		retSt=lockOrQueueSemaphore(st0, event, queue, tempRtData)
		if((Boolean)retSt.exitOut){
			msg.m='Event queued'
			if((Integer)tempRtData.logging!=0)info msg, tempRtData
			updateLogs(tempRtData, startTime)
			return
		}
		if((Boolean)retSt.wAtSem) warn 'Piston waited at a semaphore for '+(Long)retSt.semaphoreDelay+'ms', tempRtData
	}
	tempRtData.lended=now()

//measure how Long first state access takes
	Long stAccess
	if(tempRtData.stateAccess==null){
		Long stStart=now()
		Long b=(Long)state.nextSchedule
		def a=state.schedules
		Map pEvt=state.lastEvent
		Long stEnd=now()
		stAccess=stEnd-stStart
	}else stAccess=(Long)tempRtData.stateAccess

	tempRtData.cachePersist=null
	Map rtD=getRunTimeData(tempRtData, retSt, false, true)
	checkVersion(rtD)

	Long theend=now()
	Long t0=theend-startTime
	Long t1=(Long)rtD.lended-(Long)rtD.lstarted
	Long t2=(Long)rtD.generatedIn
	Long t3=(Long)rtD.pEnd-(Long)rtD.pStart
	Long missing=t0-t1-t2
	Long t4=(Long)rtD.lended-startTime
	Long t5=theend-(Long)rtD.lended
	if((Integer)rtD.logging>1){
		if((Integer)rtD.logging>2)debug "RunTime initialize > ${t0} LockT > ${t1}ms > rtDT > ${t2}ms > pistonT > ${t3}ms (first state access ${missing} $t4 $t5)".toString(), rtD
		String adMsg=''
		if(eric())adMsg=" (Init: $t0, Lock: $t1, pistonT $t3 first state access $missing ($t4 $t5) $stAccess".toString()
		if((Integer)rtD.logging>1)trace "Runtime (${(Integer)"$rtD".size()} bytes) successfully initialized in ${t2}ms (${HEversion()})".toString()+adMsg, rtD
	}
	rtD.curStat=[i:t0, l:t1, r:t2, p:t3, s:stAccess]

	rtD.temp=[randoms:[:]] // equivalent of resetRandomValues()
	rtD.tPause=0L
	rtD.stats.timing=[t:startTime, d:eventDelay>0L ? eventDelay:0L, l:Math.round(1.0D*now()-startTime)]

	startTime=now()
	Map msg2=timer "Execution stage complete.", rtD, -1
	Boolean success=true
	Boolean firstTime=true
	if(evntName!='time' && evntName!='wc_async_reply'){
		if((Integer)rtD.logging>0)info "Execution stage started", rtD, 1
		success=executeEvent(rtD, event)
		firstTime=false
	}

	Boolean syncTime=true
	String myId=(String)rtD.id
	while (success && (Long)getPistonLimits.executionTime+(Long)rtD.timestamp-now()>(Long)getPistonLimits.schedule){
		List schedules
		Map tt0=getCachedMaps()
		if(tt0!=null)schedules=[]+(List)tt0.schedules
		else schedules=myPep ? (List)atomicState.schedules:(List)state.schedules
		if(schedules==null || schedules==[] || (Integer)schedules.size()==0)break
		Long t=now()
		if(evntName=='wc_async_reply'){
			event.schedule=schedules.sort{ (Long)it.t }.find{ (String)it.d==evntVal }
			syncTime=false
		}else{
			//anything less than .9 seconds in the future is considered due, we'll do some pause to sync with it
			//we're doing this because many times, the scheduler will run a job early, usually 0-1.5 seconds early...
			evntName='time'
			evntVal=t.toString()
			event=[date:event.date, device:location, name:evntName, value:t, schedule:schedules.sort{ (Long)it.t }.find{ (Long)it.t<t+(Long)getPistonLimits.scheduleVariance }]
		}
		if(event.schedule==null) break
		schedules.remove(event.schedule)

		if(tt0!=null)theCacheFLD[myId].schedules=schedules
		if(myPep)atomicState.schedules=schedules
		else state.schedules=schedules

		if(evntName=='wc_async_reply'){
			Integer responseCode=(Integer)event.responseCode
			Boolean statOk=responseCode>=200 && responseCode<=299
			String eMsg
			switch(evntVal){
			case 'httpRequest':
				if(event.schedule.stack!=null){
					event.schedule.stack.response=event.responseData
					event.schedule.stack.json=event.jsonData
				}
				setSystemVariableValue(rtD, '$httpContentType', (String)event.contentType)
			case 'storeMedia':
				if(event.setRtData){
					for(item in event.setRtData){
						rtD[(String)item.key]=item.value
					}
				}
				setSystemVariableValue(rtD, '$httpStatusCode', responseCode)
				setSystemVariableValue(rtD, '$httpStatusOk', statOk)
				break
			case 'iftttMaker':
				setSystemVariableValue(rtD, '$iftttStatusCode', responseCode)
				setSystemVariableValue(rtD, '$iftttStatusOk', statOk)
				break
			case 'sendEmail':
				break
			default:
				eMsg="unknown "
				error eMsg+"async event "+evntVal, rtD
			}
			evntName='time'
			event.name=evntName
			event.value=t
			evntVal=t.toString()
		}else{
			Integer responseCode=408
			Boolean statOk=false
			String eMsg
			String ttyp=(String)event.schedule.d
			Boolean found=false
			switch(ttyp){
			case 'httpRequest':
				setSystemVariableValue(rtD, '$httpContentType', '')
				if(event.schedule.stack!=null) event.schedule.stack.response=null
			case 'storeMedia':
				setSystemVariableValue(rtD, '$httpStatusCode', responseCode)
				setSystemVariableValue(rtD, '$httpStatusOk', statOk)
				found=true
				break
			case 'sendEmail':
				found=true
				break
			case 'iftttMaker':
				setSystemVariableValue(rtD, '$iftttStatusCode', responseCode)
				setSystemVariableValue(rtD, '$iftttStatusOk', statOk)
				found=true
				break
			}
			if(found){
				error eMsg+"Timeout Error "+ttyp, rtD
				syncTime=true
			}
		}
		//if we have any other pending -3 events (device schedules), we cancel them all
		//if(event.schedule.i>0)schedules.removeAll{ (it.s==event.schedule.s) && (it.i==-3)}
		if(syncTime && strictSync){
			Long delay=Math.round((Long)event.schedule.t-1.0D*now())
			if(delay>0L && delay<(Long)getPistonLimits.scheduleVariance){
				if((Integer)rtD.logging>1)trace "Synchronizing scheduled event, waiting for ${delay}ms".toString(), rtD
				pauseExecution(delay)
			}
		}
		if(firstTime){
			msg2=timer "Execution stage complete.", rtD, -1
			if((Integer)rtD.logging>0)info "Execution stage started", rtD, 1
		}
		success=executeEvent(rtD, event)
		syncTime=true
		firstTime=false
	}

	rtD.stats.timing.e=Math.round(1.0D*now()-startTime)
	if((Integer)rtD.logging>0)info msg2, rtD
	if(!success)msg.m='Event processing failed'
	if(eric())msg.m=(String)msg.m+' Total Pauses ms: '+((Long)rtD.tPause).toString()
	finalizeEvent(rtD, msg, success)

	if((Boolean)rtD.logPExec && rtD.currentEvent!=null){
		String desc='webCore piston \''+(String)app.label+'\' was executed'
		sendLocationEvent(name:'webCoRE', value:'pistonExecuted', isStateChange:true, displayed:false, linkText:desc, descriptionText:desc, data:[
			id:appId,
			name:(String)app.label,
			event:[date:new Date((Long)rtD.currentEvent.date), delay:(Long)rtD.currentEvent.delay, duration:now()-(Long)rtD.currentEvent.date, device:"${rtD.event.device}".toString(), name:(String)rtD.currentEvent.name, value:rtD.currentEvent.value, physical:(Boolean)rtD.currentEvent.physical, index:rtD.currentEvent.index],
			state:[old:(String)rtD.state.old, new:(String)rtD.state.new]
		])
	}

// any queued events?
	String queueName='aevQ'+(app.id).toString()
	while(doSerialization && !callMySelf){
		List evtQ=theQueuesFLD!=null ? theQueuesFLD[queueName] : []
		if(evtQ==null || evtQ==[])break
		List evtList=evtQ.sort{ (Long)it.t }
		def theEvent=evtList.remove(0)
		theQueuesFLD[queueName]=evtList

		Integer qsize=(Integer)evtQ.size()
		if(qsize>8) log.error "large queue size ${qsize}".toString()
		theEvent.date=new Date((Long)theEvent.t)
		handleEvents(theEvent, false, true, (LinkedHashMap)rtD.piston)
	}

	String msgt='Exiting'
	String semName=(String)rtD.semaphoreName
	if(doSerialization && semName!=(String)null && (Long)theSemaphoresFLD[semName]<=(Long)rtD.semaphore){
		msgt='Released Lock and exiting'
		theSemaphoresFLD[semName]=0L
	}
	if((Integer)rtD.logging>2) log.debug msgt 
	List data=rtD.collect{ it.key }
	for(item in data)rtD.remove((String)item)
}

private Boolean executeEvent(Map rtD, event){
	String myS='executeEvent'
	if((Boolean)rtD.eric) myDetail rtD, myS, 1
	try{
		rtD.event=event
		Map pEvt=state.lastEvent
		if(pEvt==null)pEvt=[:]
		rtD.previousEvent=pEvt
		String evntName=(String)event.name
		Integer index=0 //event?.index ?: 0
		if(event.jsonData!=null){
			Map attribute=Attributes()[evntName]
			String attrI=attribute!=null ? (String)attribute.i:(String)null
			if(attrI!=(String)null && event.jsonData[attrI]){ // .i is the attribute to lookup
				index=event.jsonData[attrI]
			}
			if(!index)index=1
		}
		Map srcEvent=null
		rtD.args=[:]
		Map sysV=(Map)rtD.systemVars
		if(event!=null){
			rtD.args= evntName=='time' && event.schedule!=null && event.schedule.args!=null && event.schedule.args instanceof Map ? event.schedule.args:(event.jsonData!=null ? event.jsonData:[:])
			if(evntName=='time' && event.schedule!=null){
				srcEvent=event.schedule.evt!=null ? event.schedule.evt:null
				Map tMap=event.schedule.stack
				if(tMap!=null){
					sysV['$index'].v=tMap.index
					sysV['$device'].v=tMap.device
					sysV['$devices'].v=tMap.devices
					rtD.json=tMap.json!=null ? tMap.json:[:]
					rtD.response=tMap.response!=null ? tMap.response:[:]
					index=srcEvent?.index!=null ? srcEvent.index:0
// more to restore here?
					rtD.systemVars=sysV
				}
			}
		}
		setSystemVariableValue(rtD, '$args', rtD.args)
		sysV=(Map)rtD.systemVars

		String theDevice=srcEvent!=null ? (String)srcEvent.device:(String)null
		def theDevice1=theDevice==(String)null && event.device ? event.device.id:null
		String theFinalDevice=theDevice!=(String)null ? theDevice : (theDevice1!=null ? (!isDeviceLocation(event.device) ? hashId(theDevice1) : (String)rtD.locationId) : (String)rtD.locationId)
		Map myEvt=[
			date:(Long)event.date.getTime(),
			delay:rtD.stats?.timing?.d ? (Long)rtD.stats.timing.d : 0L,
			device:theFinalDevice,
			index:index
		]
		if(srcEvent!=null){
			myEvt=myEvt + [
				name:(String)srcEvent.name,
				value:srcEvent.value,
				descriptionText:(String)srcEvent.descriptionText,
				unit:srcEvent.unit,
				physical:(Boolean)srcEvent.physical,
			]
		}else{
			myEvt=myEvt + [
				name:evntName,
				value:event.value,
				descriptionText:(String)event.descriptionText,
				unit:event.unit,
				physical:!!event.physical,
			]
		}
		rtD.currentEvent=myEvt
		state.lastEvent=myEvt

		rtD.conditionStateChanged=false
		rtD.pistonStateChanged=false
		rtD.fastForwardTo=0
		rtD.statementLevel=0
		rtD.break=false
		rtD.resumed=false
		rtD.terminated=false
		if(evntName=='time'){
			rtD.fastForwardTo=(Integer)event.schedule.i
		}
		sysV['$previousEventDate'].v=pEvt.date ?: now()
		sysV['$previousEventDelay'].v=pEvt.delay ?: 0L
		sysV['$previousEventDevice'].v=[pEvt.device]
		sysV['$previousEventDeviceIndex'].v=pEvt.index ?: 0
		sysV['$previousEventAttribute'].v=pEvt.name ?: ''
		sysV['$previousEventDescription'].v=pEvt.descriptionText ?: ''
		sysV['$previousEventValue'].v=pEvt.value ?: ''
		sysV['$previousEventUnit'].v=pEvt.unit ?: ''
		sysV['$previousEventDevicePhysical'].v=!!pEvt.physical

		sysV['$currentEventDate'].v=(Long)myEvt.date
		sysV['$currentEventDelay'].v=(Long)myEvt.delay
		sysV['$currentEventDevice'].v=[myEvt.device]
		sysV['$currentEventDeviceIndex'].v=myEvt.index!='' && myEvt.index!=null? (Integer)myEvt.index:0
		sysV['$currentEventAttribute'].v=(String)myEvt.name
		sysV['$currentEventDescription'].v=(String)myEvt.descriptionText
		sysV['$currentEventValue'].v=myEvt.value
		sysV['$currentEventUnit'].v=myEvt.unit
		sysV['$currentEventDevicePhysical'].v=(Boolean)myEvt.physical
		rtD.systemVars=sysV

		rtD.stack=[c: 0, s: 0, cs:[], ss:[]]
		Boolean ended=false
		try{
			Boolean allowed=!rtD.piston.r || rtD.piston.r.length==0 || evaluateConditions(rtD, (Map)rtD.piston, 'r', true)
			rtD.restricted=!rtD.piston.o?.aps && !allowed //allowPreScheduled tasks to execute during restrictions
			if(allowed || (Integer)rtD.fastForwardTo!=0){
				if((Integer)rtD.fastForwardTo==-3){
					//device related time schedules
					if(!(Boolean)rtD.restricted){
						def data=event.schedule.d
						if(data!=null && (String)data.d && (String)data.c){
							//we have a device schedule, execute it
							def device=getDevice(rtD, (String)data.d)
							if(device!=null){
								//executing scheduled physical command
								//used by fades, flashes, etc.
								executePhysicalCommand(rtD, device, (String)data.c, data.p, 0L, (String)null, true)
							}
						}
					}
				}else{
					if(executeStatements(rtD, (List)rtD.piston.s)){
						ended=true
						tracePoint(rtD, 'end', 0L, 0)
					}
					processSchedules rtD
				}
			}else{
				warn 'Piston execution aborted due to restrictions in effect', rtD
				//we need to run through all to update stuff
				rtD.fastForwardTo=-9
				Boolean a=executeStatements(rtD, (List)rtD.piston.s)
				ended=true
				tracePoint(rtD, 'end', 0L, 0)
				processSchedules rtD
			}
			if(!ended)tracePoint(rtD, 'break', 0L, 0)
		}catch (all){
			error 'An error occurred while executing the event: ', rtD, -2, all
		}
		if((Boolean)rtD.eric) myDetail rtD, myS+' Result: TRUE', -1
		return true
	}catch(all){
		error 'An error occurred within executeEvent: ', rtD, -2, all
	}
	processSchedules rtD
	return false
}

private void finalizeEvent(Map rtD, Map initialMsg, Boolean success=true){
	Long startTime=now()
	Boolean myPep=(Boolean)rtD.pep

	processSchedules(rtD, true)

	if(success){
		if((Integer)rtD.logging>0)info initialMsg, rtD
	}else error initialMsg, rtD

	updateLogs(rtD, (Long)rtD.timestamp)

	Map t0=getCachedMaps()
	String myId=(String)rtD.id
	if(t0!=null)theCacheFLD[myId].state=[:]+rtD.state
	state.state=[:]+rtD.state

	rtD.trace.d=Math.round(1.0D*now()-(Long)rtD.trace.t)
	if(t0!=null)theCacheFLD[myId].trace=[:]+rtD.trace
	state.trace=rtD.trace

	//flush the new cache value
	for(item in (Map)rtD.newCache)rtD.cache[(String)item.key]=item.value

	//overwrite state, might have changed meanwhile
	if(t0!=null){
		theCacheFLD[myId].cache=[:]+rtD.cache
		theCacheFLD[myId].store=[:]+rtD.store
	}
	if(myPep){
		atomicState.cache=rtD.cache
		atomicState.store=rtD.store
	}else{
		state.cache=rtD.cache
		state.store=rtD.store
	}

//remove large stuff
	List data=[ 'lstarted', 'lended', 'pStart', 'pEnd', 'generatedIn', 'ended', 'wAtSem', 'semaphoreDelay', 'vars', 'stateAccess', 'author', 'bin', 'build', 'newCache', 'mediaData', 'weather', 'logs', 'trace', 'systemVars', 'localVars', 'currentAction', 'previousEvent', 'json', 'response', 'cache', 'store', 'settings', 'locationModeId', 'locationId', 'coreVersion', 'hcoreVersion', 'cancelations', 'conditionStateChanged', 'pistonStateChanged', 'fastForwardTo', 'resumed', 'terminated', 'instanceId', 'wakingUp', 'statementLevel', 'args', 'nfl', 'temp' ]
	for(String foo in data) rtD.remove(foo)
	if(!(rtD?.event instanceof com.hubitat.hub.domain.Event)){
		if(rtD?.event?.responseData)rtD.event.responseData=[:]
		if(rtD?.event?.jsonData)rtD.event.jsonData=[:]
		if(rtD?.event?.setRtData)rtD.event.setRtData=[:]
		if(rtD?.event?.schedule?.stack)rtD.event.schedule.stack=[:]
	}

	if((Boolean)rtD.updateDevices) updateDeviceList(rtD, rtD.devices*.value.id) // this will clear the cache
	rtD.remove('devices')

	if(rtD.gvCache!=null || rtD.gvStoreCache!=null){
		unschedule(finishUIupdate)
		LinkedHashMap tpiston=[:]+(LinkedHashMap)rtD.piston
		rtD.piston=[:]
		rtD.piston.z=(String)tpiston.z

		if(rtD.gvCache!=null){
			Map vars=globalVarsFLD
			for(var in rtD.gvCache){
				String varName=(String)var.key
				if(varName && varName.startsWith('@') && vars[varName] && var.value.v!=vars[varName].v){
					globalVarsFLD[varName].v=var.value.v
				}
			}
		}

		parent.updateRunTimeData(rtD) // pCallupdateRunTimeData(rtD)

		rtD.piston=tpiston

		rtD.remove('gvCache')
		rtD.remove('gvStoreCache')
		//update graph data
		rtD.stats.timing.u=Math.round(1.0D*now()-startTime)
	}else{
		// schedule to update UI for state
		def st=[:]+state.state
		st.remove('old')
		//update graph data
		rtD.stats.timing.u=Math.round(1.0D*now()-startTime)
		Map myRt=[
			id:(String)rtD.id,
			active:(Boolean)rtD.active,
			category:rtD.category,
			stats:[
				nextSchedule:(Long)rtD.stats.nextSchedule,
				timing: (Map)rtD.stats.timing
			],
			piston:[
				z:(String)rtD.piston.z
			],
			state:st
		]
		runIn(2, finishUIupdate, [data: myRt])
	}

	Map stats
	if(myPep)stats=atomicState.stats
	else stats=state.stats
	stats=stats ?: [:]

	stats.timing=(List)stats.timing ?: []
	Boolean a=((List)stats.timing).push([:]+(Map)rtD.stats.timing)
	Integer t1=settings.maxStats!=null ? (Integer)settings.maxStats: (Integer)getPistonLimits.maxStats
	if(t1<=0)t1=(Integer)getPistonLimits.maxStats
	Integer t2=(Integer)((List)stats.timing).size()
	if(t2>t1){
		stats.timing=stats.timing[t2-t1..t2-1]
	}
	if(myPep)atomicState.stats=stats
	else state.stats=stats
	rtD.stats.timing=null

	if(t0!=null){
		theCacheFLD[myId].mem=mem()
		theCacheFLD[myId].runStats=[:]+rtD.curStat
		List hisList=theCacheFLD[myId].runTimeHis
		Long totTime=Math.round(now()*1.0D-(Long)rtD.timestamp)
		Boolean b=hisList.push(totTime)
		t1=20
		t2=(Integer)hisList.size()
		if(t2>t1) hisList=hisList[t2-t1..t2-1]
		theCacheFLD[myId].runTimeHis=hisList
	}
}

public void finishUIupdate(myRt){
	parent.updateRunTimeData(myRt) //pCallupdateRunTimeData(myRt)
}

private void processSchedules(Map rtD, Boolean scheduleJob=false){
	Boolean myPep=(Boolean)rtD.pep

	List schedules
	Map t0=getCachedMaps()
	if(t0!=null)schedules=[]+(List)t0.schedules
	else schedules=myPep ? (List)atomicState.schedules:(List)state.schedules

	//if automatic piston states, we set it based on the autoNew - if any
	if(rtD.piston.o?.mps==null || !rtD.piston.o.mps) rtD.state.new=(String)rtD.state.autoNew ?: 'true'
	rtD.state.old=(String)rtD.state.new

	if((Boolean)rtD.cancelations.all) Boolean a=schedules.removeAll{ (Integer)it.i>0 }

	//cancel statements
	Boolean b=schedules.removeAll{ Map schedule -> !!((List)rtD.cancelations.statements).find{ Map cancelation -> (Integer)cancelation.id==(Integer)schedule.s && (!cancelation.data || ((String)cancelation.data==(String)schedule.d))}}

	//cancel on conditions
	for(Integer cid in (List)rtD.cancelations.conditions){
		Boolean a=schedules.removeAll{ cid in (List)it.cs }
	}

	//cancel on piston state change
	if((Boolean)rtD.pistonStateChanged){
		Boolean a=schedules.removeAll{ (Integer)it.ps!=0 }
	}

	rtD.cancelations=[statements:[], conditions:[], all:false]
	schedules=(schedules+(List)rtD.schedules)//.sort{ (Long)it.t }

	String myId=(String)rtD.id
	if(t0!=null)theCacheFLD[myId].schedules=[]+schedules
	if(myPep)atomicState.schedules=schedules
	else state.schedules=[]+schedules

	if(scheduleJob){
		Long nextT=0L
		if((Integer)schedules.size()>0){
			Map tnext=schedules.sort{ (Long)it.t }[0]
			nextT=(Long)tnext.t
			Long t=Math.round((nextT-now())/1000.0D)
			t=(t<1L ? 1L:t)
			if((Integer)rtD.logging>0) info 'Setting up scheduled job for '+formatLocalTime(nextT)+' (in '+t.toString()+'s)' + ((Integer)schedules.size()>1 ? ', with ' + ((Integer)schedules.size()-1).toString() + ' more job' + ((Integer)schedules.size()>2 ? 's' : '') + ' pending' : ''), rtD
			Integer t1=Math.round(t)

			runIn(t1, timeHandler, [data: tnext])
		}
		if(nextT==0L && (Long)rtD.nextSchedule!=0L){
			unschedule(timeHandler)
			rtD.nextSchedule=0L
		}

		rtD.stats.nextSchedule=nextT
		if(t0!=null)theCacheFLD[myId].nextSchedule=nextT
		state.nextSchedule=nextT
	}
	rtD.schedules=[]
}

private void updateLogs(Map rtD, Long lastExecute=null){
	if(!rtD || !rtD.logs)return

	String myId=(String)rtD.id
	Map cacheMap=getCachedMaps()
	if(cacheMap!=null && lastExecute!=null){
		theCacheFLD[myId].lastExecuted=lastExecute
		state.lastExecuted=(Long)lastExecute
		theCacheFLD[myId].temp=rtD.temp
		theCacheFLD[myId].cachePersist=rtD.cachePersist
	}

	//we only save the logs if we got some
	if((Integer)((List)rtD.logs).size()<2)return

	Boolean myPep=(Boolean)rtD.pep
	Integer t1=settings.maxLogs!=null ? (Integer)settings.maxLogs:(Integer)getPistonLimits.maxLogs
	if(t1<=0)t1=(Integer)getPistonLimits.maxLogs

	List t0

	if(cacheMap!=null)t0=[]+(List)cacheMap.logs
	else t0=myPep ? atomicState.logs:state.logs
	List logs=[]+(List)rtD.logs+t0
	while (t1>=0){
		Integer t2=(Integer)logs.size()
		if(t1==0 || t2==0){ logs=[]; break }
		if(t1<(t2-1)){
			logs=logs[0..t1]
			state.logs=logs
		}
		if(t1>5 && (Integer)state.toString().size()>75000) t1 -= Math.min(50, Math.round(t1/2.0D))
		else break
	}
	if(cacheMap!=null)theCacheFLD[myId].logs=logs
	if(myPep)atomicState.logs=logs
	else state.logs=logs
	rtD.logs=[]
}

private Boolean executeStatements(Map rtD, List statements, Boolean async=false){
	rtD.statementLevel=(Integer)rtD.statementLevel+1
	for(Map statement in statements){
		//only execute statements that are enabled
		Boolean disab=statement.di!=null && (Boolean)statement.di
		if(!disab && !executeStatement(rtD, statement, async)){
			//stop processing
			rtD.statementLevel=(Integer)rtD.statementLevel-1
			return false
		}
	}
	//continue processing
	rtD.statementLevel=(Integer)rtD.statementLevel-1
	return true
}

private Boolean executeStatement(Map rtD, Map statement, Boolean async=false){
	//if rtD.fastForwardTo is a positive, non-zero number, we need to fast forward through all
	//branches until we find the task with an id equal to that number, then we play nicely after that
	if(statement==null)return false
	Integer statementNum=statement.$!=null ? (Integer)statement.$:0
	if((Integer)rtD.fastForwardTo==0){
		String sMsg="Skipping execution for statement #${statementNum} because "
		switch ((String)statement.tep){ // Task Execution Policy
		case 'c':
			if(!(Boolean)rtD.conditionStateChanged){
				if((Integer)rtD.logging>2)debug sMsg+'condition state did not change', rtD
				return true
			}
			break
		case 'p':
			if(!(Boolean)rtD.pistonStateChanged){
				if((Integer)rtD.logging>2)debug sMsg+'piston state did not change', rtD
				return true
			}
			break
		case 'b':
			if( !(Boolean)rtD.conditionStateChanged && !(Boolean)rtD.pistonStateChanged){
				if((Integer)rtD.logging>2)debug sMsg+'neither condition state nor piston state changed', rtD
				return true
			}
			break
		}
	}
	String mySt='executeStatement '+(String)statement.t
	if((Boolean)rtD.eric) myDetail rtD, mySt, 1
	Boolean a=((List)rtD.stack.ss).push((Integer)rtD.stack.s)
	rtD.stack.s=statementNum
	Long t=now()
	Boolean value=true
	Integer c=(Integer)rtD.stack.c
	Boolean stacked=true /* cancelable on condition change */
	if(stacked)a=((List)rtD.stack.cs).push(c)
	Boolean parentConditionStateChanged=(Boolean)rtD.conditionStateChanged
	//def parentAsync=async
	Double parentIndex=rtD.systemVars['$index'].v
	def parentDevice=rtD.systemVars['$device'].v
	Boolean selfAsync= (String)statement.a=='1' || (String)statement.t=='every' || (String)statement.t=='on' // execution method
	async=async || selfAsync
	Boolean myPep=(Boolean)rtD.pep
	Boolean perform=false
	Boolean repeat=true
	Double index=null
	Boolean allowed=!statement.r || statement.r.length==0 || evaluateConditions(rtD, statement, 'r', async)
	if(allowed || (Integer)rtD.fastForwardTo!=0){
		while (repeat){
			switch ((String)statement.t){
			case 'every':
				//we override current condition so that child statements can cancel on it
				Boolean ownEvent= rtD.event!=null && (String)rtD.event.name=='time' && rtD.event.schedule!=null && (Integer)rtD.event.schedule.s==statementNum && (Integer)rtD.event.schedule.i<0

				List schedules
				Map t0=getCachedMaps()
				if(t0!=null)schedules=[]+(List)t0.schedules
				else schedules=myPep ? (List)atomicState.schedules:(List)state.schedules
				if(ownEvent || !schedules.find{ (Integer)it.s==statementNum }){
					//if the time has come for our timer, schedule the next timer
					//if no next time is found quick enough, a new schedule with i=-2 will be setup so that a new attempt can be made at a later time
					if(ownEvent)rtD.fastForwardTo=0
					scheduleTimer(rtD, statement, ownEvent ? (Long)rtD.event.schedule.t:0L)
				}
				rtD.stack.c=statementNum
				if(ownEvent)rtD.fastForwardTo=0
				if((Integer)rtD.fastForwardTo!=0 || (ownEvent && allowed && !(Boolean)rtD.restricted)){
					//we don't want to run this if there are piston restrictions in effect
					//we only execute the every if i=-1 (for rapid timers with large restrictions i.e. every second, but only on Mondays)we need to make sure we don't block execution while trying
					//to find the next execution scheduled time, so we give up after too many attempts and schedule a rerun with i=-2 to give us the chance to try again at that later time
					if((Integer)rtD.fastForwardTo!=0 || (Integer)rtD.event.schedule.i==-1)a=executeStatements(rtD, (List)statement.s, true)
					//we always exit a timer, this only runs on its own schedule, nothing else is executed
					if(ownEvent)rtD.terminated=true
					value=false
					break
				}
				value=true
				break
			case 'repeat':
				//we override current condition so that child statements can cancel on it
				rtD.stack.c=statementNum
				if(!executeStatements(rtD, (List)statement.s, async)){
					//stop processing
					value=false
					if((Integer)rtD.fastForwardTo==0)break
				}
				value=true
				perform=(evaluateConditions(rtD, statement, 'c', async)==false)
				break
			case 'on':
				perform=false
				if((Integer)rtD.fastForwardTo==0){
					//look to see if any of the event matches
					String deviceId= rtD.event.device!=null ? hashId(rtD.event.device.id):(String)null
					for (event in statement.c){
						def operand=event.lo
						if(operand!=null && (String)operand.t){
							switch ((String)operand.t){
							case 'p':
								if(deviceId!=(String)null && (String)rtD.event.name==(String)operand.a && (List)operand.d!=[] && deviceId in expandDeviceList(rtD, (List)operand.d, true)) perform=true
								break
							case 'v':
								if((String)rtD.event.name==(String)operand.v) perform=true
								break
							case 'x':
								String operX=(String)operand.x
								if(rtD.event.value==operX && (String)rtD.event.name==(String)rtD.instanceId+'.'+operX) perform=true
								break
							}
						}
						if(perform)break
					}
				}
				value= (Integer)rtD.fastForwardTo!=0 || perform ? executeStatements(rtD, (List)statement.s, async):true
				break
			case 'if':
			case 'while':
				//check conditions for if and while
				perform=evaluateConditions(rtD, statement, 'c', async)
				//we override current condition so that child statements can cancel on it
				rtD.stack.c=statementNum
				if((Integer)rtD.fastForwardTo==0 && !rtD.piston.o?.mps && (String)statement.t=='if' && (Integer)rtD.statementLevel==1 && perform){
					//automatic piston state
					rtD.state.autoNew='true'
				}
				if(perform || (Integer)rtD.fastForwardTo!=0){
					if((String)statement.t in ['if', 'while']){
						if(!executeStatements(rtD, (List)statement.s, async)){
							//stop processing
							value=false
							if((Integer)rtD.fastForwardTo==0)break
						}
						value=true
						if((Integer)rtD.fastForwardTo==0)break
					}
				}
				if(!perform || (Integer)rtD.fastForwardTo!=0){
					if((String)statement.t=='if'){
						//look for else-ifs
						for (elseIf in statement.ei){
							perform=evaluateConditions(rtD, elseIf, 'c', async)
							if(perform || (Integer)rtD.fastForwardTo!=0){
								if(!executeStatements(rtD, (List)elseIf.s, async)){
									//stop processing
									value=false
									if((Integer)rtD.fastForwardTo==0)break
								}
								value=true
								if((Integer)rtD.fastForwardTo==0)break
							}
						}
						if((Integer)rtD.fastForwardTo==0 && !rtD.piston.o?.mps && (Integer)rtD.statementLevel==1){
							//automatic piston state
								rtD.state.autoNew='false'
						}
						if((!perform || (Integer)rtD.fastForwardTo!=0) && !executeStatements(rtD, (List)statement.e, async)){
							//stop processing
							value=false
							if((Integer)rtD.fastForwardTo==0)break
						}
					}
				}
				break
			case 'for':
			case 'each':
				List devices=[]
				Double startValue=0.0D
				Double endValue=0.0D
				Double stepValue=1.0D
				if((String)statement.t=='each'){
					def t0=((Map)evaluateOperand(rtD, null, (Map)statement.lo)).v
					devices=t0 ?: []
					endValue=(Integer)devices.size()-1.0D
				}else{
					startValue=(Double)evaluateScalarOperand(rtD, statement, (Map)statement.lo, null, 'decimal').v
					endValue=(Double)evaluateScalarOperand(rtD, statement, (Map)statement.lo2, null, 'decimal').v
					Double t0=(Double)evaluateScalarOperand(rtD, statement, (Map)statement.lo3, null, 'decimal').v
					stepValue=t0 ?: 1.0D
				}
				String counterVariable=(String)getVariable(rtD, (String)statement.x).t!='error' ? (String)statement.x:(String)null
				if( (startValue<=endValue && stepValue>0.0D) || (startValue>=endValue && stepValue<0.0D) || (Integer)rtD.fastForwardTo!=0){
					//initialize the for loop
					if((Integer)rtD.fastForwardTo!=0)index=(Double)cast(rtD, rtD.cache["f:${statementNum}".toString()], 'decimal')
					if(index==null){
						index=(Double)cast(rtD, startValue, 'decimal')
						//index=startValue
						rtD.cache["f:${statementNum}".toString()]=index
					}
					rtD.systemVars['$index'].v=index
					if((String)statement.t=='each' && (Integer)rtD.fastForwardTo==0)setSystemVariableValue(rtD, '$device', index<(Integer)devices.size() ? [devices[(Integer)index]]:[])
					if(counterVariable!=(String)null && (Integer)rtD.fastForwardTo==0)def m=setVariable(rtD, counterVariable, (String)statement.t=='each' ? (index<(Integer)devices.size() ? [devices[(Integer)index]]:[]):index)
					//do the loop
					perform=executeStatements(rtD, (List)statement.s, async)
					if(!perform){
						//stop processing
						value=false
						if((Boolean)rtD.break){
							//we reached a break, so we really want to continue execution outside of the for
							value=true
							rtD.break=false
							//perform=false
						}
						break
					}
					//don't do the rest if we're fast forwarding
					if((Integer)rtD.fastForwardTo!=0)break
					index=index+stepValue
					rtD.systemVars['$index'].v=index
					if((String)statement.t=='each' && (Integer)rtD.fastForwardTo==0)setSystemVariableValue(rtD, '$device', index<(Integer)devices.size() ? [devices[(Integer)index]]:[])
					if(counterVariable!=(String)null && (Integer)rtD.fastForwardTo==0)def n=setVariable(rtD, counterVariable, (String)statement.t=='each' ? (index<(Integer)devices.size()? [devices[(Integer)index]]:[]):index)
					rtD.cache["f:${statementNum}".toString()]=index
					if((stepValue>0.0D && index>endValue) || (stepValue<0.0D && index<endValue)){
						perform=false
						break
					}
				}
				break
			case 'switch':
				Map lo=[operand: (Map)statement.lo, values: (List)evaluateOperand(rtD, statement, (Map)statement.lo)]
				//go through all cases
				Boolean found=false
				Boolean implicitBreaks= (String)statement.ctp=='i' // case traversal policy
				Boolean fallThrough=!implicitBreaks
				perform=false
				if((Integer)rtD.logging>2)debug "Evaluating switch with values $lo.values", rtD
				for (Map _case in (List)statement.cs){
					Map ro=[operand: (Map)_case.ro, values: (List)evaluateOperand(rtD, _case, (Map)_case.ro)]
					Map ro2= (String)_case.t=='r' ? [operand: (Map)_case.ro2, values: (List)evaluateOperand(rtD, _case, (Map)_case.ro2, null, false, true)]:null
					perform=perform || evaluateComparison(rtD, ((String)_case.t=='r' ? 'is_inside_of_range':'is'), lo, ro, ro2)
					found=found || perform
					if(perform || (found && fallThrough)|| (Integer)rtD.fastForwardTo!=0){
						Integer fastForwardTo=(Integer)rtD.fastForwardTo
						if(!executeStatements(rtD, (List)_case.s, async)){
							//stop processing
							value=false
							if((Boolean)rtD.break){
								//we reached a break, so we really want to continue execution outside of the switch
								value=true
								found=true
								fallThrough=false
								rtD.break=false
							}
							if((Integer)rtD.fastForwardTo==0){
								break
							}
						}
						//if we determine that the fast forwarding ended during this execution, we assume found is true
						found=found || (fastForwardTo!=(Integer)rtD.fastForwardTo)
						value=true
						//if implicit breaks
						if(implicitBreaks && (Integer)rtD.fastForwardTo==0){
							fallThrough=false
							break
						}
					}
				}
				if(statement.e && ((List)statement.e).length && (value || (Integer)rtD.fastForwardTo!=0) && (!found || fallThrough || (Integer)rtD.fastForwardTo!=0)){
					//no case found, let's do the default
					if(!executeStatements(rtD, (List)statement.e, async)){
						//stop processing
						value=false
						if((Boolean)rtD.break){
							//we reached a break, so we really want to continue execution outside of the switch
							value=true
							rtD.break=false
						}
						if((Integer)rtD.fastForwardTo==0)break
					}
				}
				break
			case 'action':
				value=executeAction(rtD, statement, async)
				break
			case 'do':
				value=executeStatements(rtD, (List)statement.s, async)
				break
			case 'break':
				if((Integer)rtD.fastForwardTo==0){
					rtD.break=true
				}
				value=false
				break
			case 'exit':
				if((Integer)rtD.fastForwardTo==0){
					vcmd_setState(rtD, null, [(String)cast(rtD, ((Map)evaluateOperand(rtD, null, (Map)statement.lo)).v, 'string')])
					rtD.terminated=true
				}
				value=false
				break
			}
			//break the loop
			if((Integer)rtD.fastForwardTo!=0 || (String)statement.t=='if')perform=false

			//is this statement a loop
			Boolean loop=((String)statement.t in ['while', 'repeat', 'for', 'each'])
			if(loop && !value && (Boolean)rtD.break){
				//someone requested a break from the loop, we're doing it
				rtD.break=false
				//but we're allowing the rest to continue
				value=true
				perform=false
			}
			//do we repeat the loop?
			repeat=perform && value && loop && (Integer)rtD.fastForwardTo==0

			Long overBy=checkForSlowdown(rtD)
			if(overBy>0L){
				Long delay=(Long)getPistonLimits.taskShortDelay
				if(overBy>(Long)getPistonLimits.useBigDelay){
					delay=(Long)getPistonLimits.taskLongDelay
				}
				String mstr="executeStatement: Execution time exceeded by ${overBy}ms, "
				if(repeat && overBy>(Long)getPistonLimits.executionTime){
					error mstr+'Terminating', rtD
					rtD.terminated=true
					repeat=false
				}else{
					Long b=doPause(mstr+'Waiting for '+delay+'ms', delay, rtD)
				}
			}
		}
	}
	if((Integer)rtD.fastForwardTo==0){
		Map schedule
		if((String)statement.t=='every'){
			Map t0=((List)rtD.schedules).find{ (Integer)it.s==statementNum}
			if(t0==null){
				List schedules
				Map t1=getCachedMaps()
				if(t1!=null)schedules=[]+(List)t1.schedules
				else schedules=myPep ? (List)atomicState.schedules:(List)state.schedules
				schedule=schedules.find{ (Integer)it.s==statementNum }
			}else schedule=t0
		}
		String myS="s:${statementNum}".toString()
		Long myL=Math.round(1.0D*now()-t)
		if(schedule!=null){
			//timers need to show the remaining time
			tracePoint(rtD, myS, myL, Math.round(1.0D*now()-(Long)schedule.t))
		}else{
			tracePoint(rtD, myS, myL, value)
		}
	}
	//if(statement.a=='1'){
		//when an async action requests the thread termination, we continue to execute the parent
		//when an async action terminates as a result of a time event, we exit completely
//		value=(rtD.event.name!='time')
	//}
	if(selfAsync){
		//if running in async mode, we return true (to continue execution)
		value=!(Boolean)rtD.resumed
		rtD.resumed=false
	}
	if((Boolean)rtD.terminated){
		value=false
	}
	//restore current condition
	rtD.stack.c=c
	if(stacked){
		def tc=((List)rtD.stack.cs).pop()
	}
	rtD.stack.s=(Integer)((List)rtD.stack.ss).pop()
	rtD.systemVars['$index'].v=parentIndex
	rtD.systemVars['$device'].v=parentDevice
	rtD.conditionStateChanged=parentConditionStateChanged
	Boolean ret=value || (Integer)rtD.fastForwardTo!=0
	if((Boolean)rtD.eric) myDetail rtD, mySt+" result: $ret".toString(), -1
	return ret
}

private Long checkForSlowdown(Map rtD){
	//return how Long over the time limit we are
	Long overBy=0L
	Long curRunTime=Math.round(1.0D*now()-(Long)rtD.timestamp-(Long)getPistonLimits.slTime)
	if(curRunTime>overBy){
		overBy=curRunTime
	}
	return overBy
}

private Long doPause(String mstr, Long delay, Map rtD){
	Long actDelay=0L
	Long t0=now()
	if(!rtD.lastPause || (t0-(Long)rtD.lastPause)>1000L){
		if((Integer)rtD.logging>1)trace mstr+'; lastPause: '+rtD.lastPause, rtD
		rtD.lastPause=t0
		pauseExecution(delay)
		Long t1=now()
		actDelay=t1-t0
		Long t2=rtD.tPause!=null ? (Long)rtD.tPause : 0L
		rtD.tPause=t2+actDelay
		rtD.lastPause=t1
		t2=state.pauses!=null ? (Long)state.pauses : 0L
		state.pauses=t2+1L
	}
	return actDelay
}

private Boolean executeAction(Map rtD, Map statement, Boolean async){
	String mySt='executeAction'
	if((Boolean)rtD.eric) myDetail rtD, mySt, 1
	def parentDevicesVar=rtD.systemVars['$devices'].v
	//if override
	if((Integer)rtD.fastForwardTo==0 && (String)statement.tsp!='a'){ // Task scheduling policy
		cancelStatementSchedules(rtD, (Integer)statement.$)
	}
	Boolean result=true
	List deviceIds=expandDeviceList(rtD, (List)statement.d)
	List devices=deviceIds.collect{ getDevice(rtD, (String)it)}
	rtD.currentAction=statement
	for (Map task in (List)statement.k){
		if(task.$!=null && (Integer)task.$==(Integer)rtD.fastForwardTo){
			//resuming a waiting task, we need to bring back the devices
			if(rtD.event && rtD.event.schedule && rtD.event.schedule.stack){
				rtD.systemVars['$index'].v=rtD.event.schedule.stack.index
				rtD.systemVars['$device'].v=rtD.event.schedule.stack.device
				if(rtD.event.schedule.stack.devices instanceof List){
					deviceIds=(List)rtD.event.schedule.stack.devices
					rtD.systemVars['$devices'].v=deviceIds
					devices=deviceIds.collect{ getDevice(rtD, (String)it)}
				}
			}
		}
		rtD.systemVars['$devices'].v=deviceIds
		result=executeTask(rtD, devices, statement, task, async)
		if(!result && (Integer)rtD.fastForwardTo==0){
			break
		}
	}
	rtD.systemVars['$devices'].v=parentDevicesVar
	if((Boolean)rtD.eric) myDetail rtD, mySt+" result: $result".toString(), -1
	return result
}

private Boolean executeTask(Map rtD, List devices, Map statement, Map task, Boolean async){
	Long t=now()
	String myS='t:'+(Integer)task.$
	if((Integer)rtD.fastForwardTo!=0){
		if((Integer)task.$==(Integer)rtD.fastForwardTo){
			//finally found the resuming point, play nicely from hereon
			tracePoint(rtD, myS, Math.round(1.0D*now()-t), null)
			rtD.fastForwardTo=0
			//restore $device and $devices
			rtD.resumed=true
		}
		//we're not doing anything, we're fast forwarding...
		return true
	}
	if(task.m!=null && task.m instanceof List && (Integer)((List)task.m).size()>0){
		if(!((String)rtD.locationModeId in (List)task.m)){
			if((Integer)rtD.logging>2)debug "Skipping task ${(Integer)task.$} because of mode restrictions", rtD
			return true
		}
	}
	String mySt='executeTask '+(String)task.c
	if((Boolean)rtD.eric) myDetail rtD, mySt, 1

	//parse parameters
	List params=[]
	for (Map param in (List)task.p){
		def p
		switch ((String)param.vt){
		case 'variable':
			if((String)param.t!='x')continue
			p=param.x instanceof List ? (List)param.x : (String)param.x + ((String)param.xi!=(String)null ? '['+(String)param.xi+']':'')
			break
		default:
			Map v=(Map)evaluateOperand(rtD, null, param)
			//if not selected, we want to return null
			String tt1=(String)param.vt //?: (String)v.vt
			def t0=v.v
			Boolean match=(tt1!=null && ((tt1==(String)v.t)|| (t0 instanceof String && tt1 in ['string', 'enum', 'text'])||
					(t0 instanceof Integer && tt1=='integer')||
					(t0 instanceof Long && tt1=='long')||
					(t0 instanceof Double && tt1=='decimal')||
					(t0 instanceof BigDecimal && tt1=='decimal')))
			p=(t0!=null)? (!match ? evaluateExpression(rtD, v, tt1).v:t0):null
		}
		//ensure value type is successfuly passed through
		def a=params.push(p)
	}

	//handle duplicate command "push" which was replaced with fake command "pushMomentary"
	def override=CommandsOverrides.find{ (String)it.value.r==(String)task.c }
	String command=override ? (String)override.value.c:(String)task.c

	def virtualDevice=(Integer)devices.size()!=0 ? null:location
	Map vcmd=VirtualCommands()[command]
	Long delay=0L
	for (device in (virtualDevice!=null ? [virtualDevice]:devices)){
		if(virtualDevice==null && device.hasCommand(command) && !(vcmd && vcmd.o /*virtual command overrides physical command*/)){
			Map msg=timer "Executed [$device].${command}", rtD
			try{
				delay="cmd_${command}"(rtD, device, params)
			}catch(all){
				executePhysicalCommand(rtD, device, command, params)
			}
			if((Integer)rtD.logging>1)trace msg, rtD
		}else{
			if(vcmd!=null){
				delay=executeVirtualCommand(rtD, vcmd.a ? devices:device, command, params)
				//aggregate commands only run once, for all devices at the same time
				if(vcmd.a)break
			}
		}
	}
	//if we don't have to wait, we're home free

	//negative delays force us to reschedule, no sleeping on this one
	Boolean reschedule= delay<0L
	delay=reschedule ? -delay:delay

	if(delay!=0L){
		//get remaining piston time
		if(reschedule || async || delay>(Long)getPistonLimits.taskMaxDelay){
			//schedule a wake up
			Long sec=Math.round(delay/1000.0D)
			if((Integer)rtD.logging>1)trace "Requesting a wake up for ${formatLocalTime(Math.round(now()*1.0D+delay))} (in ${sec}s)", rtD
			tracePoint(rtD, myS, Math.round(1.0D*now()-t), -delay)
			requestWakeUp(rtD, statement, task, delay, (String)task.c)
			if((Boolean)rtD.eric) myDetail rtD, mySt+" result: FALSE".toString(), -1
			return false
		}else{
			if((Integer)rtD.logging>1)trace "executeTask: Waiting for ${delay}ms", rtD
			pauseExecution(delay)
		}
	}
	tracePoint(rtD, myS, Math.round(1.0D*now()-t), delay)

	//get remaining piston time
	Long overBy=checkForSlowdown(rtD)
	if(overBy>0L){
		Long mdelay=(Long)getPistonLimits.taskShortDelay
		if(overBy>(Long)getPistonLimits.useBigDelay){
			mdelay=(Long)getPistonLimits.taskLongDelay
		}
		Long actDelay=doPause("executeTask: Execution time exceeded by ${overBy}ms, Waiting for ${mdelay}ms", mdelay, rtD)
	}
	if((Boolean)rtD.eric) myDetail rtD, mySt+" result: TRUE".toString(), -1
	return true
}

private Long executeVirtualCommand(Map rtD, devices, String command, List params){
	Map msg=timer "Executed virtual command ${devices ? (devices instanceof List ? "$devices.":"[$devices]."):""}${command}", rtD
	Long delay=0L
	try{
		delay="vcmd_${command}"(rtD, devices, params)
		if((Integer)rtD.logging>1)trace msg, rtD
	}catch(all){
		msg.m="Error executing virtual command ${devices instanceof List ? "$devices":"[$devices]"}.${command}:"
		msg.e=all
		error msg, rtD
	}
	return delay
}

private void executePhysicalCommand(Map rtD, device, String command, params=[], Long delay=0L, String scheduleDevice=(String)null, Boolean disableCommandOptimization=false){
	if(delay!=0L && scheduleDevice!=(String)null){
		//delay without schedules is not supported in hubitat
		//scheduleDevice=hashId(device.id)
		//we're using schedules instead
		Map statement=(Map)rtD.currentAction
		List cs=[]+ ((String)statement.tcp=='b' || (String)statement.tcp=='c' ? (rtD.stack?.cs!=null ? (List)rtD.stack.cs:[]):[]) // task cancelation policy
		Integer ps= (String)statement.tcp=='b' || (String)statement.tcp=='p' ? 1:0
		Boolean a=cs.removeAll{ it==0 }
		Map schedule=[
			t:(Long)Math.round(now()*1.0D+delay),
			s:(Integer)statement.$,
			i:-3,
			cs:cs,
			ps:ps,
			d:[
				d:scheduleDevice,
				c:command,
				p:params
			]
		]
		if((Boolean)rtD.eric)trace "Requesting a physical command wake up for ${formatLocalTime(Math.round(now()*1.0D+delay))}", rtD
		a=((List)rtD.schedules).push(schedule)
	}else{
		List nparams=(params instanceof List)? (List)params:(params!=null ? [params]:[])
		try{
			//cleanup the params so that SONOS works
			while ((Integer)nparams.size()>0 && nparams[(Integer)nparams.size()-1]==null)def a=nparams.pop()
			Map msg=timer '', rtD
			Boolean skip=false
			if(!rtD.piston.o?.dco && !disableCommandOptimization && !(command in ['setColorTemperature', 'setColor', 'setHue', 'setSaturation'])){
				def cmd=PhysicalCommands()[command]
				if(cmd!=null && (String)cmd.a!=(String)null){
					if(cmd.v!=null && (Integer)nparams.size()==0){
						//commands with no parameter that set an attribute to a preset value
						if((String)getDeviceAttributeValue(rtD, device, (String)cmd.a)==(String)cmd.v){
							skip=true
						}
					}else if((Integer)nparams.size()==1){
						if(getDeviceAttributeValue(rtD, device, (String)cmd.a)==nparams[0]){
							skip=(command in ['setLevel', 'setInfraredLevel'] ? (String)getDeviceAttributeValue(rtD, device, 'switch')=='on':true)
						}
					}
				}
			}
			//if we're skipping, we already have a message
			String tstr=' physical command ['+"${device.label ?: device.name}".toString()+'].'+command+'('
			if(skip){
				msg.m='Skipped execution of'+tstr+"$nparams"+') because it would make no change to the device.'
			}else{
				String tailStr=')'
				if(delay>(Long)getPistonLimits.taskMaxDelay)delay=1000L
				if(delay>0L){
					pauseExecution(delay) //simulated in hubitat
					tailStr="[delay: $delay])"
				}
				tstr='Executed'+tstr
				if((Integer)nparams.size()>0){
					msg.m=tstr+"$nparams"+', '+tailStr
					device."$command"(nparams as Object[])
				}else{
					msg.m=tstr+tailStr
					device."$command"()
				}
			}
			if((Integer)rtD.logging>2)debug msg, rtD
		}catch(all){
			error "Error while executing physical command $device.$command($nparams):", rtD, -2, all
		}
		Long t0=rtD.piston.o?.ced ? (Integer)rtD.piston.o.ced:0L
		if(t0!=0L){
			if(t0>(Long)getPistonLimits.taskMaxDelay)t0=1000L
			pauseExecution(t0)
			if((Integer)rtD.logging>1)trace "Injected command execution delay ${t0}ms after [$device].$command(${nparams ? "$nparams":''})", rtD
		}
	}
}

private void scheduleTimer(Map rtD, Map timer, Long lastRun=0L){
	//if already scheduled once during this run, don't do it again
	if(((List)rtD.schedules).find{ (Integer)it.s==(Integer)timer.$ })return
	if((Boolean)rtD.eric) myDetail rtD, "scheduleTimer $timer     $lastRun", 1
	//complicated stuff follows...
	Long t=now()
	String tinterval="${((Map)evaluateOperand(rtD, null, (Map)timer.lo)).v}".toString()
	if(!tinterval.isInteger()){
		if((Boolean)rtD.eric) myDetail rtD, "scheduleTimer", -1
		return
	}
	Integer interval=tinterval.toInteger()
	if(interval<=0){
		if((Boolean)rtD.eric) myDetail rtD, "scheduleTimer", -1
		return
	}
	String intervalUnit=(String)timer.lo.vt
	Integer level=0
	switch(intervalUnit){
		case 'ms': level=1; break
		case 's': level=2; break
		case 'm': level=3; break
		case 'h': level=4; break
		case 'd': level=5; break
		case 'w': level=6; break
		case 'n': level=7; break
		case 'y': level=8; break
	}

	Long delta=0L
	Long time=0L
	switch (intervalUnit){
		case 'ms': delta=1L; break
		case 's': delta=1000L; break
		case 'm': delta=60000L; break
		case 'h': delta=3600000L; break
	}

	if(delta==0L){
		//let's get the offset
		time=(Long)evaluateExpression(rtD, (Map)evaluateOperand(rtD, null, (Map)timer.lo2), 'datetime').v
		if((String)timer.lo2.t!='c'){
			Map offset=(Map)evaluateOperand(rtD, null, (Map)timer.lo3)
			time += (Long)evaluateExpression(rtD, [t:'duration', v:offset.v, vt:(String)offset.vt], 'long').v
		}
		//resulting time is in UTC
		if(lastRun==0L){
			//first run, just adjust the time so we're in the future
			time=pushTimeAhead(time, now())
		}
	}
	delta=Math.round(delta*interval*1.0D)
	Boolean priorActivity=lastRun!=0L

	Long rightNow=now()
	lastRun=lastRun!=0L ? lastRun:rightNow
	Long nextSchedule=lastRun

	if(lastRun>rightNow){
		//sometimes timers early, so we need to make sure we're at least in the near future
		rightNow=Math.round(lastRun+1.0D)
	}

	if(intervalUnit=='h'){
		Long min=(Long)cast(rtD, timer.lo.om, 'long')
		nextSchedule=Math.round(3600000.0D*Math.floor(nextSchedule/3600000.0D)+(min*60000.0D))
	}

	//next date
	Integer cycles=100
	while (cycles!=0){
		if(delta!=0L){
			if(nextSchedule<(rightNow-delta)){
				//we're behind, let's fast forward to where the next occurrence happens in the future
				Long count=Math.floor((rightNow-nextSchedule)/delta*1.0D)
				//if((Integer)rtD.logging>2)debug "Timer fell behind by $count interval${count>1 ? 's':''}, catching up...", rtD
				nextSchedule=Math.round(nextSchedule+delta*count*1.0D)
			}
			nextSchedule=nextSchedule+delta
		}else{
			//advance one day if we're in the past
			time=pushTimeAhead(time, rightNow)
			Long lastDay=Math.floor(nextSchedule/86400000.0D)
			Long thisDay=Math.floor(time/86400000.0D)

			//the repeating interval is not necessarily constant
			switch (intervalUnit){
				case 'd':
					if(priorActivity){
						//add the required number of days
						nextSchedule=time+Math.round(86400000.0D*(interval-(thisDay-lastDay)))
					}else{
						nextSchedule=time
					}
					break
				case 'w':
					//figure out the first day of the week matching the requirement
					Long currentDay=(Integer)(new Date(time)).day
					Long requiredDay=(Long)cast(rtD, timer.lo.odw, 'long')
					if(currentDay>requiredDay)requiredDay += 7
					//move to first matching day
					nextSchedule=time+Math.round(86400000.0D*(requiredDay-currentDay))
					if(nextSchedule<rightNow){
						nextSchedule=Math.round(nextSchedule+604800000.0D*interval)
					}
					break
				case 'n':
				case 'y':
					//figure out the first day of the week matching the requirement
					Integer odm=timer.lo.odm.toInteger()
					def odw=timer.lo.odw
					Integer omy=intervalUnit=='y' ? timer.lo.omy.toInteger():0
					Integer day=0
					Date date=new Date(time)
					Integer year=(Integer)date.year
					Integer month=Math.round((intervalUnit=='n' ? (Integer)date.month:omy)+(priorActivity ? interval:((nextSchedule<rightNow)? 1.0D:0.0D))*(intervalUnit=='n' ? 1.0D:12.0D))
					if(month>=12){
						year += Math.floor(month/12.0D)
						month=month.mod(12)
					}
					date.setDate(1)
					date.setMonth(month)
					date.setYear(year)
					Integer lastDayOfMonth=(Integer)(new Date((Integer)date.year, (Integer)date.month+1, 0)).date
					if(odw=='d'){
						if(odm>0){
							day=(odm<=lastDayOfMonth)? odm:0
						}else{
							day=lastDayOfMonth+1+odm
							day=(day>=1)? day:0
						}
					}else{
						odw=odw.toInteger()
						//find the nth week day of the month
						if(odm>0){
							//going forward
							Integer firstDayOfMonthDOW=(Integer)(new Date((Integer)date.year, (Integer)date.month, 1)).day
							//find the first matching day
							Integer firstMatch=Math.round(1+odw-firstDayOfMonthDOW+(odw<firstDayOfMonthDOW ? 7.0D:0.0D))
							day=Math.round(firstMatch+7.0D*(odm-1.0D))
							day=(day<=lastDayOfMonth)? day:0
						}else{
							//going backwards
							Integer lastDayOfMonthDOW=(Integer)(new Date((Integer)date.year, (Integer)date.month+1, 0)).day
							//find the first matching day
							Integer firstMatch=lastDayOfMonth+odw-lastDayOfMonthDOW-(odw>lastDayOfMonthDOW ? 7:0)
							day=Math.round(firstMatch+7.0D*(odm+1))
							day=(day>=1)? day:0
						}
					}
					if(day){
						date.setDate(day)
						nextSchedule=(Long)date.getTime()
					}
					break
			}
		}
		//check to see if it fits the restrictions
		if(nextSchedule>=rightNow){
			Long offset=checkTimeRestrictions(rtD, timer.lo, nextSchedule, level, interval)
			if(offset==0L)break
			if(offset>0L)nextSchedule += offset
		}
		time=nextSchedule
		priorActivity=true
		cycles -= 1
	}

	if(nextSchedule>lastRun){
		Boolean a=((List)rtD.schedules).removeAll{ (Integer)it.s==(Integer)timer.$ }
		requestWakeUp(rtD, timer, [$: -1], nextSchedule)
	}
	if((Boolean)rtD.eric) myDetail rtD, "scheduleTimer", -1
}

private Long pushTimeAhead(Long pastTime, Long curTime){
	Long retTime=pastTime
	while (retTime<curTime){
		Long t0=Math.round(retTime+86400000.0D)
		Long t1=Math.round(t0+((Integer)location.timeZone.getOffset(retTime)-(Integer)location.timeZone.getOffset(t0))*1.0D)
		retTime=t1
	}
	return retTime
}

private void scheduleTimeCondition(Map rtD, Map condition){
	if((Boolean)rtD.eric) myDetail rtD, "scheduleTimeCondition", 1
	Integer conditionNum=(Integer)condition.$
	//if already scheduled once during this run, don't do it again
	if(((List)rtD.schedules).find{ (Integer)it.s==conditionNum && (Integer)it.i==0 })return
	Map comparison=Comparisons().conditions[(String)condition.co]
	Boolean trigger=false
	if(comparison==null){
		comparison=Comparisons().triggers[(String)condition.co]
		if(comparison==null)return
		trigger=true
	}
	cancelStatementSchedules(rtD, conditionNum)
	if(!comparison.p)return
	Map tv1=condition.ro!=null && (String)condition.ro.t!='c' ? (Map)evaluateOperand(rtD, null, (Map)condition.to):null

	Map v1a=(Map)evaluateOperand(rtD, null, (Map)condition.ro)
	Long tt0=(Long)v1a.v
	Long v1b= (String)v1a.t=='time' && tt0<86400000L ? getTimeToday(tt0):tt0
	Long v1c= tv1!=null ? ((String)tv1.t=='integer' && (Integer)tv1.v==0 ? 0L : (Long)evaluateExpression(rtD,[t:'duration',v:tv1.v,vt:(String)tv1.vt], 'long').v) :0L
	Long v1=v1b+v1c

	Map tv2=condition.ro2!=null && (String)condition.ro2.t!='c' && (Integer)comparison.p>1 ? (Map)evaluateOperand(rtD, null, (Map)condition.to2):null
	Long v2=trigger ? v1 : ((Integer)comparison.p>1 ? ((Long)evaluateExpression(rtD, (Map)evaluateOperand(rtD, null, (Map)condition.ro2, null, false, true), 'datetime').v + (tv2!=null ? (Long)evaluateExpression(rtD, [t:'duration', v:tv2.v, vt:(String)tv2.vt]).v : 0L)) : (String)condition.lo.v=='time' ? getMidnightTime():v1 )
	Long n=Math.round(1.0D*now()+2000L)
	if((String)condition.lo.v=='time'){
		v1=pushTimeAhead(v1, n)
		v2=pushTimeAhead(v2, n)
	}
	//figure out the next time
	v1=(v1<n)? v2:v1
	v2=(v2<n)? v1:v2
	n=v1<v2 ? v1:v2
	if(n>now()){
		if((Integer)rtD.logging>2)debug "Requesting time schedule wake up at ${formatLocalTime(n)}", rtD
		requestWakeUp(rtD, condition, [$:0], n)
	}
	if((Boolean)rtD.eric) myDetail rtD, "scheduleTimeCondition", -1
}

private Long checkTimeRestrictions(Map rtD, Map operand, Long time, Integer level, Integer interval){
	//returns 0 if restrictions are passed
	//returns a positive number as millisecond offset to apply to nextSchedule for fast forwarding
	//returns a negative number as a failed restriction with no fast forwarding offset suggestion

	List om=level<=2 && operand.om instanceof List && (Integer)((List)operand.om).size()>0 ? (List)operand.om:null
	List oh=level<=3 && operand.oh instanceof List && (Integer)((List)operand.oh).size()>0 ? (List)operand.oh:null
	List odw=level<=5 && operand.odw instanceof List && (Integer)((List)operand.odw).size()>0 ? (List)operand.odw:null
	List odm=level<=6 && operand.odm instanceof List && (Integer)((List)operand.odm).size()>0 ? (List)operand.odm:null
	List owm=level<=6 && odm==null && operand.owm instanceof List && (Integer)((List)operand.owm).size()>0 ? (List)operand.owm:null
	List omy=level<=7 && operand.omy instanceof List && (Integer)((List)operand.omy).size()>0 ? (List)operand.omy:null


	if(om==null && oh==null && odw==null && odm==null && owm==null && omy==null)return 0L
	Date date=new Date(time)

	Long result=-1L
	//month restrictions
	if(omy!=null && (omy.indexOf((Integer)date.month+1)<0)){
		Integer month=(omy.sort{ it }.find{ it>(Integer)date.month+1 } ?: 12+omy.sort{ it }[0])- 1
		Integer year=date.year+(month>=12 ? 1:0)
		month=(month>=12 ? month-12:month)
		Long ms=(Long)(new Date(year, month, 1)).time-time
		switch (level){
		case 2: //by second
			result=Math.round(interval*(Math.floor(ms/1000.0D/interval)-2.0D)*1000.0D)
			break
		case 3: //by minute
			result=Math.round(interval*(Math.floor(ms/60000.0D/interval)-2.0D)*60000.0D)
			break
		}
		return (result>0L)? result:-1L
	}

	//week of month restrictions
	if(owm!=null){
		if(!((owm.indexOf(getWeekOfMonth(date))>=0)|| (owm.indexOf(getWeekOfMonth(date, true))>=0))){
			switch (level){
			case 2: //by second
				result=Math.round(interval*(Math.floor(((7.0D-(Integer)date.day)*86400.0D-(Integer)date.hours*3600.0D-(Integer)date.minutes*60.0D)/interval)-2.0D)*1000.0D)
				break
			case 3: //by minute
				result=Math.round(interval*(Math.floor(((7.0D-(Integer)date.day)*1440.0D-(Integer)date.hours*60.0D-(Integer)date.minutes)/interval)-2.0D)*60000.0D)
				break
			}
			return (result>0L)? result:-1L
		}
	}

	//day of month restrictions
	if(odm!=null){
		if(odm.indexOf((Integer)date.date)<0){
			Integer lastDayOfMonth=(Integer)(new Date((Integer)date.year, (Integer)date.month+1, 0)).date
			if(odm.find{ it<1 }){
				//we need to add the last days
				odm=[]+odm //copy the array
				if(odm.indexOf(-1)>=0)Boolean a=odm.push(lastDayOfMonth)
				if(odm.indexOf(-2)>=0)Boolean a=odm.push(lastDayOfMonth-1)
				if(odm.indexOf(-3)>=0)Boolean a=odm.push(lastDayOfMonth-2)
				Boolean a=odm.removeAll{ it<1 }
			}
			switch (level){
			case 2: //by second
				result=Math.round(interval*(Math.floor((((odm.sort{ it }.find{ it>(Integer)date.date } ?: lastDayOfMonth+odm.sort{ it }[0])-(Integer)date.date)*86400.0D-(Integer)date.hours*3600.0D-(Integer)date.minutes*60.0D)/interval)- 2.0D)*1000.0D)
				break
			case 3: //by minute
				result=Math.round(interval*(Math.floor((((odm.sort{ it }.find{ it>(Integer)date.date } ?: lastDayOfMonth+odm.sort{ it }[0])-(Integer)date.date)*1440.0D-(Integer)date.hours*60.0D-(Integer)date.minutes)/interval)-2.0D)*60000.0D)
				break
			}
			return (result>0L)? result:-1L
		}
	}

	//day of week restrictions
	if(odw!=null && (odw.indexOf(date.day)<0)){
		switch (level){
		case 2: //by second
			result=Math.round(interval*(Math.floor((((odw.sort{ it }.find{ it>(Integer)date.day } ?: 7.0D+odw.sort{ it }[0])-(Integer)date.day)*86400.0D-(Integer)date.hours*3600.0D-(Integer)date.minutes*60.0D)/interval)-2.0D)*1000.0D)
			break
		case 3: //by minute
			result=Math.round(interval*(Math.floor((((odw.sort{ it }.find{ it>(Integer)date.day } ?: 7.0D+odw.sort{ it }[0])-(Integer)date.day)*1440.0D-(Integer)date.hours*60.0D-(Integer)date.minutes)/interval)-2.0D)*60000.0D)
			break
		}
		return (result>0L)? result:-1L
	}

	//hour restrictions
	if(oh!=null && (oh.indexOf((Integer)date.hours)<0)){
		switch (level){
		case 2: //by second
			result=Math.round(interval*(Math.floor((((oh.sort{ it }.find{ it>(Integer)date.hours } ?: 24.0D+oh.sort{ it }[0])-(Integer)date.hours)*3600.0D-(Integer)date.minutes*60.0D)/interval)-2.0D)*1000.0D)
			break
		case 3: //by minute
			result=Math.round(interval*(Math.floor((((oh.sort{ it }.find{ it>(Integer)date.hours } ?: 24.0D+oh.sort{ it }[0])-(Integer)date.hours)*60.0D-(Integer)date.minutes)/interval)-2.0D)*60000.0D)
			break
		}
		return (result>0L)? result:-1L
	}

	//minute restrictions
	if(om!=null && (om.indexOf((Integer)date.minutes)<0)){
		//get the next highest minute
	//suggest an offset to reach the next minute
		result=Math.round(interval*(Math.floor(((om.sort{ it }.find{ it>(Integer)date.minutes } ?: 60.0D+om.sort{ it }[0])-(Integer)date.minutes-1.0D)*60.0D/interval)-2.0D)*1000.0D)
		return (result>0L)? result:-1L
	}
	return 0L
}


//return the number of occurrences of same day of week up until the date or from the end of the month if backwards, i.e. last Sunday is -1, second-last Sunday is -2
private Integer getWeekOfMonth(date=null, Boolean backwards=false){
	Integer day=(Integer)date.date
	if(backwards){
		Integer month=(Integer)date.month
		Integer year=(Integer)date.year
		Integer lastDayOfMonth=(Integer)(new Date(year, month+1, 0)).date
		return -(1+Math.floor((lastDayOfMonth-day)/7))
	}else{
		return 1+Math.floor((day-1)/7) //1 based
	}
}


private void requestWakeUp(Map rtD, Map statement, Map task, Long timeOrDelay, String data=(String)null){
	Long time=timeOrDelay>9999999999L ? timeOrDelay:now()+timeOrDelay
	List cs=[]+ ((String)statement.tcp=='b' || (String)statement.tcp=='c' ? (rtD.stack?.cs!=null ? (List)rtD.stack.cs:[]):[]) // task cancelation policy
	Integer ps= (String)statement.tcp=='b' || (String)statement.tcp=='p' ? 1:0
	Boolean a=cs.removeAll{ it==0 }
// state to save across a sleep
	Map schedule=[
		t:time,
		s:(Integer)statement.$,
		i:task?.$!=null ? (Integer)task.$:0,
		cs:cs,
		ps:ps,
		d:data,
		evt:rtD.currentEvent,
		args:rtD.args,
		stack:[
			index:rtD.systemVars['$index'].v,
			device:rtD.systemVars['$device'].v,
			devices:rtD.systemVars['$devices'].v,
			json:rtD.json ?: [:],
			response:rtD.response ?: [:]
// what about previousEvent httpContentType httpStatusCode httpStatusOk iftttStatusCode iftttStatusOk "\$mediaId" "\$mediaUrl" "\$mediaType" mediaData (big)
// currentEvent in case of httpRequest
		]
	]
	a=((List)rtD.schedules).push(schedule)
}

private Long do_setLevel(Map rtD, device, List params, String attr, val=null){
	Integer arg=val!=null ? (Integer)val:(Integer)params[0]
	String mstate=(Integer)params.size()>1 ? (String)params[1]:(String)null
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	Long delay=(Integer)params.size()>2 ? (Long)params[2]:0L
	executePhysicalCommand(rtD, device, attr, arg, delay)
	return 0L
}

private Long cmd_setLevel(Map rtD, device, List params){
	return do_setLevel(rtD, device, params, 'setLevel')
}

private Long cmd_setInfraredLevel(Map rtD, device, List params){
	return do_setLevel(rtD, device, params, 'setInfraredLevel')
}

private Long cmd_setHue(Map rtD, device, List params){
	Integer hue=(Integer)cast(rtD, Math.round(params[0]/3.6D), 'integer')
	return do_setLevel(rtD, device, params, 'setHue', hue)
}

private Long cmd_setSaturation(Map rtD, device, List params){
	return do_setLevel(rtD, device, params, 'setSaturation')
}

private Long cmd_setColorTemperature(Map rtD, device, List params){
	return do_setLevel(rtD, device, params, 'setColorTemperature')
}

private Map getColor(Map rtD, String colorValue){
	Map color=(colorValue=='Random')? getRandomColor(rtD):getColorByName(rtD, colorValue)
	if(color!=null){
		color=[
			hex:(String)color.rgb,
			hue:Math.round((Integer)color.h/3.6D),
			saturation:(Integer)color.s,
			level:(Integer)color.l
		]
	}else{
		color=hexToColor(colorValue)
		if(color!=null){
			color=[
				hex:(String)color.hex,
				hue:Math.round((Integer)color.hue/3.6D),
				saturation:(Integer)color.saturation,
				level:(Integer)color.level
			]
		}
	}
	return color
}

private Long cmd_setColor(Map rtD, device, List params){
	def color=getColor(rtD, (String)params[0])
	if(!color){
		error "ERROR: Invalid color $params", rtD
		return 0L
	}
	String mstate=(Integer)params.size()>1 ? (String)params[1]:(String)null
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	Long delay=(Integer)params.size()>2 ? (Long)params[2]:0L
	executePhysicalCommand(rtD, device, 'setColor', color, delay)
	return 0L
}

private Long cmd_setAdjustedColor(Map rtD, device, List params){
	def color=getColor(rtD, (String)params[0])
	if(!color){
		error "ERROR: Invalid color $params", rtD
		return 0L
	}
	Long duration=(Long)cast(rtD, params[1], 'long')
	String mstate=(Integer)params.size()>2 ? (String)params[2]:(String)null
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	Long delay=(Integer)params.size()>3 ? (Long)params[3]:0L
	executePhysicalCommand(rtD, device, 'setAdjustedColor', [color, duration], delay)
	return 0L
}

private Long cmd_setAdjustedHSLColor(Map rtD, device, List params){
	Integer hue=(Integer)cast(rtD, Math.round(params[0]/3.6D), 'integer')
	Integer saturation=(Integer)params[1]
	Integer level=(Integer)params[2]
	def color=[
		hue: hue,
		saturation: saturation,
		level: level
	]
	Long duration=(Long)cast(rtD, params[3], 'long')
	String mstate=(Integer)params.size()>4 ? (String)params[4]:(String)null
	Long delay=(Integer)params.size()>5 ? (Long)params[5]:0L
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	executePhysicalCommand(rtD, device, 'setAdjustedColor', [color, duration], delay)
	return 0L
}

private Long cmd_setLoopDuration(Map rtD, device, List params){
	Integer duration=(Integer)Math.round((Long)cast(rtD, params[0], 'long')/1000)
	executePhysicalCommand(rtD, device, 'setLoopDuration', duration)
	return 0L
}

private Long cmd_setVideoLength(Map rtD, device, List params){
	Integer duration=(Integer)Math.round((Long)cast(rtD, params[0], 'long')/1000)
	executePhysicalCommand(rtD, device, 'setVideoLength', duration)
	return 0L
}


private Long vcmd_log(Map rtD, device, List params){
	String command=params[0] ? (String)params[0]:''
	String message=(String)params[1]
	Map a=log(message, rtD, -2, null, command.toLowerCase().trim(), true)
	return 0L
}

private Long vcmd_setState(Map rtD, device, List params){
	String value=params[0]
	if(rtD.piston.o?.mps){
		rtD.state.new=value
		rtD.pistonStateChanged=(Boolean)rtD.pistonStateChanged || ((String)rtD.state.old!=(String)rtD.state.new)
	}else{
		error "Cannot set the piston state while in automatic mode. Please edit the piston settings to disable the automatic piston state if you want to manually control the state.", rtD
	}
	return 0L
}

private Long vcmd_setTileColor(Map rtD, device, List params){
	Integer index=(Integer)cast(rtD, params[0], 'integer')
	if(index<1 || index>16)return 0L
	rtD.state["c$index".toString()]=(String)getColor(rtD, (String)params[1])?.hex
	rtD.state["b$index".toString()]=(String)getColor(rtD, (String)params[2])?.hex
	rtD.state["f$index".toString()]=!!params[3]
	return 0L
}

private Long vcmd_setTileTitle(Map rtD, device, List params){
	return helper_setTile(rtD, 'i', params)
}

private Long vcmd_setTileText(Map rtD, device, List params){
	return helper_setTile(rtD, 't', params)
}

private Long vcmd_setTileFooter(Map rtD, device, List params){
	return helper_setTile(rtD, 'o', params)
}

private Long vcmd_setTileOTitle(Map rtD, device, List params){
	return helper_setTile(rtD, 'p', params)
}

private Long helper_setTile(Map rtD, String typ, List params){
	Integer index=(Integer)cast(rtD, params[0], 'integer')
	if(index<1 || index>16)return 0L
	rtD.state["${typ}$index".toString()]=(String)params[1]
	return 0L
}

private Long vcmd_setTile(Map rtD, device, List params){
	Integer index=(Integer)cast(rtD, params[0], 'integer')
	if(index<1 || index>16)return 0L
	rtD.state["i$index".toString()]=(String)params[1]
	rtD.state["t$index".toString()]=(String)params[2]
	rtD.state["o$index".toString()]=(String)params[3]
	rtD.state["c$index".toString()]=(String)getColor(rtD, (String)params[4])?.hex
	rtD.state["b$index".toString()]=(String)getColor(rtD, (String)params[5])?.hex
	rtD.state["f$index".toString()]=!!params[6]
	return 0L
}

private Long vcmd_clearTile(Map rtD, device, List params){
	Integer index=(Integer)cast(rtD, params[0], 'integer')
	if(index<1 || index>16)return 0L
	Map t0=rtD.state
	t0.remove("i$index".toString())
	t0.remove("t$index".toString())
	t0.remove("c$index".toString())
	t0.remove("o$index".toString())
	t0.remove("b$index".toString())
	t0.remove("f$index".toString())
	t0.remove("p$index".toString())
	rtD.state=t0
	return 0L
}

private Long vcmd_setLocationMode(Map rtD, device, List params){
	String modeIdOrName=(String)params[0]
	def mode=location.getModes()?.find{ (hashId((Long)it.id)==modeIdOrName)|| ((String)it.name==modeIdOrName)}
	if(mode) location.setMode((String)mode.name)
	else error "Error setting location mode. Mode '$modeIdOrName' does not exist.", rtD
	return 0L
}

private Long vcmd_setAlarmSystemStatus(Map rtD, device, List params){
	String statusIdOrName=(String)params[0]
	def dev=VirtualDevices()['alarmSystemStatus']
	def options=dev?.ac
	List status=options?.find{ (String)it.key==statusIdOrName || (String)it.value==statusIdOrName }?.collect{ [id: (String)it.key, name: it.value] }

	if(status && (Integer)status.size()!=0){
		sendLocationEvent(name:'hsmSetArm', value: status[0].id)
	}else{
		error "Error setting HSM status. Status '$statusIdOrName' does not exist.", rtD
	}
	return 0L
}

private Long vcmd_sendEmail(Map rtD, device, List params){
	def data=[
		i: (String)rtD.id,
		n: (String)app.label,
		t: (String)params[0],
		s: (String)params[1],
		m: (String)params[2]
	]

	Map requestParams=[
		uri: "https://api.webcore.co/email/send/${(String)rtD.locationId}".toString(),
		query: null,
		headers: [:], //(auth ? [Authorization: auth]:[:]),
		requestContentType: "application/json",
		body: data
	]
	String msg='Unknown error'

	try{
		asynchttpPost('ahttpRequestHandler', requestParams, [command:'sendEmail', em: data])
		return 24000L
	}catch (all){
		error "Error sending email to ${data.t}: $msg", rtD
	}
	return 0L
}

private Long vcmd_noop(Map rtD, device, List params){
	return 0L
}

private Long vcmd_wait(Map rtD, device, List params){
	return (Long)cast(rtD, params[0], 'long')
}

private Long vcmd_waitRandom(Map rtD, device, List params){
	Long min=(Long)cast(rtD, params[0], 'long')
	Long max=(Long)cast(rtD, params[1], 'long')
	if(max<min){
		Long v=max
		max=min
		min=v
	}
	return min+(Integer)Math.round(1.0D*(max-min)*Math.random())
}

private Long vcmd_waitForTime(Map rtD, device, List params){
	Long time
	time=(Long)cast(rtD, (Long)cast(rtD, params[0], 'time'), 'datetime', 'time')
	Long rightNow=now()
	time=pushTimeAhead(time, rightNow)
	return time-rightNow
}

private Long vcmd_waitForDateTime(Map rtD, device, List params){
	Long time=(Long)cast(rtD, params[0], 'datetime')
	Long rightNow=now()
	return (time>rightNow)? time-rightNow:0L
}

private Long vcmd_setSwitch(Map rtD, device, List params){
	if((Boolean)cast(rtD, params[0], 'boolean')){
		executePhysicalCommand(rtD, device, 'on')
	}else{
		executePhysicalCommand(rtD, device, 'off')
	}
	return 0L
}

private Long vcmd_toggle(Map rtD, device, List params){
	if((String)getDeviceAttributeValue(rtD, device, 'switch')=='off'){
		executePhysicalCommand(rtD, device, 'on')
	}else{
		executePhysicalCommand(rtD, device, 'off')
	}
	return 0L
}

private Long vcmd_toggleRandom(Map rtD, device, List params){
	Integer probability=(Integer)cast(rtD, (Integer)params.size()==1 ? params[0]:50, 'integer')
	if(probability<=0)probability=50
	if(Math.round(100.0D*Math.random())<=probability){
		executePhysicalCommand(rtD, device, 'on')
	}else{
		executePhysicalCommand(rtD, device, 'off')
	}
	return 0L
}

private Long vcmd_toggleLevel(Map rtD, device, List params){
	Integer level=params[0]
	if((Integer)getDeviceAttributeValue(rtD, device, 'level')==level){
		executePhysicalCommand(rtD, device, 'setLevel', 0)
	}else{
		executePhysicalCommand(rtD, device, 'setLevel', level)
	}
	return 0L
}

private Long do_adjustLevel(Map rtD, device, List params, String attr, String attr1, Integer val=null, Boolean big=false){
	Integer arg=val!=null ? val : (Integer)cast(rtD, params[0], 'integer')
	String mstate=(Integer)params.size()>1 ? (String)params[1]:(String)null
	Long delay=(Integer)params.size()>2 ? (Long)params[2]:0L
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	arg=arg+(Integer)cast(rtD, getDeviceAttributeValue(rtD, device, attr), 'integer')
	Integer low=big ? 1000:0
	Integer hi=big ? 30000:100
	arg=(arg<low)? low:((arg>hi)? hi:arg)
	executePhysicalCommand(rtD, device, attr1, arg, delay)
	return 0L
}

private Long vcmd_adjustLevel(Map rtD, device, List params){
	return do_adjustLevel(rtD, device, params, 'level', 'setLevel')
}

private Long vcmd_adjustInfraredLevel(Map rtD, device, List params){
	return do_adjustLevel(rtD, device, params, 'infraredLevel', 'setInfraredLevel')
}

private Long vcmd_adjustSaturation(Map rtD, device, List params){
	return do_adjustLevel(rtD, device, params, 'saturation', 'setSaturation')
}

private Long vcmd_adjustHue(Map rtD, device, List params){
	Integer hue=(Integer)cast(rtD, Math.round(params[0]/3.6D), 'integer')
	return do_adjustLevel(rtD, device, params, 'hue', 'setHue', hue)
}

private Long vcmd_adjustColorTemperature(Map rtD, device, List params){
	return do_adjustLevel(rtD, device, params, 'colorTemperature', 'setColorTemperature', null, true)
}

private Long do_fadeLevel(Map rtD, device, List params, String attr, String attr1, val=null, val1=null, Boolean big=false){
	Integer startlevel
	Integer endLevel
	if(val==null){
		startLevel=(params[0]!=null)? (Integer)cast(rtD, params[0], 'integer'):(Integer)cast(rtD, getDeviceAttributeValue(rtD, device, attr), 'integer')
		endLevel=(Integer)cast(rtD, params[1], 'integer')
	}else{
		startlevel=(Integer)val
		endLevel=(Integer)val1
	}
	Long duration=(Long)cast(rtD, params[2], 'long')
	String mstate=(Integer)params.size()>3 ? (String)params[3]:(String)null
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	Integer low=big ? 1000:0
	Integer hi=big ? 30000:100
	startLevel=(startLevel<low)? low:((startLevel>hi)? hi:startLevel)
	endLevel=(endLevel<low)? low:((endLevel>hi)? hi:endLevel)
	return vcmd_internal_fade(rtD, device, attr1, startLevel, endLevel, duration)
}

private Long vcmd_fadeLevel(Map rtD, device, List params){
	return do_fadeLevel(rtD, device, params, 'level', 'setLevel')
}

private Long vcmd_fadeInfraredLevel(Map rtD, device, List params){
	return do_fadeLevel(rtD, device, params, 'infraredLevel', 'setInfraredLevel')
}

private Long vcmd_fadeSaturation(Map rtD, device, List params){
	return do_fadeLevel(rtD, device, params, 'saturation', 'setSaturation')
}

private Long vcmd_fadeHue(Map rtD, device, List params){
	Integer startLevel=(params[0]!=null)? (Integer)cast(rtD, Math.round((Integer)params[0]/3.6D), 'integer'):(Integer)cast(rtD, getDeviceAttributeValue(rtD, device, 'hue'), 'integer')
	Integer endLevel=(Integer)cast(rtD, Math.round((Integer)params[1]/3.6D), 'integer')
	return do_fadeLevel(rtD, device, params, 'hue', 'setHue', startLevel, endLevel)
}

private Long vcmd_fadeColorTemperature(Map rtD, device, List params){
	return do_fadeLevel(rtD, device, params, 'colorTemperature', 'setColorTemperature', null, null, true)
}

private Long vcmd_internal_fade(Map rtD, device, String command, Integer startLevel, Integer endLevel, Long duration){
	Long minInterval=5000L
	if(duration<=5000L){
		minInterval=500L
	}else if(duration<=10000L){
		minInterval=1000L
	}else if(duration<=30000L){
		minInterval=3000L
	}else{
		minInterval=5000L
	}
	if((startLevel==endLevel)|| (duration<=500L)){
		//if the fade is too fast, or not changing anything, give it up and go to the end level directly
		executePhysicalCommand(rtD, device, command, endLevel)
		return 0L
	}
	Integer delta=endLevel-startLevel
	//the max number of steps we can do
	Integer steps=delta>0 ? delta:-delta
	//figure out the interval
	Long interval=Math.round(duration/steps)
	if(interval<minInterval){
		//intervals too small, adjust to do one change per 500ms
		steps=Math.floor(1.0D*duration/minInterval)
		interval=Math.round(1.0D*duration/steps)
	}
	String scheduleDevice=hashId(device.id)
	Integer oldLevel=startLevel
	executePhysicalCommand(rtD, device, command, startLevel)
	for(Integer i=1; i<=steps; i++){
		Integer newLevel=Math.round(startLevel+delta*i/steps*1.0D)
		if(oldLevel!=newLevel){
			executePhysicalCommand(rtD, device, command, newLevel, i*interval, scheduleDevice, true)
		}
		oldLevel=newLevel
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtD, device, command, endLevel, duration+99L, scheduleDevice, true)
	return duration+105L
}

private Long vcmd_emulatedFlash(Map rtD, device, List params){
	vcmd_flash(rtD, device, params)
}

private Long vcmd_flash(Map rtD, device, List params){
	Long onDuration=(Long)cast(rtD, params[0], 'long')
	Long offDuration=(Long)cast(rtD, params[1], 'long')
	Integer cycles=(Integer)cast(rtD, params[2], 'integer')
	String mstate=(Integer)params.size()>3 ? (String)params[3]:(String)null
	String currentState=(String)getDeviceAttributeValue(rtD, device, 'switch')
	if(mstate!=(String)null && (currentState!=mstate)){
		return 0L
	}
	Long duration=Math.round((onDuration+offDuration)*cycles*1.0D)
	if(duration<=500L){
		//if the flash is too fast, ignore it
		return 0L
	}
	//initialize parameters
	String firstCommand=currentState=='on' ? 'off':'on'
	Long firstDuration=firstCommand=='on' ? onDuration:offDuration
	String secondCommand=firstCommand=='on' ? 'off':'on'
	Long secondDuration=firstCommand=='on' ? offDuration:onDuration
	String scheduleDevice=hashId(device.id)
	Long dur=0L
	for(Integer i=1; i<=cycles; i++){
		executePhysicalCommand(rtD, device, firstCommand, [], dur, scheduleDevice, true)
		dur += firstDuration
		executePhysicalCommand(rtD, device, secondCommand, [], dur, scheduleDevice, true)
		dur += secondDuration
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtD, device, currentState, [], dur+100L, scheduleDevice, true)
	return dur+105L
}

private Long vcmd_flashLevel(Map rtD, device, List params){
	Integer level1=(Integer)cast(rtD, params[0], 'integer')
	Long duration1=(Long)cast(rtD, params[1], 'long')
	Integer level2=(Integer)cast(rtD, params[2], 'integer')
	Long duration2=(Long)cast(rtD, params[3], 'long')
	Integer cycles=(Integer)cast(rtD, params[4], 'integer')
	String mstate=(Integer)params.size()>5 ? (String)params[5]:(String)null
	String currentState=(String)getDeviceAttributeValue(rtD, device, 'switch')
	if(mstate!=(String)null && (currentState!=mstate)){
		return 0L
	}
	Integer currentLevel=(Integer)getDeviceAttributeValue(rtD, device, 'level')
	Long duration=Math.round((duration1+duration2)*cycles*1.0D)
	if(duration<=500L){
		//if the flash is too fast, ignore it
		return 0L
	}
	String scheduleDevice=hashId(device.id)
	Long dur=0L
	for(Integer i=1; i<=cycles; i++){
		executePhysicalCommand(rtD, device, 'setLevel', [level1], dur, scheduleDevice, true)
		dur += duration1
		executePhysicalCommand(rtD, device, 'setLevel', [level2], dur, scheduleDevice, true)
		dur += duration2
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtD, device, 'setLevel', [currentLevel], dur+100L, scheduleDevice, true)
	executePhysicalCommand(rtD, device, currentState, [], dur+101L, scheduleDevice, true)
	return dur+105L
}

private Long vcmd_flashColor(Map rtD, device, List params){
	def color1=getColor(rtD, (String)params[0])
	Long duration1=(Long)cast(rtD, params[1], 'long')
	def color2=getColor(rtD, (String)params[2])
	Long duration2=(Long)cast(rtD, params[3], 'long')
	Integer cycles=(Integer)cast(rtD, params[4], 'integer')
	String mstate=(Integer)params.size()>5 ? (String)params[5]:(String)null
	String currentState=(String)getDeviceAttributeValue(rtD, device, 'switch')
	if(mstate!=(String)null && (currentState!=mstate)){
		return 0L
	}
	Long duration=Math.round((duration1+duration2)*cycles*1.0D)
	if(duration<=500L){
		//if the flash is too fast, ignore it
		return 0L
	}
	String scheduleDevice=hashId(device.id)
	Long dur=0
	for(Integer i=1; i<=cycles; i++){
		executePhysicalCommand(rtD, device, 'setColor', [color1], dur, scheduleDevice, true)
		dur += duration1
		executePhysicalCommand(rtD, device, 'setColor', [color2], dur, scheduleDevice, true)
		dur += duration2
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtD, device, currentState, [], dur+99L, scheduleDevice, true)
	return dur+105L
}

private Long vcmd_sendNotification(Map rtD, device, List params){
	def message="Hubitat does not support sendNotification "+params[0]
	Map a=log(message, rtD, -2, "Err", 'warn', true)
	//sendNotificationEvent(message)
	return 0L
}

private Long vcmd_sendPushNotification(Map rtD, device, List params){
	String message=(String)params[0]
	if(rtD.initPush==null){
		rtD.pushDev=(List)parent.getPushDev()
		rtD.initPush=true
	}
	List t0=(List)rtD.pushDev
	try{
		t0*.deviceNotification(message)
	}catch (all){
		message="Default push device not set properly in webCoRE "+params[0]
		error message, rtD
	}
	return 0L
}

private Long vcmd_sendSMSNotification(Map rtD, device, List params){
	String message=(String)params[0]
	String msg="HE SMS notifications are being removed, please convert to a notification device "+params[0]
	warn msg, rtD
	return 0L
}

private Long vcmd_sendNotificationToContacts(Map rtD, device, List params){
	// Contact Book has been disabled and we're falling back onto PUSH notifications, if the option is on
	String message=(String)params[0]
	def save=!!params[2]
	return vcmd_sendPushNotification(rtD, devices, [message, save])
}

private static Map parseVariableName(String name){
	Map result=[
		name: name,
		index: (String)null
	]
	if(name!=(String)null && !name.startsWith('$') && name.endsWith(']')){
		List parts=name.replace(']', '').tokenize('[')
		if((Integer)parts.size()==2){
			result=[
				name: (String)parts[0],
				index: (String)parts[1]
			]
		}
	}
	return result
}

private Long vcmd_setVariable(Map rtD, device, List params){
	String name=(String)params[0]
	def value=params[1]
	if((Boolean)rtD.eric) myDetail rtD, "setVariable $name  $value"
	def t0=setVariable(rtD, name, value)
	return 0L
}

private Long vcmd_executePiston(Map rtD, device, List params){
	String selfId=(String)rtD.id
	String pistonId=(String)params[0]
	List arguments=(params[1] instanceof List ? (List)params[1]:params[1].toString().tokenize(',')).unique()
	Boolean wait=((Integer)params.size()>2)? (Boolean)cast(rtD, params[2], 'boolean'):false
	String description="webCoRE: Piston ${(String)app.label} requested execution of piston $pistonId".toString()
	Map data=[:]
	for (String argument in arguments){
		if(argument)data[argument]=getVariable(rtD, argument).v
	}
	if(wait){
		wait=(Boolean)parent.executePiston(pistonId, data, selfId)
		pauseExecution(100L)
	}
	if(!wait){
		sendLocationEvent(name:pistonId, value:selfId, isStateChange:true, displayed:false, linkText:description, descriptionText:description, data:data)
	}
	return 0L
}

private Long vcmd_pausePiston(Map rtD, device, List params){
	String selfId=(String)rtD.id
	String pistonId=(String)params[0]
	if(!(Boolean)parent.pausePiston(pistonId)){
		message="Piston not found "+pistonId
		error message, rtD
	}
	return 0L
}

private Long vcmd_resumePiston(Map rtD, device, List params){
	String selfId=(String)rtD.id
	String pistonId=(String)params[0]
	if(!(Boolean)parent.resumePiston(pistonId)){
		message="Piston not found "+pistonId
		error message, rtD
	}
	return 0L
}

private Long vcmd_executeRule(Map rtD, device, List params){
	String ruleId=(String)params[0]
	String action=(String)params[1]
	Boolean wait=((Integer)params.size()>2)? (Boolean)cast(rtD, params[2], 'boolean'):false
	def rules=RMUtils.getRuleList()
	List myRule=[]
	rules.each{rule->
		List t0=rule.find{ hashId((String)it.key)==ruleId }.collect{(String)it.key}
		myRule += t0
	}

	if(myRule){
		String ruleAction
		if(action=="Run")ruleAction="runRuleAct"
		if(action=="Stop")ruleAction="stopRuleAct"
		if(action=="Pause")ruleAction="pauseRule"
		if(action=="Resume")ruleAction="resumeRule"
		if(action=="Evaluate")ruleAction="runRule"
		if(action=="Set Boolean True")ruleAction="setRuleBooleanTrue"
		if(action=="Set Boolean False")ruleAction="setRuleBooleanFalse"
		RMUtils.sendAction(myRule, ruleAction, (String)app.label)
	}else{
		String message="Rule not found "+ruleId
		error message, rtD
	}
	return 0L
}

private Long vcmd_setHSLColor(Map rtD, device, List params){
	Integer hue=(Integer)cast(rtD, Math.round(params[0]/3.6D), 'integer')
	Integer saturation=params[1]
	Integer level=params[2]
	def color=[
		hue: hue,
		saturation: saturation,
		level: level
	]
	String mstate=(Integer)params.size()>3 ? (String)params[3]:(String)null
	Long delay=(Integer)params.size()>4 ? (Long)params[4]:0L
	if(mstate!=(String)null && (String)getDeviceAttributeValue(rtD, device, 'switch')!=mstate){
		return 0L
	}
	executePhysicalCommand(rtD, device, 'setColor', color, delay)
	return 0L
}

private Long vcmd_wolRequest(Map rtD, device, List params){
	String mac=params[0]
	String secureCode=params[1]
	mac=mac.replace(":", '').replace("-", '').replace(".", '').replace(" ", '').toLowerCase()

	sendHubCommand(HubActionClass().newInstance(
		"wake on lan $mac".toString(),
		HubProtocolClass().LAN,
		null,
		secureCode ? [secureCode: secureCode]:[:]
	))
	return 0L
}

private Long vcmd_iftttMaker(Map rtD, device, List params){
	String key
	if(rtD.settings==null){
		error "no settings", rtD
	}else{
		key=((String)rtD.settings.ifttt_url ?: '').trim().replace('https://', '').replace('http://', '').replace('maker.ifttt.com/use/', '')
	}
	if(!key){
		error "Failed to send IFTTT event, because the IFTTT integration is not properly set up. Please visit Settings in your webCoRE dashboard and configure the IFTTT integration.", rtD
		return 0L
	}
	String event=params[0]
	def value1=(Integer)params.size()>1 ? params[1]:''
	def value2=(Integer)params.size()>2 ? params[2]:''
	def value3=(Integer)params.size()>3 ? params[3]:''
	def body=[:]
	if(value1)body.value1=value1
	if(value2)body.value2=value2
	if(value3)body.value3=value3
	def data=[
		t: event,
		p1:value1,
		p2:value2,
		p3:value3
	]
	def requestParams=[
		uri: "https://maker.ifttt.com/trigger/${java.net.URLEncoder.encode(event, "UTF-8")}/with/key/"+key,
		requestContentType: "application/json",
		body: body
	]
	try{
		asynchttpPost('ahttpRequestHandler', requestParams, [command:'iftttMaker', em: data])
		return 24000L
	}catch (all){
		error "Error iftttMaker to ${requestParams.uri}  ${data.t}: ${data..p1}, ${data.p2}, ${data.p3}", rtD
	}
	return 0L
}

private Long vcmd_httpRequest(Map rtD, device, List params){
	String uri=((String)params[0]).replace(" ", "%20")
	if(!uri){
		error "Error executing external web request: no URI", rtD
		return 0L
	}
	String method=(String)params[1]
	Boolean useQueryString=method=='GET' || method=='DELETE' || method=='HEAD'
	String requestBodyType=(String)params[2]
	def variables=params[3]
	String auth=(String)null
	def requestBody=null
	String contentType=(String)null
	if((Integer)params.size()==5){
		auth=(String)params[4]
	}else if((Integer)params.size()==7){
		requestBody=(String)params[4]
		contentType=(String)params[5] ?: 'text/plain'
		auth=(String)params[6]
	}
	String protocol="https"
	String requestContentType=(method=="GET" || requestBodyType=="FORM")? "application/x-www-form-urlencoded":(requestBodyType=="JSON")? "application/json":contentType
	String userPart=''
	List uriParts=uri.split("://").toList()
	if((Integer)uriParts.size()>2){
		warn "Invalid URI for web request: $uri", rtD
		return 0L
	}
	if((Integer)uriParts.size()==2){
		//remove the httpX:// from the uri
		protocol=(String)uriParts[0].toLowerCase()
		uri=(String)uriParts[1]
	}
	//support for user:pass@IP
	if(uri.contains('@')){
		List uriSubParts=uri.split('@').toList()
		userPart=(String)uriSubParts[0]+'@'
		uri=(String)uriSubParts[1]
	}
	def data=null
	if(requestBodyType=='CUSTOM' && !useQueryString){
		data=requestBody
	}else if(variables instanceof List){
		for(String variable in variables.findAll{ !!it }){
			data=data ?: [:]
			data[variable]=getVariable(rtD, variable).v
		}
	}
	try{
		Map requestParams=[
			uri: protocol+'://'+userPart+uri,
			query: useQueryString ? data:null,
			headers: (auth ? ((auth.startsWith('{') && auth.endsWith('}'))? (new groovy.json.JsonSlurper().parseText(auth)):[Authorization: auth]):[:]),
			requestContentType: requestContentType,
			body: !useQueryString ? data:null
		]
		String func=''
		switch(method){
			case 'GET':
				func='asynchttpGet'
				break
			case 'POST':
				func='asynchttpPost'
				break
			case 'PUT':
				func='asynchttpPut'
				break
			case 'DELETE':
				func='asynchttpDelete'
				break
			case 'HEAD':
				func='asynchttpHead'
				break
		}
		if((Integer)rtD.logging>2)debug "Sending ${func} web request to: $uri", rtD
		if(func!=''){
			"$func"('ahttpRequestHandler', requestParams, [command:'httpRequest'])
			return 24000L
		}
	}catch (all){
		error "Error executing external web request: ", rtD, -2, all
	}
	return 0L
}

public void ahttpRequestHandler(resp, Map callbackData){
	Boolean binary=false
	def t0=resp.getHeaders()
	String t1=t0!=null && (String)t0."Content-Type" ? (String)t0."Content-Type" : (String)null
	String mediaType=t1 ? (String)(t1.toLowerCase()?.tokenize(';')[0]):(String)null
	switch (mediaType){
		case 'image/jpeg':
		case 'image/png':
		case 'image/gif':
			binary=true
	}
	def data
	def json
	Map setRtData=[mediaData:null, mediaType:null, mediaUrl:null]
	String callBackC=(String)callbackData?.command
	Boolean success=false
	if(callBackC=='sendEmail'){
		String msg='Unknown error'
		def em=callbackData?.em
		if(resp.status==200){
			data=resp.getJson()
			if(data!=null){
				if((String)data.result=='OK'){
					success=true
				}else{
					msg=((String)data.result).replace('ERROR ', '')
				}
			}
		}
		if(!success){
			error "Error sending email to ${em?.t}: ${msg}", [:]
		}
	}else if(callBackC=='httpRequest'){
		if(resp.status==204){
			mediaType=''
		}else{
			if(resp.status>=200 && resp.status<300 && resp.data){
				if(!binary){
					def theData
					try{
						theData=resp.getData()
						data=theData
						if(data!=null && data instanceof Map){
						}else{
							try{
								json=resp.getJson()
								if(json!=null) data=json
							}catch (all1){
								json=[:]
							}
						}
					}catch (all){
						data=resp.data
					}
				}else{
					if(resp.data!=null && resp.data instanceof java.io.ByteArrayInputStream){
						setRtData.mediaType=mediaType
						setRtData.mediaData=resp.data.getBytes()
					}
				}
			}else{
				if(resp.hasError()){
					error "http Response Status: ${resp.status}  error Message: ${resp.getErrorMessage()}", [:]
				}
			}
		}
	}else if(callBackC=='iftttMaker'){
		def em=callbackData?.em
		if(resp.status>=200 && resp.status<300) success=true
		if(!success){
			String eMsg=''
			if(resp.hasError())eMsg="http Response Status: ${resp.status}  error Message: ${resp.getErrorMessage()}".toString()
			error "Error iftttMaker to ${em?.t}: ${em?.p1}, ${em?.p2}, ${em?.p3}  ".toString()+eMsg, [:]
		}
	}

	handleEvents([date: new Date(), device: location, name:'wc_async_reply', value: callBackC, contentType: mediaType, responseData: data, jsonData:[:], responseCode: resp.status, setRtData: setRtData])
}

private Long vcmd_writeToFuelStream(Map rtD, device, List params){
	String canister=(String)params[0]
	String name=(String)params[1]
	def data=params[2]
	def source=params[3]

	if(rtD.useLocalFuelStreams==null){
		rtD.useLocalFuelStreams=(Boolean)parent.useLocalFuelStreams()
	}

	if((Boolean)rtD.useLocalFuelStreams && name!=(String)null){
		Map req=[
			c: canister,
			n: name,
			s: source,
			d: data,
			i: (String)rtD.instanceId
		]
		parent.writeToFuelStream(req)
	}else{
		log.error "Fuel stream app is not installed. Install it to write to local fuel streams "+name, rtD
	}
	return 0L
}

private Long vcmd_storeMedia(Map rtD, device, List params){
	if(!rtD.mediaData || !rtD.mediaType || !(rtD.mediaData)|| ((Integer)rtD.mediaData.size()<=0)){
		error 'No media is available to store; operation aborted.', rtD
		return 0L
	}
	String data=new String(rtD.mediaData, 'ISO_8859_1')
	Map requestParams=[
		uri: "https://api-${rtD.region}-${rtD.instanceId[32]}.webcore.co:9247".toString(),
		path: "/media/store",
		headers: [
			'ST':(String)rtD.instanceId,
			'media-type':rtD.mediaType
		],
		body: data,
		requestContentType: rtD.mediaType
	]
	asynchttpPut(asyncHttpRequestHandler, requestParams, [command:'storeMedia'])
	return 24000L
}

public void asyncHttpRequestHandler(response, Map callbackData){
	def mediaId
	def mediaUrl
	if(response.status==200){
		def data=response.getJson()
		if((String)data.result=='OK' && data.url){
			mediaId=data.id
			mediaUrl=data.url
		}else{
			if(data.message){
				error "Error storing media item: $response.data.message", [:]
			}
		}
	}
	handleEvents([date: new Date(), device: location, name:'wc_async_reply', value: (String)callbackData?.command, responseCode: response.status, setRtData: [mediaId: mediaId, mediaUrl: mediaUrl]])
}

private Long vcmd_saveStateLocally(Map rtD, device, List params, Boolean global=false){
	List attributes=((String)cast(rtD, params[0], 'string')).tokenize(',')
	String canister=((Integer)params.size()>1 ? (String)cast(rtD, params[1], 'string')+':':'')+hashId(device.id)+':'
	Boolean overwrite=!((Integer)params.size()>2 ? (Boolean)cast(rtD, params[2], 'boolean'):false)
	for (String attr in attributes){
		String n=canister+attr
		if(global && !(Boolean)rtD.initGStore){
			rtD.globalStore=(Map)parent.getGStore()
			rtD.initGStore=true
		}
		if(overwrite || (global ? (rtD.globalStore[n]==null):(rtD.store[n]==null))){
			def value=getDeviceAttributeValue(rtD, device, attr)
			if(attr=='hue')value=value*3.6D
			if(global){
				rtD.globalStore[n]=value
				Map cache=rtD.gvStoreCache ?: [:]
				cache[n]=value
				rtD.gvStoreCache=cache
			}else{
				rtD.store[n]=value
			}
		}
	}
	return 0L
}

private Long vcmd_saveStateGlobally(Map rtD, device, List params){
	return vcmd_saveStateLocally(rtD, device, params, true)
}

private Long vcmd_loadStateLocally(Map rtD, device, List params, Boolean global=false){
	List attributes=((String)cast(rtD, params[0], 'string')).tokenize(',')
	String canister=((Integer)params.size()>1 ? (String)cast(rtD, params[1], 'string')+':':'')+hashId(device.id)+':'
	Boolean empty=(Integer)params.size()>2 ? (Boolean)cast(rtD, params[2], 'boolean'):false
	for (String attr in attributes){
		String n=canister+attr
		if(global && !(Boolean)rtD.initGStore){
			rtD.globalStore=(Map)parent.getGStore()
			rtD.initGStore=true
		}
		def value=global ? rtD.globalStore[n]:rtD.store[n]
		if(attr=='hue')value=(Double)cast(rtD, value, 'decimal')/3.6D
		if(empty){
			if(global){
				rtD.globalStore.remove(n)
				Map cache=rtD.gvStoreCache ?: [:]
				cache[n]=null
				rtD.gvStoreCache=cache
			}else rtD.store.remove(n)
		}
		if(value==null)continue
		String exactCommand
		String fuzzyCommand
		for (command in PhysicalCommands()){
			if((String)command.value.a==attr){
				if(command.value.v==null){
					fuzzyCommand=(String)command.key
				}else{
					if((String)command.value.v==value){
						exactCommand=(String)command.key
						break
					}
				}
			}
		}
		String t0="Restoring attribute '$attr' to value '$value' using command".toString()
		if(exactCommand!=(String)null){
			if((Integer)rtD.logging>2)debug "${t0} $exactCommand()", rtD
			executePhysicalCommand(rtD, device, exactCommand)
			continue
		}
		if(fuzzyCommand!=(String)null){
			if((Integer)rtD.logging>2)debug "${t0} $fuzzyCommand($value)", rtD
			executePhysicalCommand(rtD, device, fuzzyCommand, value)
			continue
		}
		warn "Could not find a command to set attribute '$attr' to value '$value'", rtD
	}
	return 0L
}

private Long vcmd_loadStateGlobally(Map rtD, device, List params){
	return vcmd_loadStateLocally(rtD, device, params, true)
}

private Long vcmd_parseJson(Map rtD, device, List params){
	String data=params[0]
	try{
		if(data.startsWith('{') && data.endsWith('}')){
			rtD.json=(LinkedHashMap)new groovy.json.JsonSlurper().parseText(data)
		}else if(data.startsWith('[') && data.endsWith(']')){
			rtD.json=(List)new groovy.json.JsonSlurper().parseText(data)
		}else{
			rtD.json=[:]
		}
	}catch (all){
		error "Error parsing JSON data $data", rtD
	}
	return 0L
}

private Long vcmd_cancelTasks(Map rtD, device, List params){
	rtD.cancelations.all=true
	return 0L
}

private Boolean evaluateFollowedByCondition(Map rtD, Map condition, String collection, Boolean async, ladderUpdated){
	Boolean result=evaluateCondition(rtD, condition, collection, async)
}

private Boolean evaluateConditions(Map rtD, Map conditions, String collection, Boolean async){
	if((Boolean)rtD.eric) myDetail rtD, "evaluateConditions", 1
	Long t=now()
	Map msg=timer '', rtD
	//override condition id
	Integer c=(Integer)rtD.stack.c
	Integer myC=conditions.$!=null ? (Integer)conditions.$:0
	rtD.stack.c=myC
	Boolean not= collection=='c' ? !!conditions.n:!!conditions.rn
	String grouping= collection=='c' ? (String)conditions.o:(String)conditions.rop
	Boolean value= grouping=='or' ? false:true


	if(grouping=='followed by' && collection=='c'){
		if((Integer)rtD.fastForwardTo==0 || (Integer)rtD.fastForwardTo==myC){
			//we're dealing with a followed by condition
			Integer ladderIndex=(Integer)cast(rtD, rtD.cache["c:fbi:${myC}".toString()], 'integer')
			Long ladderUpdated=(Long)cast(rtD, rtD.cache["c:fbt:${myC}".toString()], 'datetime')
			Integer steps=conditions[collection] ? (Integer)conditions[collection].size():0
			if(ladderIndex>=steps){
				value=false
			}else{
				def condition=conditions[collection][ladderIndex]
				Long duration=0L
				if(ladderIndex){
					Map tv=(Map)evaluateOperand(rtD, null, (Map)condition.wd)
					duration=(Long)evaluateExpression(rtD, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v
				}
				if(ladderUpdated && duration!=0L && (ladderUpdated+duration<now())){
					//time has expired
					value=((String)condition.wt=='n')
					if(!value){
						if((Integer)rtD.logging>2)debug "Conditional ladder step failed due to a timeout", rtD
					}
				}else{
					value=evaluateCondition(rtD, condition, collection, async)
					if((String)condition.wt=='n'){
						if(value){
							value=false
						}else{
							value=null
						}
					}
					//we allow loose matches to work even if other events happen
					if((String)condition.wt=='l' && !value)value=null
				}
				if(value){
					//successful step, move on
					ladderIndex += 1
					ladderUpdated=now()
					cancelStatementSchedules(rtD, myC)
					if((Integer)rtD.logging>2)debug "Condition group #${myC} made progress up the ladder; currently at step $ladderIndex of $steps", rtD
					if(ladderIndex<steps){
						//delay decision, there are more steps to go through
						value=null
						condition=conditions[collection][ladderIndex]
						Map tv=(Map)evaluateOperand(rtD, null, (Map)condition.wd)
						duration=(Long)evaluateExpression(rtD, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v
						requestWakeUp(rtD, conditions, conditions, duration)
					}
				}
			}

			switch (value){
			case null:
				//we need to exit time events set to work out the timeouts...
				if((Integer)rtD.fastForwardTo==myC)rtD.terminated=true
				break
			case true:
			case false:
				//ladder either collapsed or finished, reset data
				ladderIndex=0
				ladderUpdated=0L
				cancelStatementSchedules(rtD, myC)
				break
			}
			if((Integer)rtD.fastForwardTo==myC)rtD.fastForwardTo=0
			rtD.cache["c:fbi:${myC}".toString()]=ladderIndex
			rtD.cache["c:fbt:${myC}".toString()]=ladderUpdated
		}
	}else{
		for(condition in conditions[collection]){
			Boolean res=evaluateCondition(rtD, condition, collection, async)
			value= grouping=='or' ? value||res : value&&res
			//cto == disable condition traversal optimizations
			if((Integer)rtD.fastForwardTo==0 && !rtD.piston.o?.cto && ((value && grouping=='or') || (!value && grouping=='and')))break
		}
	}
	Boolean result=false
	if(value!=null){
		result=not ? !value:value
	}
	if(value!=null && myC!=0){
		String mC="c:${myC}".toString()
		if((Integer)rtD.fastForwardTo==0)tracePoint(rtD, mC, Math.round(1.0D*now()-t), result)
		Boolean oldResult=!!(Boolean)rtD.cache[mC]
		rtD.conditionStateChanged=(oldResult!=result)
		if((Boolean)rtD.conditionStateChanged){
			//condition change, perform Task Cancellation Policy TCP
			cancelConditionSchedules(rtD, myC)
		}
		rtD.cache[mC]=result
		//true/false actions
		if(collection=='c'){
			if((result || (Integer)rtD.fastForwardTo!=0) && conditions.ts!=null && (List)(conditions.ts).length)Boolean a=executeStatements(rtD, (List)conditions.ts, async)
			if((!result || (Integer)rtD.fastForwardTo!=0) && conditions.fs!=null && (List)(conditions.fs).length)Boolean a=executeStatements(rtD, (List)conditions.fs, async)
		}
		if((Integer)rtD.fastForwardTo==0){
			msg.m="Condition group #${myC} evaluated $result (state ${(Boolean)rtD.conditionStateChanged ? 'changed':'did not change'})".toString()
			if((Integer)rtD.logging>2)debug msg, rtD
		}
	}
	//restore condition id
	rtD.stack.c=c
	if((Boolean)rtD.eric) myDetail rtD, "evaluateConditions result: $result", -1
	return result
}

private evaluateOperand(Map rtD, Map node, Map operand, index=null, Boolean trigger=false, Boolean nextMidnight=false){
	if((Boolean)rtD.eric) myDetail rtD, "evaluateOperand $operand", 1
	List values=[]
	//older pistons don't have the 'to' operand (time offset), we're simulating an empty one
	if(!operand)operand=[t:'c']
	String ovt=(String)operand.vt
	String nodeI="${node?.$}:$index:0".toString()
	switch ((String)operand.t){
	case '': //optional, nothing selected
		values=[[i:nodeI, v:[t:ovt, v:null]]]
		break
	case 'p': //physical device
		String operA=(String)operand.a
		Map attribute=operA ? Attributes()[operA]:[:]
		for(String deviceId in expandDeviceList(rtD, (List)operand.d)){
			Map value=[i: deviceId+':'+operA, v:getDeviceAttribute(rtD, deviceId, operA, operand.i, trigger)+(ovt ? [vt:ovt]:[:])+(attribute && attribute.p ? [p:operand.p]:[:])]
			updateCache(rtD, value)
			Boolean a=values.push(value)
		}
		if((Integer)values.size()>1 && !((String)operand.g in ['any', 'all'])){
			//if we have multiple values and a grouping other than any or all we need to apply that function
			try{
				values=[[i:nodeI, v:(Map)"func_${(String)operand.g}"(rtD, values*.v)+(ovt ? [vt:ovt]:[:])]]
			}catch(all){
				error "Error applying grouping method ${(String)operand.g}", rtD
			}
		}
		break
	case 'd': //devices
		List deviceIds=[]
		for (String d in expandDeviceList(rtD, (List)operand.d)){
			if(getDevice(rtD, d))Boolean a=deviceIds.push(d)
		}
		values=[[i:"${node?.$}:d".toString(), v:[t:'device', v:deviceIds.unique()]]]
		break
	case 'v': //virtual devices
		String rEN=(String)rtD.event.name
		String evntVal="${rtD.event.value}".toString()
		String nodeV="${node?.$}:v".toString()
		switch ((String)operand.v){
		case 'mode':
		case 'alarmSystemStatus':
			values=[[i:nodeV, v:getDeviceAttribute(rtD, (String)rtD.locationId, (String)operand.v)]]
			break
		case 'alarmSystemAlert':
			String valStr=evntVal+(rEN=='hsmAlert' && evntVal=="rule" ? ", ${(String)rtD.event.descriptionText}".toString():'')
			values=[[i:nodeV, v:[t:'string', v:(rEN=='hsmAlert' ? valStr:(String)null)]]]
			break
		case 'alarmSystemEvent':
			values=[[i:nodeV, v:[t:'string', v:(rEN=='hsmSetArm' ? evntVal:(String)null)]]]
			break
		case 'alarmSystemRule':
			values=[[i:nodeV, v:[t:'string', v:(rEN=='hsmRules' ? evntVal:(String)null)]]]
			break
		case 'powerSource':
			values=[[i:nodeV, v:[t:'enum', v:rtD.powerSource]]]
			break
		case 'time':
		case 'date':
		case 'datetime':
			values=[[i:nodeV, v:[t:(String)operand.v, v:(Long)cast(rtD, now(), (String)operand.v, 'long')]]]
			break
		case 'routine':
			values=[[i:nodeV, v:[t:'string', v:(rEN=='routineExecuted' ? hashId(evntVal):(String)null)]]]
			break
		case 'systemStart':
			values=[[i:nodeV, v:[t:'string', v:(rEN=='systemStart' ? evntVal:(String)null)]]]
			break
		case 'tile':
			values=[[i:nodeV, v:[t:'string', v:(rEN==(String)operand.v ? evntVal:(String)null)]]]
			break
		case 'ifttt':
			values=[[i:nodeV, v:[t:'string', v:(rEN==('ifttt.'+evntVal)? evntVal:(String)null)]]]
			break
		case 'email':
			values=[[i:nodeV, v:[t:'email', v:(rEN==('email.'+evntVal)? evntVal:(String)null)]]]
			break
		}
		break
	case 's': //preset
		Boolean time=false
		switch (ovt){
		case 'time':
			time=true
		case 'datetime':
			Long v=0L
			switch ((String)operand.s){
			case 'midnight': v=nextMidnight ? getNextMidnightTime():getMidnightTime(); break
			case 'sunrise': v=adjustPreset(rtD, "Sunrise", nextMidnight); break
			case 'noon': v=adjustPreset(rtD, "Noon", nextMidnight); break
			case 'sunset': v=adjustPreset(rtD, "Sunset", nextMidnight); break
			}
			if(time)v=(Long)cast(rtD, v, ovt, 'datetime')
			values=[[i:nodeI, v:[t:ovt, v:v]]]
			break
		default:
			values=[[i:nodeI, v:[t:ovt, v:operand.s]]]
			break
		}
		break
	case 'x': //variable
		if(ovt=='device' && operand.x instanceof List){
			//we could have multiple devices selected
			List sum=[]
			for (String x in (List)operand.x){
				Map var=getVariable(rtD, x)
				if(var.v instanceof List){
					sum += (List)var.v
				}else{
					Boolean a=sum.push(var.v)
				}
			}
			values=[[i:nodeI, v:[t:'device', v:sum]+(ovt ? [vt:ovt]:[:])]]
		}else{
			values=[[i:nodeI, v:getVariable(rtD, (String)operand.x+((String)operand.xi!=(String)null ? '['+(String)operand.xi+']':''))+(ovt ? [vt:ovt]:[:])]]
		}
		break
	case 'c': //constant
		switch (ovt){
		case 'time':
			Long offset=(operand.c instanceof Integer)? operand.c:(Integer)cast(rtD, operand.c, 'integer')
			values=[[i:nodeI, v:[t:'time', v:(offset%1440L)*60000L]]]
			break
		case 'date':
		case 'datetime':
			values=[[i:nodeI, v:[t:ovt, v:operand.c]]]
			break
		}
		if((Integer)values.size()!=0)break
	case 'e': //expression
		values=[[i:nodeI, v: [:]+evaluateExpression(rtD, (Map)operand.exp)+(ovt ? [vt:ovt]:[:])]]
		break
	case 'u': //expression
		values=[[i:nodeI, v:getArgument(rtD, (String)operand.u)]]
		break
	}
	def ret=values
	if(node==null){ // return a Map instead of a List
		if(values.length)ret=values[0].v
		else ret=[t:'dynamic', v:null]
	}
	if((Boolean)rtD.eric) myDetail rtD, "evaluateOperand $operand result: $ret", -1
	return ret
}

private Long adjustPreset(Map rtD, String ttyp, nextMidnight){
	Long t2=(Long)"get${ttyp}Time"(rtD)
	Long tnow=now()
	if(tnow<t2)return t2
// this deals with both DST skew and sunrise/sunset skews
	Long t0=(Long)"getNext${ttyp}Time"(rtD)
	Long t1=Math.round(t0-86400000.0D)
	Long delta=Math.round((Integer)location.timeZone.getOffset(t1)- (Integer)location.timeZone.getOffset(t0)*1.0D)
	Long t4=Math.round(t1+delta*1.0D)
	if(tnow>t4)return t4
	return t2
}

private Map evaluateScalarOperand(Map rtD, Map node, Map operand, index=null, String dataType='string'){
	Map value=(Map)evaluateOperand(rtD, null, operand, index)
	return [t:dataType, v:cast(rtD, (value ? value.v:''), dataType)]
}

private Boolean evaluateCondition(Map rtD, Map condition, String collection, Boolean async){
	if((Boolean)rtD.eric) myDetail rtD, "evaluateCondition $condition", 1
	Long t=now()
	Map msg=timer '', rtD
	//override condition id
	Integer c=(Integer)rtD.stack.c
	Integer conditionNum=condition.$!=null ? (Integer)condition.$:0
	rtD.stack.c=conditionNum
	Boolean not=false
	Boolean oldResult=!!rtD.cache["c:${conditionNum}".toString()]
	Boolean result=false
	if((String)condition.t=='group'){
		Boolean tt1=evaluateConditions(rtD, condition, collection, async)
		if((Boolean)rtD.eric) myDetail rtD, "evaluateCondition $condition result: $tt1", -1
		return tt1
	}else{
		not=!!condition.n
		Map comparison=Comparisons().triggers[(String)condition.co]
		Boolean trigger=comparison!=null
		if(!trigger)comparison=Comparisons().conditions[(String)condition.co]
		rtD.wakingUp=(String)rtD.event.name=='time' && rtD.event.schedule!=null && (Integer)rtD.event.schedule.s==conditionNum
		if((Integer)rtD.fastForwardTo!=0 || comparison!=null){
			if((Integer)rtD.fastForwardTo==0 || ((Integer)rtD.fastForwardTo==-9 /*initial run*/)){
				Integer paramCount=comparison.p!=null ? (Integer)comparison.p:0
				Map lo=null
				Map ro=null
				Map ro2=null
				for(Integer i=0; i<=paramCount; i++){
					Map operand=(i==0 ? (Map)condition.lo:(i==1 ? (Map)condition.ro : (Map)condition.ro2))
					//parse the operand
					List values=(List)evaluateOperand(rtD, condition, operand, i, trigger)
					switch (i){
					case 0:
						lo=[operand:operand, values:values]
						break
					case 1:
						ro=[operand:operand, values:values]
						break
					case 2:
						ro2=[operand:operand, values:values]
						break
					}
				}

				//we now have all the operands, their values, and the comparison, let's get to work
				Boolean t_and_compt=(trigger && comparison.t!=null)
				Map options=[
					//we ask for matching/non-matching devices if the user requested it or if the trigger is timed
					//setting matches to true will force the condition group to evaluate all members (disables evaluation optimizations)
					matches: lo.operand.dm!=null || lo.operand.dn!=null || t_and_compt,
					forceAll: t_and_compt
				]
				Map to=(comparison.t!=null || (ro!=null && (String)lo.operand.t=='v' && (String)lo.operand.v=='time' && (String)ro.operand.t!='c')) && condition.to!=null ? [operand: (Map)condition.to, values: (Map)evaluateOperand(rtD, null, (Map)condition.to)]:null
				Map to2=ro2!=null && (String)lo.operand.t=='v' && (String)lo.operand.v=='time' && (String)ro2.operand.t!='c' && condition.to2!=null ? [operand: (Map)condition.to2, values: (Map)evaluateOperand(rtD, null, (Map)condition.to2)]:null
				result=evaluateComparison(rtD, (String)condition.co, lo, ro, ro2, to, to2, options)
				//save new values to cache
				if(lo)for (Map value in (List)lo.values)updateCache(rtD, value)
				if(ro)for (Map value in (List)ro.values)updateCache(rtD, value)
				if(ro2)for (Map value in (List)ro2.values)updateCache(rtD, value)
				if((Integer)rtD.fastForwardTo==0)tracePoint(rtD, "c:${conditionNum}".toString(), Math.round(1.0D*now()-t), result)
				if(lo.operand.dm!=null && options.devices!=null)def m=setVariable(rtD, (String)lo.operand.dm, options.devices.matched!=null ? (List)options.devices.matched:[])
				if(lo.operand.dn!=null && options.devices!=null)def n=setVariable(rtD, (String)lo.operand.dn, options.devices.unmatched!=null ? (List)options.devices.unmatched:[])
				//do the stay logic here
				if(t_and_compt && (Integer)rtD.fastForwardTo==0){
					//timed trigger
					if(to!=null){
						def tvalue=to.operand && to.values ? (Map)to.values+[f: to.operand.f]:null
						if(tvalue!=null){
							Long delay=(Long)evaluateExpression(rtD, [t:'duration', v:tvalue.v, vt:(String)tvalue.vt], 'long').v
							if((String)lo.operand.t=='p' && (String)lo.operand.g=='any' && (Integer)lo.values.size()>1){

								List schedules
								Map t0=getCachedMaps()
								if(t0!=null)schedules=[]+(List)t0.schedules
								else schedules=(Boolean)rtD.pep ? (List)atomicState.schedules:(List)state.schedules
								for (value in (List)lo.values){
									String dev=(String)value.v?.d
									if(dev in (List)options.devices.matched){
										//schedule one device schedule
										if(!schedules.find{ (Integer)it.s==conditionNum && (String)it.d==dev }){
											//schedule a wake up if there's none, otherwise just move on
											if((Integer)rtD.logging>2)debug "Adding a timed trigger schedule for device $dev for condition ${conditionNum}", rtD
											requestWakeUp(rtD, condition, condition, delay, dev)
										}
									}else{
										//cancel that one device schedule
										if((Integer)rtD.logging>2)debug "Cancelling any timed trigger schedules for device $dev for condition ${conditionNum}", rtD
										cancelStatementSchedules(rtD, conditionNum, dev)
									}
								}
							}else{
								if(result){
								//if we find the comparison true, set a timer if we haven't already

									List schedules
									Map t0=getCachedMaps()
									if(t0!=null)schedules=[]+(List)t0.schedules
									else schedules=(Boolean)rtD.pep ? (List)atomicState.schedules:(List)state.schedules
									if(!schedules.find{ ((Integer)it.s==conditionNum)}){
										if((Integer)rtD.logging>2)debug "Adding a timed trigger schedule for condition ${conditionNum}", rtD
										requestWakeUp(rtD, condition, condition, delay)
									}
								}else{
									if((Integer)rtD.logging>2)debug "Cancelling any timed trigger schedules for condition ${conditionNum}", rtD
									cancelStatementSchedules(rtD, conditionNum)
								}
							}
						}
					}
					result=false
				}
				result=not ? !result:result
			}else if((String)rtD.event.name=='time' && (Integer)rtD.fastForwardTo==conditionNum){
				rtD.fastForwardTo=0
				rtD.resumed=true
				result=!not
			}else{
				result=oldResult
			}
		}
	}
	rtD.wakingUp=false
	rtD.conditionStateChanged=oldResult!=result
	if((Boolean)rtD.conditionStateChanged){
		//condition change, perform Task Cancellation Policy TCP
		cancelConditionSchedules(rtD, conditionNum)
	}
	rtD.cache["c:${conditionNum}".toString()]=result
	//true/false actions
	if((result || (Integer)rtD.fastForwardTo!=0) && condition.ts!=null && ((List)condition.ts).length!=0)Boolean a=executeStatements(rtD, (List)condition.ts, async)
	if((!result || (Integer)rtD.fastForwardTo!=0) && condition.fs!=null && ((List)condition.fs).length!=0)Boolean a=executeStatements(rtD, (List)condition.fs, async)
	//restore condition id
	rtD.stack.c=c
	if((Integer)rtD.fastForwardTo==0){
		msg.m="Condition #${conditionNum} evaluated $result"
		if((Integer)rtD.logging>2)debug msg, rtD
	}
	if((Integer)rtD.fastForwardTo<=0 && (Boolean)condition.s && (String)condition.t=='condition' && condition.lo!=null && (String)condition.lo.t=='v'){
		switch ((String)condition.lo.v){
		case 'time':
		case 'date':
		case 'datetime':
			scheduleTimeCondition(rtD, condition)
			break
		}
	}
	if((Boolean)rtD.eric) myDetail rtD, "evaluateCondition $condition result: $result", -1
	return result
}

private void updateCache(Map rtD, Map value){
	Map oldValue=rtD.cache[(String)value.i]
	if(oldValue==null || ((String)oldValue.t!=(String)value.v.t) || (oldValue.v!=value.v.v)){
		rtD.newCache[(String)value.i]=(Map)value.v+[s: now()]
	}
}

private Boolean evaluateComparison(Map rtD, String comparison, Map lo, Map ro=null, Map ro2=null, Map to=null, Map to2=null, options=[:]){
	if((Boolean)rtD.eric) myDetail rtD, "evaluateComparison $comparison", 1
	String fn="comp_"+comparison
	Boolean result= (String)lo.operand.g=='any' ? false:true
	if(options?.matches){
		options.devices=[matched: [], unmatched: []]
	}
	//if multiple left values, go through each
	Map tvalue=to && to.operand && to.values ? (Map)to.values+[f: to.operand.f]:null
	Map tvalue2=to2 && to2.operand && to2.values ? (Map)to2.values:null
	for(Map value in (List)lo.values){
		Boolean res=false
		if(value && value.v && (!value.v.x || options.forceAll)){
			try{
				//physical support
				//value.p=lo.operand.p
				if(value && ((String)value.v.t=='device'))value.v=evaluateExpression(rtD, (Map)value.v, 'dynamic')
				if(!ro){
					Map msg=timer '', rtD
					if(comparison=='event_occurs' && (String)lo.operand.t=='v' && (String)rtD.event.name==(String)lo.operand.v)res=true
					else res=(Boolean)"$fn"(rtD, value, null, null, tvalue, tvalue2)
					msg.m="Comparison (${value?.v?.t}) ${value?.v?.v} $comparison = $res"
					if((Integer)rtD.logging>2)debug msg, rtD
				}else{
					Boolean rres
					res= (String)ro.operand.g=='any' ? false:true
					//if multiple right values, go through each
					for (Map rvalue in (List)ro.values){
						if(rvalue && ((String)rvalue.v.t=='device'))rvalue.v=evaluateExpression(rtD, (Map)rvalue.v, 'dynamic')
						if(!ro2){
							Map msg=timer '', rtD
							rres=(Boolean)"$fn"(rtD, value, rvalue, null, tvalue, tvalue2)
							msg.m="Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} = $rres"
							if((Integer)rtD.logging>2)debug msg, rtD
						}else{
							rres=(String)ro2.operand.g=='any' ? false:true
							//if multiple right2 values, go through each
							for (Map r2value in (List)ro2.values){
								if(r2value && ((String)r2value.v.t=='device'))r2value.v=evaluateExpression(rtD, (Map)r2value.v, 'dynamic')
								Map msg=timer '', rtD
//if((Boolean)rtD.eric) myDetail rtD, "$fn $value   $rvalue    $r2value    $tvalue   $tvalue2", 1
								Boolean r2res=(Boolean)"$fn"(rtD, value, rvalue, r2value, tvalue, tvalue2)
//if((Boolean)rtD.eric) myDetail rtD, "$r2res  ${myObj(value?.v?.v)}    ${myObj(rvalue?.v?.v)}  $fn $value   $rvalue    $r2value    $tvalue   $tvalue2", -1
								msg.m="Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} .. (${r2value?.v?.t}) ${r2value?.v?.v} = $r2res"
								if((Integer)rtD.logging>2)debug msg, rtD
								rres= (String)ro2.operand.g=='any' ? rres||r2res : rres&&r2res
								if(((String)ro2.operand.g=='any' && rres) || ((String)ro2.operand.g!='any' && !rres))break
							}
						}
						res=((String)ro.operand.g=='any' ? res||rres : res&&rres)
						if(((String)ro.operand.g=='any'&& res) || ((String)ro.operand.g!='any' && !res))break
					}
				}
			}catch(all){
				error "Error calling comparison $fn:", rtD, -2, all
				res=false
			}

			if(res && ((String)lo.operand.t=='v')){
				switch ((String)lo.operand.v){
				case 'time':
				case 'date':
				case 'datetime':
					Boolean pass=(checkTimeRestrictions(rtD, lo.operand, now(), 5, 1)==0L)
					if((Integer)rtD.logging>2)debug "Time restriction check ${pass ? 'passed':'failed'}", rtD
					if(!pass)res=false
				}
			}
		}
		result= (String)lo.operand.g=='any' ? result||res : result&&res
		if(options?.matches && (String)value.v.d){
			if(res){
				Boolean a=((List)options.devices.matched).push((String)value.v.d)
			}else{
				Boolean a=((List)options.devices.unmatched).push((String)value.v.d)
			}
		}
		if((String)lo.operand.g=='any' && res && !(options?.matches)){
			//logical OR if we're using the ANY keyword
			break
		}
		if((String)lo.operand.g=='all' && !result && !(options?.matches)){
			//logical AND if we're using the ALL keyword
			break
		}
	}
	if((Boolean)rtD.eric) myDetail rtD, "evaluateComparison $comparison result: $result", -1
	return result
}

private void cancelStatementSchedules(Map rtD, Integer statementId, String data=(String)null){
	//cancel all schedules that are pending for statement statementId
	Boolean found=false
	for(Map item in (List)rtD.cancelations.statements){
		found=(statementId==(Integer)item.id && (!data || data==(String)item.data))
		if(found)break
	}
	if((Integer)rtD.logging>2)debug "Cancelling statement #${statementId}'s schedules...", rtD
	if(!found)Boolean a=((List)rtD.cancelations.statements).push([id: statementId, data: data])
}

private void cancelConditionSchedules(Map rtD, Integer conditionId){
	//cancel all schedules that are pending for condition conditionId
	if((Integer)rtD.logging>2)debug "Cancelling condition #${conditionId}'s schedules...", rtD
	if(!(conditionId in (List)rtD.cancelations.conditions)){
		Boolean a=((List)rtD.cancelations.conditions).push(conditionId)
	}
}

private static Boolean matchDeviceSubIndex(list, deviceSubIndex){
	return true
}

private static Boolean matchDeviceInteraction(String option, Boolean isPhysical){
	return !((option=='p' && !isPhysical) || (option=='s' && isPhysical))
}

private List listPreviousStates(device, String attribute, Long threshold, Boolean excludeLast){
	List result=[]
	List events=device.events([all: true, max: 100]).findAll{(String)it.name==attribute}
	//if we got any events, let's go through them
	//if we need to exclude last event, we start at the second event, as the first one is the event that triggered this function. The attribute's value has to be different from the current one to qualify for quiet
	if((Integer)events.size()!=0){
		Long thresholdTime=now()-threshold
		Long endTime=now()
		for(Integer i=0; i<(Integer)events.size(); i++){
			Long startTime=(Long)events[i].date.getTime()
			Long duration=endTime-startTime
			if(duration>=1000L && (i>0 || !excludeLast)){
				Boolean a=result.push([value: events[i].value, startTime: startTime, duration: duration])
			}
			if(startTime<thresholdTime)
				break
			endTime=startTime
		}
	}else{
		def currentState=device.currentState(attribute, true)
		if(currentState){
			Long startTime=(Long)currentState.getDate().getTime()
			Boolean a=result.push([value: currentState.value, startTime: startTime, duration: now()- startTime])
		}
	}
	return result
}

private static Map valueCacheChanged(Map rtD, Map comparisonValue){
	Map oldValue=rtD.cache[(String)comparisonValue.i]
	def newValue=comparisonValue.v
	if(!(oldValue instanceof Map))oldValue=null
	return (oldValue!=null && ((String)oldValue.t!=(String)newValue.t || "${oldValue.v}"!="${newValue.v}")) ? [i: (String)comparisonValue.i, v: oldValue] : null
}

private Boolean valueWas(Map rtD, Map comparisonValue, Map rightValue, Map rightValue2, Map timeValue, String func){
	if(comparisonValue==null || comparisonValue.v==null || !(String)comparisonValue.v.d || !(String)comparisonValue.v.a || timeValue==null || !timeValue.v || !(String)timeValue.vt){
		return false
	}
	def device=getDevice(rtD, (String)comparisonValue.v.d)
	if(device==null)return false
	String attribute=(String)comparisonValue.v.a
	Long threshold=(Long)evaluateExpression(rtD, [t:'duration', v:timeValue.v, vt:(String)timeValue.vt], 'long').v

	List states=listPreviousStates(device, attribute, threshold, rtD.event.device?.id==device.id && (String)rtD.event.name==attribute)
	Boolean result=true
	Long duration=0
	for (stte in states){
		if(!("comp_$func"(rtD, [i: (String)comparisonValue.i, v: [t: (String)comparisonValue.v.t, v: cast(rtD, stte.value, (String)comparisonValue.v.t)]], rightValue, rightValue2, timeValue)))break
		duration += stte.duration
	}
	if(duration==0L)return false
	result=((String)timeValue.f=='l')? duration<threshold:duration>=threshold
	if((Integer)rtD.logging>2)debug "Duration ${duration}ms for ${func.replace('is_', 'was_')} ${timeValue.f=='l' ? '<':'>='} ${threshold}ms threshold = ${result}", rtD
	return result
}

private Boolean valueChanged(Map rtD, Map comparisonValue, Map timeValue){
	if(comparisonValue==null || comparisonValue.v==null || !(String)comparisonValue.v.d || !(String)comparisonValue.v.a || timeValue==null || !timeValue.v || !(String)timeValue.vt){
		return false
	}
	def device=getDevice(rtD, (String)comparisonValue.v.d)
	if(device==null)return false
	String attribute=(String)comparisonValue.v.a
	Long threshold=(Long)evaluateExpression(rtD, [t:'duration', v:timeValue.v, vt:(String)timeValue.vt], 'long').v

	List states=listPreviousStates(device, attribute, threshold, false)
	if((Integer)states.size()==0)return false
	def value=states[0].value
	for (tstate in states){
		if(tstate.value!=value)return true
	}
	return false
}

private static Boolean match(String string, String pattern){
	if((Integer)pattern.size()>2 && pattern.startsWith('/') && pattern.endsWith('/')){
		pattern=~pattern.substring(1, (Integer)pattern.size()- 1)
		return !!(string =~ pattern)
	}
	return string.contains(pattern)
}

//comparison low level functions
private Boolean comp_is					(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ((String)evaluateExpression(rtD, (Map)lv.v, 'string').v==(String)evaluateExpression(rtD, (Map)rv.v, 'string').v)|| (lv.v.n && ((String)cast(rtD, lv.v.n, 'string')==(String)cast(rtD, rv.v.v, 'string')))}
private Boolean comp_is_not				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_equal_to			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ String dt=(((String)lv?.v?.t=='decimal')|| ((String)rv?.v?.t=='decimal')? 'decimal':(((String)lv?.v?.t=='integer')|| ((String)rv?.v?.t=='integer')? 'integer':'dynamic')); return evaluateExpression(rtD, (Map)lv.v, dt).v==evaluateExpression(rtD, (Map)rv.v, dt).v }
private Boolean comp_is_not_equal_to			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_equal_to(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_different_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not_equal_to(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_less_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtD, (Map)lv.v, 'decimal').v<(Double)evaluateExpression(rtD, (Map)rv.v, 'decimal').v }
private Boolean comp_is_less_than_or_equal_to		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtD, (Map)lv.v, 'decimal').v<=(Double)evaluateExpression(rtD, (Map)rv.v, 'decimal').v }
private Boolean comp_is_greater_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtD, (Map)lv.v, 'decimal').v>(Double)evaluateExpression(rtD, (Map)rv.v, 'decimal').v }
private Boolean comp_is_greater_than_or_equal_to	(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtD, (Map)lv.v, 'decimal').v>=(Double)evaluateExpression(rtD, (Map)rv.v, 'decimal').v }
private Boolean comp_is_even				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ((Integer)evaluateExpression(rtD, (Map)lv.v, 'integer').v).mod(2)==0 }
private Boolean comp_is_odd				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ((Integer)evaluateExpression(rtD, (Map)lv.v, 'integer').v).mod(2)!=0 }
private Boolean comp_is_true				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Boolean)evaluateExpression(rtD, (Map)lv.v, 'boolean').v }
private Boolean comp_is_false				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !(Boolean)evaluateExpression(rtD, (Map)lv.v, 'boolean').v }
private Boolean comp_is_inside_of_range			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Double v=(Double)evaluateExpression(rtD, (Map)lv.v, 'decimal').v; Double v1=(Double)evaluateExpression(rtD, (Map)rv.v, 'decimal').v; Double v2=(Double)evaluateExpression(rtD, (Map)rv2.v, 'decimal').v; return (v1<v2) ? (v>=v1 && v<=v2):(v>=v2 && v<=v1)}
private Boolean comp_is_outside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_inside_of_range(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_any_of				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ String v=(String)evaluateExpression(rtD, (Map)lv.v, 'string').v; for (String vi in ((String)rv.v.v).tokenize(',')){ if(v==(String)evaluateExpression(rtD, [t: (String)rv.v.t, v: "$vi".toString().trim(), i: rv.v.i, a: rv.v.a, vt: (String)rv.v.vt], 'string').v)return true }; return false}
private Boolean comp_is_not_any_of			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_any_of(rtD, lv, rv, rv2, tv, tv2)}

private Boolean comp_was				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is')}
private Boolean comp_was_not				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_not')}
private Boolean comp_was_equal_to			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_equal_to')}
private Boolean comp_was_not_equal_to			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_not_equal_to')}
private Boolean comp_was_different_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_different_than')}
private Boolean comp_was_less_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_less_than')}
private Boolean comp_was_less_than_or_equal_to		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_less_than_or_equal_to')}
private Boolean comp_was_greater_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_greater_than')}
private Boolean comp_was_greater_than_or_equal_to	(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_greater_than_or_equal_to')}
private Boolean comp_was_even				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_even')}
private Boolean comp_was_odd				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_odd')}
private Boolean comp_was_true				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_true')}
private Boolean comp_was_false				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_false')}
private Boolean comp_was_inside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_inside_of_range')}
private Boolean comp_was_outside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_outside_of_range')}
private Boolean comp_was_any_of				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_any_of')}
private Boolean comp_was_not_any_of			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtD, lv, rv, rv2, tv, 'is_not_any_of')}

private Boolean comp_changed				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, tv2=null){ return valueChanged(rtD, lv, tv)}
private Boolean comp_did_not_change			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !valueChanged(rtD, lv, tv)}

private static Boolean comp_is_any			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return true }
private Boolean comp_is_before				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Long offset1=tv ? (Long)evaluateExpression(rtD, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v:0L; return cast(rtD, (Long)evaluateExpression(rtD, (Map)lv.v, 'datetime').v+2000L, (String)lv.v.t)<cast(rtD, (Long)evaluateExpression(rtD, (Map)rv.v, 'datetime').v+offset1, (String)lv.v.t)}
private Boolean comp_is_after				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Long offset1=tv ? (Long)evaluateExpression(rtD, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v:0L; return cast(rtD, (Long)evaluateExpression(rtD, (Map)lv.v, 'datetime').v+2000L, (String)lv.v.t)>=cast(rtD, (Long)evaluateExpression(rtD, (Map)rv.v, 'datetime').v+offset1, (String)lv.v.t)}
private Boolean comp_is_between				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Long offset1=tv ? (Long)evaluateExpression(rtD, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v:0L; Long offset2=tv2 ? (Long)evaluateExpression(rtD, [t:'duration', v:tv2.v, vt:(String)tv2.vt], 'long').v:0L; Long v=(Long)cast(rtD, (Long)evaluateExpression(rtD, (Map)lv.v, 'datetime').v+2000L, (String)lv.v.t); Long v1=(Long)cast(rtD, (Long)evaluateExpression(rtD, (Map)rv.v, 'datetime').v+offset1, (String)lv.v.t); Long v2=(Long)cast(rtD, (Long)evaluateExpression(rtD, (Map)rv2.v, 'datetime').v+offset2, (String)lv.v.t); return v1<v2 ? v>=v1 && v<v2 : v<v2 || v>=v1}
private Boolean comp_is_not_between			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_between(rtD, lv, rv, rv2, tv, tv2)}

/*triggers*/
private Boolean comp_gets				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (String)cast(rtD, lv.v.v, 'string')==(String)cast(rtD, rv.v.v, 'string') && matchDeviceSubIndex(lv.v.i, rtD.currentEvent.index)}
private Boolean comp_executes				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_arrives				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (String)rtD.event.name=='email' && match(rtD.event?.jsonData?.from ?: '', (String)evaluateExpression(rtD, (Map)rv.v, 'string').v) && match(rtD.event?.jsonData?.message ?: '', (String)evaluateExpression(rtD, (Map)rv2.v, 'string').v)}
private static Boolean comp_event_occurs		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return false }
private static Boolean comp_happens_daily_at		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Boolean)rtD.wakingUp }
private static Boolean comp_changes			(Map rtD, Map lv, rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueCacheChanged(rtD, lv)!=null && matchDeviceInteraction((String)lv.v.p, (Boolean)rtD.currentEvent.physical)}
private static Boolean comp_changes_to			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueCacheChanged(rtD, lv)!=null && ("${lv.v.v}"=="${rv.v.v}") && matchDeviceInteraction((String)lv.v.p, (Boolean)rtD.currentEvent.physical)}
private static Boolean comp_receives			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ("${lv.v.v}"=="${rv.v.v}") && matchDeviceInteraction((String)lv.v.p, (Boolean)rtD.currentEvent.physical)}
private static Boolean comp_changes_away_from		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ("${oldValue.v.v}"=="${rv.v.v}") && matchDeviceInteraction((String)lv.v.p, (Boolean)rtD.currentEvent.physical)}
private Boolean comp_drops			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')>(Double)cast(rtD, lv.v.v, 'decimal'))}
private Boolean comp_does_not_drop			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_drops(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_drops_below			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')>=(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')<(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_drops_to_or_below			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')>(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')<=(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_rises				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')<(Double)cast(rtD, lv.v.v, 'decimal'))}
private Boolean comp_does_not_rise			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_rises(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_rises_above			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')<=(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')>(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_rises_to_or_above			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')<(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')>=(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_remains_below			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')<(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')<(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_remains_below_or_equal_to		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')<=(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')<=(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_remains_above			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')>(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')>(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_remains_above_or_equal_to		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && ((Double)cast(rtD, oldValue.v.v, 'decimal')>=(Double)cast(rtD, rv.v.v, 'decimal')) && ((Double)cast(rtD, lv.v.v, 'decimal')>=(Double)cast(rtD, rv.v.v, 'decimal'))}
private Boolean comp_enters_range			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); if(oldValue==null)return false; Double ov=(Double)cast(rtD, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtD, lv.v.v, 'decimal'); Double v1=(Double)cast(rtD, rv.v.v, 'decimal'); Double v2=(Double)cast(rtD, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return ((ov<v1)|| (ov>v2)) && ((v>=v1) && (v<=v2))}
private Boolean comp_exits_range			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); if(oldValue==null)return false; Double ov=(Double)cast(rtD, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtD, lv.v.v, 'decimal'); Double v1=(Double)cast(rtD, rv.v.v, 'decimal'); Double v2=(Double)cast(rtD, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return ((ov>=v1) && (ov<=v2)) && ((v<v1)|| (v>v2))}
private Boolean comp_remains_inside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); if(oldValue==null)return false; Double ov=(Double)cast(rtD, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtD, lv.v.v, 'decimal'); Double v1=(Double)cast(rtD, rv.v.v, 'decimal'); Double v2=(Double)cast(rtD, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return (ov>=v1) && (ov<=v2) && (v>=v1) && (v<=v2)}
private Boolean comp_remains_outside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); if(oldValue==null)return false; Double ov=(Double)cast(rtD, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtD, lv.v.v, 'decimal'); Double v1=(Double)cast(rtD, rv.v.v, 'decimal'); Double v2=(Double)cast(rtD, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return ((ov<v1)|| (ov>v2)) && ((v<v1) || (v>v2))}
private Boolean comp_becomes_even			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && (((Integer)cast(rtD, oldValue.v.v, 'integer')).mod(2)!=0) && (((Integer)cast(rtD, lv.v.v, 'integer')).mod(2)==0)}
private Boolean comp_becomes_odd			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && (((Integer)cast(rtD, oldValue.v.v, 'integer')).mod(2)==0) && (((Integer)cast(rtD, lv.v.v, 'integer')).mod(2)!=0)}
private Boolean comp_remains_even			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && (((Integer)cast(rtD, oldValue.v.v, 'integer')).mod(2)==0) && (((Integer)cast(rtD, lv.v.v, 'integer')).mod(2)==0)}
private Boolean comp_remains_odd			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && (((Integer)cast(rtD, oldValue.v.v, 'integer')).mod(2)!=0) && (((Integer)cast(rtD, lv.v.v, 'integer')).mod(2)!=0)}

private Boolean comp_changes_to_any_of			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueCacheChanged(rtD, lv)!=null && comp_is_any_of(rtD, lv, rv, rv2, tv, tv2) && matchDeviceInteraction((String)lv.v.p, (Boolean)rtD.currentEvent.physical)}
private Boolean comp_changes_away_from_any_of		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Map oldValue=valueCacheChanged(rtD, lv); return oldValue!=null && comp_is_any_of(rtD, oldValue, rv, rv2) && matchDeviceInteraction((String)lv.v.p, (Boolean)rtD.currentEvent.physical)}

private Boolean comp_stays				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is(rtD, lv, rv, rv2, tv, tv2)}
private static Boolean comp_stays_unchanged		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return true }
private Boolean comp_stays_not				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_equal_to			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_equal_to(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_different_than		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_different_than(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_less_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_less_than(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_less_than_or_equal_to	(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_less_than_or_equal_to(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_greater_than			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_greater_than(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_greater_than_or_equal_to	(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_greater_than_or_equal_to(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_even				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_even(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_odd				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_odd(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_true				(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_true(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_false			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_false(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_inside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_inside_of_range(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_outside_of_range		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_outside_of_range(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_any_of			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_any_of(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_away_from			(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not_equal_to(rtD, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_away_from_any_of		(Map rtD, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not_any_of(rtD, lv, rv, rv2, tv, tv2)}

private void traverseStatements(node, closure, parentNode=null, Map data=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(def item in (List)node){
			if(!item.di){
				Boolean lastTimer=(data!=null && (Boolean)data.timer)
				if(data!=null && ((String)item.t=='every')){
					data.timer=true
				}
				traverseStatements(item, closure, parentNode, data)
				if(data!=null){
					data.timer=lastTimer
				}
			}
		}
		return
	}

	//got a statement
	if(closure instanceof Closure){
		closure(node, parentNode, data)
	}

	//if the statements has substatements, go through them
	if(node.s instanceof List){
		traverseStatements((List)node.s, closure, node, data)
	}
	if(node.e instanceof List){
		traverseStatements((List)node.e, closure, node, data)
	}
}

private void traverseEvents(node, closure, parentNode=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(item in (List)node){
			traverseEvents(item, closure, parentNode)
		}
		return
	}
	//got a condition
	if((closure instanceof Closure)){
		closure(node, parentNode)
	}
}

private void traverseConditions(node, closure, parentNode=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(item in (List)node){
			traverseConditions(item, closure, parentNode)
		}
		return
	}
	//got a condition
	if(node.t=='condition' && (closure instanceof Closure)){
		closure(node, parentNode)
	}
	//if the statements has substatements, go through them
	if(node.c instanceof List){
		if(closure instanceof Closure)closure(node, parentNode)
		traverseConditions((List)node.c, closure, node)
	}
}

private void traverseRestrictions(node, closure, parentNode=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(item in (List)node){
			traverseRestrictions(item, closure, parentNode)
		}
		return
	}
	//got a restriction
	if(node.t=='restriction' && (closure instanceof Closure)){
		closure(node, parentNode)
	}
	//if the statements has substatements, go through them
	if(node.r instanceof List){
		if(closure instanceof Closure)closure(node, parentNode)
		traverseRestrictions(node.r, closure, node)
	}
}

private void traverseExpressions(node, closure, param, parentNode=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(item in (List)node){
			traverseExpressions(item, closure, param, parentNode)
		}
		return
	}
	//got a statement
	if(closure instanceof Closure){
		closure(node, parentNode, param)
	}
	//if the statements has substatements, go through them
	if(node.i instanceof List){
		traverseExpressions((List)node.i, closure, param, node)
	}
}

private void updateDeviceList(Map rtD, List deviceIdList, Boolean clearCache=true){
	app.updateSetting('dev', [type: /*isHubitat()?*/ 'capability'/*:'capability.device'*/, value: deviceIdList.unique()])// settings update do not happen till next execution
	if(clearCache)clearMyCache("updateDeviceList")
}

private void subscribeAll(Map rtD, Boolean doit=true){
	if(eric())log.debug "subscribeAll $doit"
	try{
	if(!rtD){ log.error "no rtD subscribeAll"; return }
	Map ss=[
		events: 0,
		controls: 0,
		devices: 0,
	]
	Map statementData=[timer:false]
	Map msg=timer "Finished subscribing", rtD, -1
	if(doit){
		unsubscribe()
		rtD.devices=[:]
		app.clearSetting('dev')
		if((Integer)rtD.logging>1)trace "Subscribing to devices...", rtD, 1
	}
	Map devices=[:]
	Map rawDevices=[:]
	Map subscriptions=[:]
	Boolean hasTriggers=false
	Boolean downgradeTriggers=false
	//traverse all statements
	def expressionTraverser
	def operandTraverser
	def eventTraverser
	def conditionTraverser
	def restrictionTraverser
	def statementTraverser
	expressionTraverser={ Map expression, parentExpression, String comparisonType ->
		String subsId=(String)null
		String deviceId=(String)null
		String attribute=(String)null
		String exprID=(String)expression.id
		if((String)expression.t=='device' && exprID){
			if(exprId==(String)rtD.oldLocationId)exprId=(String)rtD.locationId
			devices[exprID]=[c: (comparisonType ? 1:0)+(devices[exprID]?.c ? (Integer)devices[exprID].c:0)]
			deviceId=exprID
			attribute=(String)expression.a
			subsId=deviceId+attribute
		}
		String exprX=(String)expression.x
		if((String)expression.t=='variable' && exprX && exprX.startsWith('@')){
			subsId=exprX
			deviceId=(String)rtD.locationId
			attribute=(String)rtD.instanceId+'.'+exprX
		}
		if(subsId!=(String)null && deviceId!=(String)null){
			String ct=(String)subscriptions[subsId]?.t ?: (String)null
			if((ct=='trigger')|| (comparisonType=='trigger')){
				ct='trigger'
			}else{
				ct=ct ?: comparisonType
			}
			//if((Boolean)rtD.eric) myDetail rtD, "subscribeAll condition is $condition"
			subscriptions[subsId]=[d: deviceId, a: attribute, t: ct, c: (subscriptions[subsId] ? (List)subscriptions[subsId].c:[])+[condition]]
			if(deviceId!=(String)rtD.locationId && deviceId.startsWith(':')){
				rawDevices[deviceId]=getDevice(rtD, deviceId)
				devices[deviceId]=[c: (comparisonType ? 1:0)+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
			}
		}
	}
	operandTraverser={ Map node, Map operand, value, String comparisonType ->
		if(!operand)return
		switch ((String)operand.t){
		case 'p': //physical device
			for(String deviceId in expandDeviceList(rtD, (List)operand.d, true)){
				if(deviceId==(String)rtD.oldLocationId)deviceId=(String)rtD.locationId
				devices[deviceId]=[c: (comparisonType ? 1:0)+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
				String attribute=(String)operand.a
				String subsId=deviceId+attribute
				//if we have any trigger, it takes precedence over anything else
				String ct=(String)subscriptions[subsId]?.t ?: (String)null
				String oct=ct
				String msgVal
				Boolean allowAval
				List avals=[]
				if((ct=='trigger')|| (comparisonType=='trigger')){
					ct='trigger'

					allowAval= subscriptions[subsId]?.allowA==null ? true : (Boolean)subscriptions[subsId].allowA
					String attrVal=(String)null
					if(allowAval && ((String)node.co=='receives' || (String)node.co=='gets') && value && (String)value.t=='c' && value.c){
						attrVal=(String)value.c
						msgVal='Attempting Attribute value'
					}else allowAval=false
					avals=subscriptions[subsId]?.avals ?: []
					if(allowAval && attrVal!=(String)null){
						if(! (attrVal in avals)) avals << attrVal
						msgVal='Attempting Attribute value '+avals
					}else{
						allowAval=false
						msgVal='Using Attribute'
						avals=[]
					}
					if(doit && msgVal!=(String)null && (Integer)rtD.logging>2)debug msgVal+' subscription', rtD

				}else{
					ct=ct ?: comparisonType
				}
				subscriptions[subsId]=[d: deviceId, a: attribute, t: ct, c: (subscriptions[subsId] ? (List)subscriptions[subsId].c:[])+(comparisonType?[node]:[]), allowA: allowAval, avals: avals]
				if(deviceId!=(String)rtD.locationId && deviceId.startsWith(':')){
					rawDevices[deviceId]=getDevice(rtD, deviceId)
				}
			}
			break
		case 'v': //virtual device
			String deviceId=(String)rtD.locationId
			//if we have any trigger, it takes precedence over anything else
			devices[deviceId]=[c: (comparisonType ? 1:0)+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
			String subsId=(String)null
			String attribute
			String operV=(String)operand.v
			String tsubId=deviceId+operV
			switch (operV){
			case 'alarmSystemStatus':
				subsId=tsubId
				attribute="hsmStatus"
				break
			case 'alarmSystemAlert':
				subsId=tsubId
				attribute="hsmAlerts"
				break
			case 'alarmSystemEvent':
				subsId=tsubId
				attribute="hsmSetArm"
				break
			case 'alarmSystemRule':
				subsId=tsubId
				attribute="hsmRules"
				break
			case 'time':
			case 'date':
			case 'datetime':
			case 'mode':
			case 'powerSource':
			case 'systemStart':
				subsId=tsubId
				attribute=operV
				break
			case 'email':
				subsId="$deviceId${operV}${(String)rtD.id}".toString()
				attribute="email.${(String)rtD.id}".toString()// receive email does not work
				break
			case 'ifttt':
				if(value && (String)value.t=='c' && value.c){
					def options=VirtualDevices()[operV]?.o
					def item=options ? options[value.c]:value.c
					if(item){
						subsId="$deviceId${operV}${item}".toString()
						String attrVal=".${item}".toString()
						attribute="${operV}${attrVal}".toString()
					}
				}
				break
			}
			if(subsId!=(String)null){
				String ct=(String)subscriptions[subsId]?.t ?: (String)null
				if((ct=='trigger')|| (comparisonType=='trigger')){
					ct='trigger'
				}else{
					ct=ct ?: comparisonType
				}
				subscriptions[subsId]=[d: deviceId, a: attribute, t: ct, c: (subscriptions[subsId] ? (List)subscriptions[subsId].c:[])+(comparisonType?[node]:[])]
				break
			}
			break
		case 'x':
			String operX=(String)operand.x
			if(operX && operX.startsWith('@')){
				String subsId=operX
				String attribute="${(String)rtD.instanceId}.${operX}".toString()
				String ct=(String)subscriptions[subsId]?.t ?: (String)null
				if((ct=='trigger')|| (comparisonType=='trigger')){
					ct='trigger'
				}else{
					ct=ct ?: comparisonType
				}
				subscriptions[subsId]=[d: (String)rtD.locationId, a: attribute, t: ct, c: (subscriptions[subsId] ? (List)subscriptions[subsId].c:[])+(comparisonType?[node]:[])]
			}
			break
		case 'c': //constant
		case 'e': //expression
			traverseExpressions(operand.exp?.i, expressionTraverser, comparisonType)
			break
		}
	}
	eventTraverser={ Map event, parentEvent ->
		if(event.lo){
			String comparisonType='trigger'
			operandTraverser(event, (Map)event.lo, null, comparisonType)
		}
	}
	conditionTraverser={ Map condition, parentCondition ->
		if((String)condition.co){
			Map comparison=Comparisons().conditions[(String)condition.co]
			String comparisonType='condition'
			if(comparison==null){
				hasTriggers=true
				comparisonType=downgradeTriggers || ((String)condition.sm=='never')? 'condition':'trigger' //subscription method
				comparison=Comparisons().triggers[(String)condition.co]
			}
			if(comparison!=null){
				condition.ct=(String)comparisonType.take(1)
				Integer paramCount=comparison.p ?: 0
				for(Integer i=0; i<=paramCount; i++){
					//get the operand to parse
					Map operand=(i==0 ? (Map)condition.lo:(i==1 ? (Map)condition.ro:(Map)condition.ro2))
					operandTraverser(condition, operand, condition.ro, comparisonType)
				}
			}
		}
		if(condition.ts instanceof List)traverseStatements((List)condition.ts, statementTraverser, condition, statementData)
		if(condition.fs instanceof List)traverseStatements((List)condition.fs, statementTraverser, condition, statementData)
	}
	restrictionTraverser={ Map restriction, parentRestriction ->
		if((String)restriction.co){
			Map comparison=Comparisons().conditions[(String)restriction.co]
			String comparisonType='condition'
			if(comparison==null){
				comparison=Comparisons().triggers[(String)restriction.co]
			}
			if(comparison!=null){
				Integer paramCount=comparison.p ?: 0
				for(Integer i=0; i<=paramCount; i++){
					//get the operand to parse
					Map operand=(i==0 ? (Map)restriction.lo:(i==1 ? (Map)restriction.ro:(Map)restriction.ro2))
					operandTraverser(restriction, operand, null, (String)null)
				}
			}
		}
	}
	statementTraverser={ Map node, parentNode, Map data ->
		downgradeTriggers=data!=null && (Boolean)data.timer
		if(node.r)traverseRestrictions(node.r, restrictionTraverser)
		for(String deviceId in node.d){
			if(deviceId==(String)rtD.oldLocationId)deviceId=(String)rtD.locationId
			devices[deviceId]=devices[deviceId] ?: [c: 0]
			if(deviceId!=(String)rtD.locationId && deviceId.startsWith(':')){
				rawDevices[deviceId]=getDevice(rtD, deviceId)
			}
		}
		switch((String)node.t){
		case 'action':
			if(node.k){
				for (Map k in (List)node.k){
					traverseStatements(k.p?:[], statementTraverser, k, data)
				}
			}
			break
		case 'if':
			if(node.ei){
				for (Map ei in (List)node.ei){
					traverseConditions(ei.c?:[], conditionTraverser)
					traverseStatements(ei.s?:[], statementTraverser, ei, data)
				}
			}
		case 'while':
		case 'repeat':
			traverseConditions(node.c, conditionTraverser)
			break
		case 'on':
			traverseEvents(node.c?:[], eventTraverser)
			break
		case 'switch':
			operandTraverser(node, (Map)node.lo, null, 'condition')
			for (Map c in (List)node.cs){
				operandTraverser(c, (Map)c.ro, null, (String)null)
				//if case is a range, traverse the second operand too
				if((String)c.t=='r')operandTraverser(c, (Map)c.ro2, null, (String)null)
				if(c.s instanceof List) traverseStatements((List)c.s, statementTraverser, node, data)
			}
			break
		case 'every':
			hasTriggers=true
			break
		}
	}
	if(rtD.piston.r)traverseRestrictions((List)rtD.piston.r, restrictionTraverser)
	if(rtD.piston.s)traverseStatements((List)rtD.piston.s, statementTraverser, null, statementData)
	//device variables
	for(variable in rtD.piston.v.findAll{ (String)it.t=='device' && it.v!=null && it.v.d!=null && it.v.d instanceof List}){
		for (String deviceId in (List)variable.v.d){
			if(deviceId==(String)rtD.oldLocationId)deviceId=(String)rtD.locationId
			devices[deviceId]=[c: 0+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
			if(deviceId!=(String)rtD.locationId){
				rawDevices[deviceId]=getDevice(rtD, deviceId)
			}
		}
	}
	Map dds=[:]
//log.debug "subscribeAll subscriptions ${subscriptions}"
	for (subscription in subscriptions){
		String devStr=(String)subscription.value.d
		String altSub='never'
		for (condition in (List)subscription.value.c)if(condition){
			condition.s=false
			String tt0=(String)condition.sm
			altSub= tt0=='always' ? tt0 : (altSub!='always' && tt0!='never' ? tt0 : altSub)
		}
		// check for disabled event subscriptions
		if(!rtD.piston.o?.des && (String)subscription.value.t && !!subscription.value.c && altSub!="never" && ((String)subscription.value.t=="trigger" || altSub=="always" || !hasTriggers)){
			def device=devStr.startsWith(':')? getDevice(rtD, devStr):null
			Boolean allowA=subscription.value.allowA!=null?(Boolean)subscription.value.allowA:false
			String a=(String)subscription.value.a
			if(a=='orientation' || a=='axisX' || a=='axisY' || a=='axisZ'){
				a='threeAxis'
				allowA=false
			}
			if(device!=null){
				for (condition in (List)subscription.value.c)if(condition){
					String t1=(String)condition.sm
					condition.s= t1!='never' && ((String)condition.ct=='t' || t1=='always' || !hasTriggers)
				}
				switch (a){
				case 'time':
				case 'date':
				case 'datetime':
					break
				default:
					Integer cnt=(Integer)ss.events
					List avals=(List)subscription.value.avals
					if(allowA && (Integer)avals.size()<9){
						for (String aval in avals){
							String myattr=a+'.'+aval
							if(doit){
								if((Integer)rtD.logging>0)info "Subscribing to $device.${myattr}...", rtD
								subscribe(device, myattr, deviceHandler)
							}
							cnt+=1
						}
					}else{
						if(doit){
							if((Integer)rtD.logging>0)info "Subscribing to $device.${a}...", rtD
							subscribe(device, a, deviceHandler)
						}
						cnt+=1
					}
					ss.events=cnt
					if(!dds[device.id]){
						ss.devices=(Integer)ss.devices+1
						dds[device.id]=1
					}
				}
			}else{
				error "Failed subscribing to $devStr.${a}, device not found", rtD
			}
		}else{
			for (condition in (List)subscription.value.c)if(condition){ condition.s=false }
			if(devices[devStr]){
				devices[devStr].c=(Integer)devices[devStr].c-1
			}
		}
	}
	//not using fake subscriptions for controlled devices - piston has device in settings
	for (d in devices.findAll{ ((Integer)it.value.c<=0 || rtD.piston.o?.des) && (String)it.key!=(String)rtD.locationId }){
		def device=((String)d.key).startsWith(':')? getDevice(rtD, (String)d.key):null
		if(device!=null && !isDeviceLocation(device)){
			if((Integer)rtD.logging>1 && doit)trace "Piston controls $device...", rtD
			ss.controls=(Integer)ss.controls+1
			if(!dds[device.id]){
				ss.devices=(Integer)ss.devices+1
				dds[device.id]=1
			}
		}
	}
	if(doit){
		//save devices
		List deviceIdList=rawDevices.collect{ it && it.value ? it.value.id:null }
		Boolean a=deviceIdList.removeAll{ it==null }
		updateDeviceList(rtD, deviceIdList, false)

		state.subscriptions=ss
		if((Integer)rtD.logging>1)trace msg, rtD

		//subscribe(app, appHandler)
		subscribe(location, (String)rtD.id, executeHandler)
		Map event=[date:new Date(), device:location, name:'time', value:now(), schedule:[t:0L, s:0, i:-9]]
		a=executeEvent(rtD, event)
		processSchedules rtD, true
	//save cache collected through dummy run
		for(item in rtD.newCache)rtD.cache[(String)item.key]=item.value

		Map t0=getCachedMaps()
		String myId=(String)rtD.id
		if(t0!=null)theCacheFLD[myId].cache=[:]+(Map)rtD.cache
		state.cache=rtD.cache
	}

	}catch (all){
		error "An error has occurred while subscribing: ", rtD, -2, all
	}
}

private List expandDeviceList(Map rtD, List devices, Boolean localVarsOnly=false){
	localVarsOnly=false	//temporary allowing global vars
	List result=[]
	for(String deviceId in devices){
		if(deviceId && (Integer)deviceId.size()==34 && deviceId.startsWith(':') && deviceId.endsWith(':')){
			Boolean a=result.push(deviceId)
		}else{
			if(localVarsOnly){
				//during subscriptions we use local vars only to make sure we don't subscribe to "variable" lists of devices
				Map var=rtD.localVars[deviceId]
				if(var && (String)var.t=='device' && var.v instanceof Map && (String)var.v.t=='d' && var.v.d instanceof List && (Integer)((List)var.v.d).size()!=0)result += (List)var.v.d
			}else{
				Map var=getVariable(rtD, deviceId)
				if((String)var.t=='device' && var.v instanceof List && (Integer)((List)var.v).size()!=0)result += (List)var.v
				if((String)var.t!='device'){
					def device=getDevice(rtD, (String)cast(rtD, var.v, 'string'))
					if(device!=null)result += [hashId(device.id)]
				}
			}
		}
	}
	return result.unique()
}

//def appHandler(evt){
//}

private static String sanitizeVariableName(String name){
	name=name!=(String)null ? name.trim().replace(" ", "_"):(String)null
}

private getDevice(Map rtD, String idOrName){
	if((String)rtD.locationId==idOrName || (String)rtD.oldLocationId==idOrName)return location
	def t0=rtD.devices[idOrName]
	def device=t0!=null ? t0:rtD.devices.find{ (String)it.value.getDisplayName()==idOrName }?.value
	if(device==null){
		if(rtD.allDevices==null){
			Map msg=timer "Device missing from piston. Loading all from parent...", rtD
			rtD.allDevices=(Map)parent.listAvailableDevices(true)
			if((Integer)rtD.logging>2)debug msg, rtD
		}
		if(rtD.allDevices!=null){
			def deviceMap=rtD.allDevices.find{ (idOrName==(String)it.key)|| (idOrName==(String)it.value.getDisplayName())}
			if(deviceMap!=null){
				device=deviceMap.value
				rtD.updateDevices=true
				rtD.devices[(String)deviceMap.key]=device
			}
		}else{
			error "Device ${idOrName} was not found. Please review your piston.", rtD
		}
	}
	return device
}

private getDeviceAttributeValue(Map rtD, device, String attributeName){
	String rtDEvN=rtD.event!=null ? (String)rtD.event.name:''
	Boolean rtDEdID=rtD.event!=null ? rtD.event.device?.id==device.id:false
	if(rtDEvN==attributeName && rtDEdID){
		return rtD.event.value
	}else{
		switch (attributeName){
		case '$status':
			return device.getStatus()
		case 'orientation':
			return getThreeAxisOrientation(rtD.event && rtDEvN=='threeAxis' && rtDEdID ? rtD.event.xyzValue:device.currentValue('threeAxis', true))
		case 'axisX':
			return rtD.event!=null && rtDEvN=='threeAxis' && rtDEdID ? rtD.event.xyzValue.x:device.currentValue('threeAxis', true).x
		case 'axisY':
			return rtD.event!=null && rtDEvN=='threeAxis' && rtDEdID ? rtD.event.xyzValue.y:device.currentValue('threeAxis', true).y
		case 'axisZ':
			return rtD.event!=null && rtDEvN=='threeAxis' && rtDEdID ? rtD.event.xyzValue.z:device.currentValue('threeAxis', true).z
		}
		def result
		try{
			result=device.currentValue(attributeName, true)
		}catch (all){
			error "Error reading current value for $device.$attributeName:", rtD, -2, all
		}
		return result!=null ? result:''
	}
}

private Map getDeviceAttribute(Map rtD, String deviceId, String attributeName, subDeviceIndex=null, Boolean trigger=false){
	if(deviceId==(String)rtD.locationId || deviceId==(String)rtD.oldLocationId){ //backwards compatibility
		//we have the location here
		switch (attributeName){
		case 'mode':
			def mode=location.getCurrentMode()
			return [t:'string', v:hashId((Long)mode.getId()), n:(String)mode.getName()]
		case 'alarmSystemStatus':
			String v=location.hsmStatus
			String n=VirtualDevices()['alarmSystemStatus']?.o[v]
			return [t:'string', v:v, n:n]
		}
		return [t:'string', v:(String)location.getName()]
	}
	def device=getDevice(rtD, deviceId)
	if(device!=null){
		Map attribute=attributeName!=null ? Attributes()[attributeName]:null
		if(attribute==null){
			attribute=[t:'string', /* m:false */ ]
		}
		//x=eXclude - if a momentary attribute is looked for and the device does not match the current device, then we must ignore this during comparisons
		def t0=(attributeName!=null ? getDeviceAttributeValue(rtD, device, attributeName):null)
		String tt1=(String)attribute.t
		Boolean match=t0!=null && ( (t0 instanceof String && tt1 in ['string', 'enum']) ||
				(t0 instanceof Integer && tt1=='integer') )
//	String tt2=myObj(t0)
//if(attributeName)log.warn "attributeName $attributeName t0   $t0 of $tt2   tt1 $tt1    match $match }"
		def value=(attributeName!=null ? (match ? t0:cast(rtD, t0, tt1)):"$device")
		if(attributeName=='hue'){
			value=cast(rtD, (Double)cast(rtD, value, 'decimal')*3.6D, (String)attribute.t)
		}
		//have to compare ids and type for hubitat since the locationid can be the same as the deviceid
		def tt0=rtD.event?.device!=null ? rtD.event.device:location
		Boolean deviceMatch=device?.id==tt0.id && isDeviceLocation(device)==isDeviceLocation(tt0)
		return [t: (String)attribute.t, v: value, d: deviceId, a: attributeName, i: subDeviceIndex, x: (attribute.m!=null || trigger) && (!deviceMatch || (( attributeName=='orientation' || attributeName=='axisX' || attributeName=='axisY' || attributeName=='axisZ' ? 'threeAxis':attributeName)!=(String)rtD.event.name))]
	}
	return [t:'error', v:"Device '${deviceId}' not found"]
}

private Map getJsonData(Map rtD, data, String name, String feature=(String)null){
	if(data!=null){
	try{
		List parts=name.replace('][', '].[').tokenize('.')
		def args=(data instanceof Map ? [:]+(Map)data : (data instanceof List ? []+(List)data : new groovy.json.JsonSlurper().parseText(data)))
		Integer partIndex=-1
		for(String part in parts){
			partIndex=partIndex+1
			if(args instanceof String || args instanceof GString){
				if(args.startsWith('{') && args.endsWith('}')){
					args=(LinkedHashMap)new groovy.json.JsonSlurper().parseText(args)
				}else if(args.startsWith('[') && args.endsWith(']')){
					args=(List)new groovy.json.JsonSlurper().parseText(args)
				}
			}
			if(args instanceof List){
				switch (part){
				case 'length':
					return [t:'integer', v:(Integer)args.size()]
				case 'first':
					args=(Integer)args.size()>0 ? args[0]:''
					continue
					break
				case 'second':
					args=(Integer)args.size()>1 ? args[1]:''
					continue
					break
				case 'third':
					args=(Integer)args.size()>2 ? args[2]:''
					continue
					break
				case 'fourth':
					args=(Integer)args.size()>3 ? args[3]:''
					continue
					break
				case 'fifth':
					args=(Integer)args.size()>4 ? args[4]:''
					continue
					break
				case 'sixth':
					args=(Integer)args.size()>5 ? args[5]:''
					continue
					break
				case 'seventh':
					args=(Integer)args.size()>6 ? args[6]:''
					continue
					break
				case 'eighth':
					args=(Integer)args.size()>7 ? args[7]:''
					continue
					break
				case 'ninth':
					args=(Integer)args.size()>8 ? args[8]:''
					continue
					break
				case 'tenth':
					args=(Integer)args.size()>9 ? args[9]:''
					continue
					break
				case 'last':
					args=(Integer)args.size()>0 ? args[(Integer)args.size()- 1]:''
					continue
					break
				}
			}
			if(!(args instanceof Map) && !(args instanceof List))return [t:'dynamic', v:'']
			//nfl overrides
			Boolean overrideArgs=false
			if(feature=='NFL' && partIndex==1 && !!args && !!args.games){
				def offset=null
				def start=null
				def end=null
				Date date=localDate()
				Integer dow=date.day
				switch (((String)part.tokenize('[')[0]).toLowerCase()){
				case 'yesterday':
					offset=-1
					break
				case 'today':
					offset=0
					break
				case 'tomorrow':
					offset=1
					break
				case 'mon':
				case 'monday':
					offset=dow<=2 ? 1 - dow:8 - dow
					break
				case 'tue':
				case 'tuesday':
					offset=dow<=2 ? 2-dow:9-dow
					break
				case 'wed':
				case 'wednesday':
					offset=dow<=2 ? -4 - dow:3-dow
					break
				case 'thu':
				case 'thursday':
					offset=dow<=2 ? -3 - dow:4-dow
					break
				case 'fri':
				case 'friday':
					offset=dow<=2 ? -2 - dow:5-dow
					break
				case 'sat':
				case 'saturday':
					offset=dow<=2 ? -1 - dow:6-dow
					break
				case 'sun':
				case 'sunday':
					offset=dow<=2 ? 0 - dow:7-dow
					break
				case 'lastweek':
					start=(dow<=2 ? -4 - dow:3-dow)-7
					end=(dow<=2 ? 2 - dow:9-dow)-7
					break
				case 'thisweek':
					start=dow<=2 ? -4 - dow:3-dow
					end=dow<=2 ? 2 - dow:9-dow
					break
				case 'nextweek':
					start=(dow<=2 ? -4 - dow:3-dow)+7
					end=(dow<=2 ? 2 - dow:9-dow)+7
					break
				}
				if(offset!=null){
					date.setTime(Math.round((Long)date.getTime()+offset*86400000.0D))
					def game=args.games.find{ it.year==date.year+1900 && it.month==date.month+1 && it.day==date.date}
					args=game
					continue
				}
				if(start!=null){
					Date startDate=localDate()
					startDate.setTime(Math.round((Long)date.getTime()+start*86400000.0D))
					Date endDate=localDate()
					endDate.setTime(Math.round((Long)date.getTime()+end*86400000.0D))
					start=((Integer)startDate.year+1900)*372+((Integer)startDate.month*31)+((Integer)startDate.date-1)
					end=((Integer)endDate.year+1900)*372+((Integer)endDate.month*31)+((Integer)endDate.date-1)
					if((Integer)parts[0].size()>3){
						def games=args.games.findAll{ (it.year*372+(it.month-1)*31+(it.day-1)>=start) && (it.year*372+(it.month-1)*31+(it.day-1)<=end)}
						args=games
						overrideArgs=true
					}else{
						def game=args.games.find{ (it.year*372+(it.month-1)*31+(it.day-1)>=start) && (it.year*372+(it.month-1)*31+(it.day-1)<=end)}
						args=game
						continue
					}
				}
			}
			def idx=0
			if(part.endsWith(']')){
				//array index
				Integer start=part.indexOf('[')
				if(start>=0){
					idx=part.substring(start+1, (Integer)part.size()-1)
					part=part.substring(0, start)
					if(idx.isInteger()){
						idx=idx.toInteger()
					}else{
						Map var=getVariable(rtD, "$idx".toString())
						idx=(String)var.t!='error' ? var.v:idx
					}
				}
				if(!overrideArgs && !!part)args=args[part]
				if(args instanceof List)idx=cast(rtD, idx, 'integer')
				args=args[idx]
				continue
			}
			if(!overrideArgs)args=args[part]
		}
		return [t:'dynamic', v:"$args".toString()]
	}catch (all){
		error "Error retrieving JSON data part $part", rtD, -2, all
		return [t:'dynamic', v:'']
	}
	}
	return [t:'dynamic', v:'']
}

private Map getArgument(Map rtD, String name){
	return getJsonData(rtD, rtD.args, name)
}

private Map getJson(Map rtD, String name){
	return getJsonData(rtD, rtD.json, name)
}

private Map getPlaces(Map rtD, String name){
	return getJsonData(rtD, rtD.settings?.places, name)
}

private Map getResponse(Map rtD, String name){
	return getJsonData(rtD, rtD.response, name)
}

private Map getWeather(Map rtD, String name){
	if(rtD.weather==null){
		Map t0=parent.getWData()
		rtD.weather=t0!=null ? t0:[:]
	}
	return getJsonData(rtD, rtD.weather, name)
}

private Map getNFLDataFeature(String dataFeature){
	Map requestParams=[
		uri: "https://api.webcore.co/nfl/$dataFeature",
		query: method=="GET" ? data:null
	]
	httpGet(requestParams){ response ->
		if(response.status==200 && response.data && !binary){
			try{
				return response.data instanceof Map ? response.data : (LinkedHashMap)new groovy.json.JsonSlurper().parseText(response.data)
			}catch (all){
				return null
			}
		}
		return null
	}
}

private Map getNFL(Map rtD, String name){
	List parts=name.tokenize('.')
	rtD.nfl=rtD.nfl ?: [:]
	if((Integer)parts.size()>0){
		String dataFeature=(String)(((String)parts[0]).tokenize('[')[0])
		if(rtD.nfl[dataFeature]==null){
			rtD.nfl[dataFeature]=getNFLDataFeature(dataFeature)
		}
	}
	return getJsonData(rtD, rtD.nfl, name, 'NFL')
}

private Map getIncidents(rtD, String name){
	return getJsonData(rtD, rtD.incidents, name)
}

@Field static Boolean initGlobalFLD
@Field static Map globalVarsFLD

public void clearGlobalCache(String meth=(String)null){
	globalVarsFLD=[:]
	initGlobalFLD=false
	if(eric())log.debug "clearing Global cache $meth"
}

private void loadGlobalCache(){
	if(!initGlobalFLD){
		globalVarsFLD=(Map)parent.listAvailableVariables()
		initGlobalFLD=true
		if(eric())log.debug 'loading Global cache'
	}
}

private Map getVariable(Map rtD, String name){
	Map var=parseVariableName(name)
	name=sanitizeVariableName((String)var.name)
	if(name==(String)null)return [t:'error', v:'Invalid empty variable name']
	Map result
	String tname=name
	if(tname.startsWith('@')){
		loadGlobalCache()
		def tresult=globalVarsFLD[tname]
		if(!(tresult instanceof Map))result=[t:'error', v:"Variable '$tname' not found"]
		else result=(Map)tresult
		result.v=cast(rtD, result.v, (String)result.t)
	}else{
		if(tname.startsWith('$')){
			Integer t0=(Integer)tname.size()
			if(tname.startsWith('$args.') && (t0>6)){
				result=getArgument(rtD, tname.substring(6))
			}else if(tname.startsWith('$args[') && (t0>6)){
				result=getArgument(rtD, tname.substring(5))
			}else if(tname.startsWith('$json.') && (t0>6)){
				result=getJson(rtD, tname.substring(6))
			}else if(tname.startsWith('$json[') && (t0>6)){
				result=getJson(rtD, tname.substring(5))
			}else if(tname.startsWith('$places.') && (t0>8)){
				result=getPlaces(rtD, tname.substring(8))
			}else if(tname.startsWith('$places[') && (t0>8)){
				result=getPlaces(rtD, tname.substring(7))
			}else if(tname.startsWith('$response.') && (t0>10)){
				result=getResponse(rtD, tname.substring(10))
			}else if(tname.startsWith('$response[') && (t0>10)){
				result=getResponse(rtD, tname.substring(9))
			}else if(tname.startsWith('$nfl.') && (t0>5)){
				result=getNFL(rtD, tname.substring(5))
			}else if(tname.startsWith('$weather.') && (t0>9)){
				result=getWeather(rtD, tname.substring(9))
			}else if(tname.startsWith('$incidents.') && (t0>11)){
				result=getIncidents(rtD, tname.substring(11))
			}else if(tname.startsWith('$incidents[') && (t0>11)){
				result=getIncidents(rtD, tname.substring(10))
			}else{
				def tresult=rtD.systemVars[tname]
				if(!(tresult instanceof Map))result=[t:'error', v:"Variable '$tname' not found"]
				else result=(Map)tresult
				if(result!=null && result.d){
					result=[t: (String)result.t, v: getSystemVariableValue(rtD, tname)]
				}
			}
		}else{
			def tlocalVar=rtD.localVars[tname]
			if(!(tlocalVar instanceof Map)){
				result=[t:'error', v:"Variable '$tname' not found"]
			}else{
				result=[t: (String)tlocalVar.t, v: tlocalVar.v]
				//make a local copy of the list
				if(result.v instanceof List)result.v=[]+(List)result.v
				//make a local copy of the map
				if(result.v instanceof Map)result.v=[:]+(Map)result.v
			}
		}
	}
	if(result!=null && (((String)result.t).endsWith(']'))){
		result.t=((String)result.t).replace('[]', '')
		if(result.v instanceof Map && (String)var.index!=(String)null && (String)var.index!=''){
			Map indirectVar=getVariable(rtD, (String)var.index)
			//indirect variable addressing
			if((String)indirectVar.t!='error'){
				def value=(String)indirectVar.t=='decimal' ? (Integer)cast(rtD, indirectVar.v, 'integer', (String)indirectVar.t):indirectVar.v
				String dataType=(String)indirectVar.t=='decimal' ? 'integer':(String)indirectVar.t
				var.index=(String)cast(rtD, value, 'string', dataType)
			}
			result.v=result.v[(String)var.index]
		}
	}else{
		if(result.v instanceof Map){
			String tt0=(String)result.t
			result=(Map)evaluateOperand(rtD, null, (Map)result.v)
			result=(tt0!=null && tt0==(String)result.t) ? result : evaluateExpression(rtD, result, tt0)
		}
	}
	return [t:(String)result.t, v:result.v]
}

private Map setVariable(Map rtD, String name, value){
	Map var=parseVariableName(name)
	name=sanitizeVariableName((String)var.name)
	if(name==(String)null)return [t:'error', v:'Invalid empty variable name']
	String tname=name
	if(tname.startsWith('@')){
		loadGlobalCache()
		def tvariable=globalVarsFLD[tname]
		if(tvariable instanceof Map){
			Map variable=(Map)globalVarsFLD[tname]
			variable.v=cast(rtD, value, (String)variable.t)
			Map cache=rtD.gvCache!=null ? (Map)rtD.gvCache:[:]
			cache[tname]=variable
			rtD.gvCache=cache
			return variable
		}
	}else{
// global vars are removed by setting them to null via webcore dashboard
// local vars are removed by 'clear all data' via HE console
		def tvariable=rtD.localVars[tname]
		if(tvariable instanceof Map){
			Map variable=(Map)rtD.localVars[tname]
			if(((String)variable.t).endsWith(']')){
				//we're dealing with a list
				variable.v=(variable.v instanceof Map)? variable.v:[:]
				if((String)var.index=='*CLEAR'){
					variable.v.clear()
				}else{
					Map indirectVar=getVariable(rtD, (String)var.index)
					//indirect variable addressing
					if((String)indirectVar.t!='error'){
						var.index=(String)cast(rtD, indirectVar.v, 'string', (String)indirectVar.t)
					}
					variable.v[(String)var.index]=cast(rtD, value, ((String)variable.t).replace('[]', ''))
				}
			}else{
				def v=(value instanceof GString)? "$value".toString():value
				Boolean match=v!=null && ((v instanceof String && t=='string')||
							(v instanceof Long && t=='long')||
							(v instanceof Integer && t=='integer')||
							(v instanceof Double && t=='decimal'))
				variable.v=match ? v:cast(rtD, v, (String)variable.t)
			}
			if(!variable.f){
				Map vars
				Map t0=getCachedMaps()
				if(t0!=null)vars=(Map)t0.vars
				else{ vars=(Boolean)rtD.pep ? (Map)atomicState.vars:(Map)state.vars }

				vars[tname]=variable.v
				String myId=(String)rtD.id
				if(t0!=null)theCacheFLD[myId].vars=vars
				if((Boolean)rtD.pep)atomicState.vars=vars
				else state.vars=vars
			}
			return variable
		}
	}
	return [t:'error', v:'Invalid variable']
}

public Map setLocalVariable(String name, value){ // called by parent (IDE)to set value to a variable
	name=sanitizeVariableName(name)
	if(name==null || name.startsWith('@'))return [:]
	def t0=atomicState.vars
	Map vars=t0!=null ? t0:[:]
	vars[name]=value
	atomicState.vars=vars
	clearMyCache('setLocalVariable')
	return vars
}

/** EXPRESSION FUNCTIONS							**/

public Map proxyEvaluateExpression(Map rtD, Map expression, String dataType=(String)null){
	rtD=getRunTimeData(rtD)
	resetRandomValues(rtD)
	try{
		Map result=evaluateExpression(rtD, expression, dataType)
		if((String)result.t=='device' && result.a!=null){
			Map attr=Attributes()[(String)result.a]
			result=evaluateExpression(rtD, result, attr!=null && attr.t!=null ? (String)attr.t:'string')
		}
		return result
	}catch (all){
		error 'An error occurred while executing the expression', rtD, -2, all
	}
	return [t:'error', v:'expression error']
}

private static Map simplifyExpression(Map expression){
	while ((String)expression.t=='expression' && expression.i && (Integer)((List)expression.i).size()==1) expression=((List)expression.i)[0]
	return expression
}

private Map evaluateExpression(Map rtD, Map expression, String dataType=(String)null){
	//if dealing with an expression that has multiple items, let's evaluate each item one by one
	//let's evaluate this expression
	if(!expression)return [t:'error', v:'Null expression']
	//not sure what it was needed for - need to comment more
	//if(expression && expression.v instanceof Map)return evaluateExpression(rtD, expression.v, expression.t)
	Long time=now()
	expression=simplifyExpression(expression)
	String mySt="evaluateExpression $expression   dataType: $dataType".toString()
	if((Boolean)rtD.eric) myDetail rtD, mySt, 1
	Map result=expression
	String exprType=(String)expression.t
	switch (exprType){
	case 'integer':
	case 'long':
	case 'decimal':
		result=[t:exprType, v:expression.v]
		break
	case 'time':
		def t0=expression.v
		Boolean found=false
		if("$t0".isNumber() && (t0<86400000))found=true
		result=[t:exprType, v: found ? t0.toLong():(Long)cast(rtD, t0, exprType, dataType)]
		break
	case 'datetime':
		def t0=expression.v
		if("$t0".isNumber() && (t0>=86400000)){
			result=[t:exprType, v: t0.toLong() ]
			break
		}
	case 'int32':
	case 'int64':
	case 'date':
		result=[t:exprType, v:cast(rtD, expression.v, exprType, dataType)]
		break
	case 'bool':
	case 'boolean':
		def t0=expression.v
		if(t0 instanceof Boolean){
			result=[t:'boolean', v:(Boolean)t0]
			break
		}
		result=[t:'boolean', v:(Boolean)cast(rtD, t0, 'boolean', dataType)]
		break
	case 'string':
	case 'enum':
	case 'error':
	case 'phone':
	case 'uri':
	case 'text':
		def t0=expression.v
		if(t0 instanceof String){
			result=[t:'string', v:(String)t0]
			break
		}
		result=[t:'string', v:(String)cast(rtD, t0, 'string', dataType)]
		break
	case 'number':
	case 'float':
	case 'double':
		def t0=expression.v
		if(t0 instanceof Double){
			result=[t:'decimal', v:(Double)t0]
			break
		}
		result=[t:'decimal', v:(Double)cast(rtD, expression.v, 'decimal', dataType)]
		break
	case 'duration':
		String t0=(String)expression.vt
		if(t0==null && expression.v instanceof Long){ result=[t:'long', v:(Long)expression.v ] }
		else result=[t:'long', v:(Long)cast(rtD, expression.v, t0!=(String)null ? t0:'long')]
		break
	case 'variable':
		//get variable as{n: name, t: type, v: value}
		//result=[t:'error', v:'Invalid variable']
		result=getVariable(rtD, (String)expression.x+((String)expression.xi!=(String)null ? '['+(String)expression.xi+']':''))
		break
	case 'device':
		//get variable as{n: name, t: type, v: value}
		if(expression.v instanceof List){
			//already parsed
			result=expression
		}else{
			List deviceIds=(expression.id instanceof List)? (List)expression.id:(expression.id ? [expression.id]:[])
			if((Integer)deviceIds.size()==0){
				Map var=getVariable(rtD, (String)expression.x)
				if((String)var.t=='device'){
					deviceIds=(List)var.v
				}else{
					def device=getDevice(rtD, (String)var.v)
					if(device!=null)deviceIds=[hashId(device.id)]
				}
			}
			result=[t:'device', v:deviceIds, a:(String)expression.a]
		}
		break
	case 'operand':
		result=[t:'string', v:(String)cast(rtD, expression.v, 'string')]
		break
	case 'function':
		String fn='func_'+(String)expression.n
		//in a function, we look for device parameters, they may be lists - we need to reformat all parameters to send them to the function properly
		String myStr
		try{
			List params=[]
			if(expression.i && (Integer)expression.i.size()!=0){
				for (Map i in (List)expression.i){
					Map param=simplifyExpression(i)
					if(((String)param.t=='device')|| ((String)param.t=='variable')){
						//if multiple devices involved, we need to spread the param into multiple params
						param=evaluateExpression(rtD, param)
						Integer sz=param.v instanceof List ? (Integer)((List)param.v).size():1
						switch (sz){
							case 0: break
							case 1: Boolean a=params.push(param); break
							default:
								for (v in param.v){
								Boolean b=params.push([t: (String)param.t, a: (String)param.a, v: [v]])
							}
						}
					}else{
						Boolean a=params.push(param)
					}
				}
			}
			myStr='calling function '+fn
			if((Boolean)rtD.eric) myDetail rtD, myStr, 1
			result=(Map)"$fn"(rtD, params)
		}catch (all){
			error "Error executing $fn: ", rtD, -2, all
			//log error
			result=[t:'error', v:all]
		}
		if((Boolean)rtD.eric) myDetail rtD, myStr+' '+"${result}".toString(), -1
		break
	case 'expression':
		//if we have a single item, we simply traverse the expression
		List items=[]
		Integer operand=-1
		Integer lastOperand=-1
		for(Map item in (List)expression.i){
			if((String)item.t=='operator'){
				if(operand<0){
					switch ((String)item.o){
					case '+':
					case '-':
					case '**':
					case '&':
					case '|':
					case '^':
					case '~':
					case '~&':
					case '~|':
					case '~^':
					case '<':
					case '>':
					case '<=':
					case '>=':
					case '==':
					case '!=':
					case '<>':
					case '<<':
					case '>>':
					case '!':
					case '!!':
					case '?':
						Boolean a=items.push([t:'integer', v:0, o:(String)item.o])
						break
					case ':':
						if(lastOperand>=0){
							//groovy-style support for (object ?: value)
							Boolean a=items.push(items[lastOperand]+[o: (String)item.o])
						}else{
							Boolean a=items.push([t:'integer', v:0, o:(String)item.o])
						}
						break
					case '*':
					case '/':
						Boolean a=items.push([t:'integer', v:1, o: (String)item.o])
						break
					case '&&':
					case '!&':
						Boolean a=items.push([t:'boolean', v:true, o: (String)item.o])
						break
					case '||':
					case '!|':
					case '^^':
					case '!^':
						Boolean a=items.push([t:'boolean', v:false, o: (String)item.o])
						break
					}
				}else{
					items[operand].o=(String)item.o
					operand=-1
				}
			}else{
				Boolean a=items.push(evaluateExpression(rtD, item)+[:])
				operand=(Integer)items.size()-1
				lastOperand=operand
			}
		}
		//clean up operators, ensure there's one for each
		Integer idx=0
		for(Map item in items){
			if(!item.o){
				switch ((String)item.t){
					case 'integer':
					case 'float':
					case 'double':
					case 'decimal':
					case 'number':
						String nextType='string'
						if(idx<(Integer)items.size()-1)nextType=(String)items[idx+1].t
						item.o=(nextType=='string' || nextType=='text')? '+':'*'
						break
					default:
						item.o='+'
						break
				}
			}
			idx++
		}
		//do the job
		idx=0
		Boolean secondary=false
		while ((Integer)items.size()>1){
			//ternary
			if((Integer)items.size()==3 && (String)items[0].o=='?' && (String)items[1].o==':'){
				//we have a ternary operator
				if((Boolean)evaluateExpression(rtD, (Map)items[0], 'boolean').v){
					items=[items[1]]
				}else{
					items=[items[2]]
				}
				items[0].o=(String)null
				break
			}
			//order of operations :D
			idx=0
			//#2	!   !!   ~   -	Logical negation, logical double-negation, bitwise NOT, and numeric negation unary operators
			for (Map item in items){
				String t0=(String)item.o
				if((t0=='!')|| (t0=='!!')|| (t0=='~')|| (item.t==null && t0=='-'))break
				secondary=true
				idx++
			}
			//#3	**	Exponent operator
			if(idx>=(Integer)items.size()){
				//we then look for power **
				idx=0
				for (Map item in items){
					if(((String)item.o)=='**')break
					idx++
				}
			}
			//#4	*   /   \   % MOD	Multiplication, division, modulo
			if(idx>=(Integer)items.size()){
				//we then look for * or /
				idx=0
				for (Map item in items){
					String t0=(String)item.o
					if((t0=='*')|| (t0=='/')|| (t0=='\\')|| (t0=='%'))break
					idx++
				}
			}
			//#5	+   -	Addition and subtraction
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='+')|| (((String)item.o)=='-'))break
					idx++
				}
			}
			//#6	<<   >>	Shift left and shift right operators
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='<<')|| (((String)item.o)=='>>'))break
					idx++
				}
			}
			//#7	<  <= >  >=	Comparisons: less than, less than or equal to, greater than, greater than or equal to
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					String t0=(String)item.o
					if((t0=='>')|| (t0=='<')|| (t0=='>=')|| (t0=='<='))break
					idx++
				}
			}
			//#8	==   !=	Comparisons: equal and not equal
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					String t0=(String)item.o
					if((t0=='==')|| (t0=='!=')|| (t0=='<>'))break
					idx++
				}
			}
			//#9	&	Bitwise AND
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='&')|| (((String)item.o)=='~&'))break
					idx++
				}
			}
			//#10	^	Bitwise exclusive OR (XOR)
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='^')|| (((String)item.o)=='~^'))break
					idx++
				}
			}
			//#11	|	Bitwise inclusive (normal)OR
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='|')|| (((String)item.o)=='~|'))break
					idx++
				}
			}
			//#12	&&	Logical AND
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='&&')|| (((String)item.o)=='!&'))break
					idx++
				}
			}
			//#13	^^	Logical XOR
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='^^')|| (((String)item.o)=='~^'))break
					idx++
				}
			}
			//#14	||	Logical OR
			if(idx>=(Integer)items.size()){
				idx=0
				for (Map item in items){
					if((((String)item.o)=='||')|| (((String)item.o)=='!|'))break
					idx++
				}
			}
			if(idx>=(Integer)items.size()){
				//just get the first one
				idx=0
			}
			if(idx>=(Integer)items.size()-1)idx=0
			//we're onto something
			def v=null
			String o=(String)items[idx].o
			String a1=(String)items[idx].a
			String t1=(String)items[idx].t
			def v1=items[idx].v
			String a2=(String)items[idx+1].a
			String t2=(String)items[idx+1].t
			def v2=items[idx+1].v
			String t=t1
			//fix-ups
			//integer with decimal gives decimal, also *, / require decimals
			if(t1=='device' && a1!=null && (Integer)a1.length()>0){
				Map attr=Attributes()[a1]
				t1=attr!=null ? (String)attr.t:'string'
			}
			if(t2=='device' && a2!=null && (Integer)a2.length()>0){
				Map attr=Attributes()[a2]
				t2=attr!=null ? (String)attr.t:'string'
			}
			if(t1=='device' && t2=='device' && ((o=='+')|| (o=='-'))){
				v1=(v1 instanceof List)? v1:[v1]
				v2=(v2 instanceof List)? v2:[v2]
				v=(o=='+') ? v1+v2 : v1-v2
				//set the results
				items[idx+1].t='device'
				items[idx+1].v=v
			}else{
				Boolean t1d=(t1=='datetime')|| (t1=='date')|| (t1=='time')
				Boolean t2d=(t2=='datetime')|| (t2=='date')|| (t2=='time')
				Boolean t1i=(t1=='number')|| (t1=='integer')|| (t1=='long')
				Boolean t2i=(t2=='number')|| (t2=='integer')|| (t2=='long')
				Boolean t1f=(t1=='decimal')|| (t1=='float')
				Boolean t2f=(t2=='decimal')|| (t2=='float')
				Boolean t1n=t1i || t1f
				Boolean t2n=t2i || t2f
				//warn "Precalc ($t1) $v1 $o ($t2) $v2 >>> t1d=$t1d, t2d=$t2d, t1n=$t1n, t2n=$t2n", rtD
				if(((o=='+') || (o=='-')) && (t1d || t2d) && (t1d || t1n) && (t2n || t2d)){
					//if dealing with date +/- date/numeric then
					if(t1n){
						t=t2
					}else if(t2n){
						t=t1
					}else{
						t=t1=='date' && t2=='date' ? 'date':((t1=='time') && (t2=='time')? 'time':'datetime')
					}
				}else{
					if((o=='+')|| (o=='-')){
						//devices and others play nice
						if(t1=='device'){
							t=t2
							t1=t2
						}else if(t2=='device'){
							t=t1
							t2=t1
						}
					}
					if((o=='*')|| (o=='/')|| (o=='-')|| (o=='**')){
						t=(t1i && t2i)? ((t1=='long')|| (t2=='long')? 'long':'integer'):'decimal'
						t1=t
						t2=t
					}
					if((o=='\\')|| (o=='%')|| (o=='&')|| (o=='|')|| (o=='^')|| (o=='~&')|| (o=='~|')|| (o=='~^')|| (o=='<<')|| (o=='>>')){
						t=(t1=='long')|| (t2=='long')? 'long':'integer'
						t1=t
						t2=t
					}
					if((o=='&&')|| (o=='||')|| (o=='^^')|| (o=='!&')|| (o=='!|')|| (o=='!^')|| (o=='!')|| (o=='!!')){
						t1='boolean'
						t2='boolean'
						t='boolean'
					}
					if((o=='+')&& ((t1=='string')|| (t1=='text')|| (t2=='string')|| (t2=='text'))){
						t1='string'
						t2='string'
						t='string'
					}
					if(t1n && t2n){
						t=(t1i && t2i)? ((t1=='long')|| (t2=='long')? 'long':'integer'):'decimal'
						t1=t
						t2=t
					}
					if((o=='==')|| (o=='!=')|| (o=='<')|| (o=='>')|| (o=='<=')|| (o=='>=')|| (o=='<>')){
						if(t1=='device')t1='string'
						if(t2=='device')t2='string'
						t1=t1=='string' ? t2:t1
						t2=t2=='string' ? t1:t2
						t='boolean'
					}
				}
				v1=evaluateExpression(rtD, (Map)items[idx], t1).v
				v2=evaluateExpression(rtD, (Map)items[idx+1], t2).v
				v1=v1=='null' ? null:v1
				v2=v2=='null' ? null:v2
				switch (o){
					case '?':
					case ':':
						error "Invalid ternary operator. Ternary operator's syntax is (condition ? trueValue:falseValue ). Please check your syntax and try again.", rtD
						v=''
						break
					case '-':
						v=v1 - v2
						break
					case '*':
						v=v1 * v2
						break
					case '/':
						v=(v2!=0 ? v1/v2 : 0)
						break
					case '\\':
						v=(Integer)Math.floor(v2!=0 ? v1/v2 : 0)
						break
					case '%':
						v=(Integer)(v2!=0 ? v1%v2 : 0)
						break
					case '**':
						v=v1 ** v2
						break
					case '&':
						v=v1 & v2
						break
					case '|':
						v=v1 | v2
						break
					case '^':
						v=v1 ^ v2
						break
					case '~&':
						v=~(v1 & v2)
						break
					case '~|':
						v=~(v1 | v2)
						break
					case '~^':
						v=~(v1 ^ v2)
						break
					case '~':
						v=~v2
						break
					case '<<':
						v=v1 << v2
						break
					case '>>':
						v=v1 >> v2
						break
					case '&&':
						v=!!v1 && !!v2
						break
					case '||':
						v=!!v1 || !!v2
						break
					case '^^':
						v=!v1!=!v2
						break
					case '!&':
						v=!(!!v1 && !!v2)
						break
					case '!|':
						v=!(!!v1 || !!v2)
						break
					case '!^':
						v=!(!v1!=!v2)
						break
					case '==':
						v=v1==v2
						break
					case '!=':
					case '<>':
						v=v1!=v2
						break
					case '<':
						v=v1<v2
						break
					case '>':
						v=v1>v2
						break
					case '<=':
						v=v1<=v2
						break
					case '>=':
						v=v1>=v2
						break
					case '!':
						v=!v2
						break
					case '!!':
						v=!!v2
						break
					case '+':
					default:
						v=t=='string' ? "$v1$v2":v1+v2
						break
				}

				if((Integer)rtD.logging>2)debug "Calculating ($t1)$v1 $o ($t2)$v2 >> ($t)$v", rtD

				//set the results
				items[idx+1].t=t
				v=(v instanceof GString)? "$v".toString():v
				Boolean match=v!=null && ((v instanceof String && t=='string')||
							(v instanceof Long && t=='long')||
							(v instanceof Integer && t=='integer')||
							(v instanceof Double && t=='decimal'))
				if(match)items[idx+1].v=v
				else items[idx+1].v=cast(rtD, v, t)
			}
			Integer sz=(Integer)items.size()
			items.remove(idx)
		}
		result=items[0] ? (((String)items[0].t=='device')? items[0]:evaluateExpression(rtD, (Map)items[0])):[t:'dynamic', v:null]
		break
	}
	//return the value, either directly or via cast, if certain data type is requested
	//when dealing with devices, they need to be "converted" unless the request is to return devices
	if(dataType!=(String)null && (dataType!='device')&& ((String)result.t=='device')){
		//if not a list, make it a list
		if(!(result.v instanceof List))result.v=[result.v]
		switch ((Integer)((List)result.v).size()){
			case 0: result=[t:'error', v:'Empty device list']; break
			case 1: result=getDeviceAttribute(rtD, (String)((List)result.v)[0], (String)result.a, result.i); break
			default: result=[t:'string', v:buildDeviceAttributeList(rtD, (List)result.v, (String)result.a)]; break
		}
	}
	if(dataType!=(String)null){
		String t0=(String)result.t
		def t1=result.v
		if(dataType!=t0){
			Boolean match= (dataType in ['string', 'enum']) && (t0 in ['string', 'enum'])
			if(!match)t1=cast(rtD, result.v, dataType, t0)
		}
		result=[t:dataType, v: t1] + (result.a ? [a:(String)result.a]:[:])+(result.i ? [a:result.i]:[:])
	}
	result.d=now()-time
	if((Boolean)rtD.eric) myDetail rtD, mySt+" result: $result".toString(), -1
	return result
}

private static String buildList(list, String suffix='and'){
	if(!list)return ''
	List nlist=(list instanceof List)? list:[list]
	Integer cnt=1
	String result=''
	for (item in nlist){
		result += "$item"+(cnt<(Integer)nlist.size()? (cnt==(Integer)nlist.size()-1 ? " $suffix ":', '):'')
		cnt++
	}
	return result
}

private String buildDeviceList(Map rtD, devices, String suffix='and'){
	if(!devices)return ''
	List nlist=(devices instanceof List)? devices:[devices]
	List list=[]
	for (String device in nlist){
		def dev=getDevice(rtD, device)
		if(dev!=null)Boolean a=list.push(dev)
	}
	return buildList(list, suffix)
}

private String buildDeviceAttributeList(Map rtD, devices, String attribute, String suffix='and'){
	if(!devices)return ''
	List nlist=(devices instanceof List)? devices:[devices]
	List list=[]
	for (String device in nlist){
		def value=getDeviceAttribute(rtD, device, attribute).v
		Boolean a=list.push(value)
	}
	return buildList(list, suffix)
}

private static Boolean checkParams(Map rtD, List params, Integer minParams){
	if(params==null || !(params instanceof List) || ((Integer)params.size()<minParams)) return false
	return true
}

/** dewPoint returns the calculated dew point temperature			**/
/** Usage: dewPoint(temperature, relativeHumidity[, scale])			**/
private Map func_dewpoint(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting dewPoint(temperature, relativeHumidity[, scale])']
	}
	Double t=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	Double rh=(Double)evaluateExpression(rtD, (Map)params[1], 'decimal').v
	//if no temperature scale is provided, we assume the location's temperature scale
	Boolean fahrenheit=((String)cast(rtD, (Integer)params.size()>2 ? (String)evaluateExpression(rtD, (Map)params[2]).v:(String)location.temperatureScale, 'string')).toUpperCase()=='F'
	if(fahrenheit){
		t=(t-32.0D)*5.0D/9.0D
	}
	//convert rh to percentage
	if((rh>0) && (rh<1)){
		rh=rh*100.0D
	}
	Double b=(Math.log(rh/100.0D)+((17.27D*t)/(237.3D+t)))/17.27D
	Double result=(237.3D*b)/(1.0D-b)
	if(fahrenheit){
		result=result*9.0D/5.0D+32.0D
	}
	return [t:'decimal', v:result]
}

/** celsius converts temperature from Fahrenheit to Celsius			**/
/** Usage: celsius(temperature)							**/
private Map func_celsius(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting celsius(temperature)']
	}
	Double t=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	//convert temperature to Celsius
	return [t:'decimal', v:(Double)((t-32.0D)*5.0D/9.0D)]
}


/** fahrenheit converts temperature from Celsius to Fahrenheit			**/
/** Usage: fahrenheit(temperature)						**/
private Map func_fahrenheit(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting fahrenheit(temperature)']
	}
	Double t=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	//convert temperature to Fahrenheit
	return [t:'decimal', v:(Double)(t*9.0D/5.0D+32.0D)]
}

/** fahrenheit converts temperature between Celsius and Fahrenheit if the	**/
/** units differ from location.temperatureScale					**/
/** Usage: convertTemperatureIfNeeded(celsiusTemperature, 'C')			**/
private Map func_converttemperatureifneeded(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting convertTemperatureIfNeeded(temperature, unit)']
	}
	Double t=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	String u=((String)evaluateExpression(rtD, (Map)params[1], 'string').v).toUpperCase()
	//convert temperature to Fahrenheit
	switch ((String)location.temperatureScale){
		case u: return [t:'decimal', v:t]
		case 'F': return func_celsius(rtD, [params[0]])
		case 'C': return func_fahrenheit(rtD, [params[0]])
	}
}

/** integer converts a decimal to integer value			**/
/** Usage: integer(decimal or string)				**/
private Map func_integer(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting integer(decimal or string)']
	}
	return [t:'integer', v:(Integer)evaluateExpression(rtD, (Map)params[0], 'integer').v]
}
private Map func_int(Map rtD, List params){ return func_integer(rtD, params)}

/** decimal/float converts an integer value to it's decimal value		**/
/** Usage: decimal(integer or string)						**/
private Map func_decimal(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting decimal(integer or string)']
	}
	return [t:'decimal', v:(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v]
}
private Map func_float(Map rtD, List params){ return func_decimal(rtD, params)}
private Map func_number(Map rtD, List params){ return func_decimal(rtD, params)}

/** string converts an value to it's string value				**/
/** Usage: string(anything)							**/
private Map func_string(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting string(anything)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtD, param, 'string').v
	}
	return [t:'string', v:result]
}
private Map func_concat(Map rtD, List params){ return func_string(rtD, params)}
private Map func_text(Map rtD, List params){ return func_string(rtD, params)}

/** Boolean converts a value to it's Boolean value				**/
/** Usage: boolean(anything)							**/
private Map func_boolean(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting boolean(anything)']
	}
	return [t:'boolean', v:(Boolean)evaluateExpression(rtD, (Map)params[0], 'boolean').v]
}
private Map func_bool(Map rtD, List params){ return func_boolean(rtD, params)}

/** sqr converts a decimal to square decimal value			**/
/** Usage: sqr(integer or decimal or string)				**/
private Map func_sqr(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting sqr(integer or decimal or string)']
	}
	return [t:'decimal', v:(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v**2]
}

/** sqrt converts a decimal to square root decimal value		**/
/** Usage: sqrt(integer or decimal or string)				**/
private Map func_sqrt(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting sqrt(integer or decimal or string)']
	}
	return [t:'decimal', v:Math.sqrt((Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v)]
}

/** power converts a decimal to power decimal value			**/
/** Usage: power(integer or decimal or string, power)			**/
private Map func_power(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting sqrt(integer or decimal or string, power)']
	}
	return [t:'decimal', v:(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v ** (Double)evaluateExpression(rtD, (Map)params[1], 'decimal').v]
}

/** round converts a decimal to rounded value			**/
/** Usage: round(decimal or string[, precision])		**/
private Map func_round(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting round(decimal or string[, precision])']
	}
	Integer precision=((Integer)params.size()>1)? (Integer)evaluateExpression(rtD, (Map)params[1], 'integer').v:0
	return [t:'decimal', v:Math.round((Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v * (10 ** precision))/(10 ** precision)]
}

/** floor converts a decimal to closest lower integer value		**/
/** Usage: floor(decimal or string)					**/
private Map func_floor(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting floor(decimal or string)']
	}
	return [t:'integer', v:(Integer)cast(rtD, Math.floor((Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v), 'integer')]
}

/** ceiling converts a decimal to closest higher integer value	**/
/** Usage: ceiling(decimal or string)						**/
private Map func_ceiling(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting ceiling(decimal or string)']
	}
	return [t:'integer', v:(Integer)cast(rtD, Math.ceil((Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v), 'integer')]
}
private Map func_ceil(Map rtD, List params){ return func_ceiling(rtD, params)}


/** sprintf converts formats a series of values into a string			**/
/** Usage: sprintf(format, arguments)						**/
private Map func_sprintf(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting sprintf(format, arguments)']
	}
	String format=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	List args=[]
	for (Integer x=1; x<(Integer)params.size(); x++){
		Boolean a=args.push(evaluateExpression(rtD, (Map)params[x]).v)
	}
	try{
		return [t:'string', v:sprintf(format, args)]
	}catch(all){
		return [t:'error', v:"$all"]
	}
}
private Map func_format(Map rtD, List params){ return func_sprintf(rtD, params)}

/** left returns a substring of a value					**/
/** Usage: left(string, count)						**/
private Map func_left(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting left(string, count)']
	}
	String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	Integer count=(Integer)evaluateExpression(rtD, (Map)params[1], 'integer').v
	if(count>(Integer)value.size())count=(Integer)value.size()
	return [t:'string', v:value.substring(0, count)]
}

/** right returns a substring of a value				**/
/** Usage: right(string, count)						**/
private Map func_right(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting right(string, count)']
	}
	String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	Integer count=(Integer)evaluateExpression(rtD, (Map)params[1], 'integer').v
	if(count>(Integer)value.size())count=(Integer)value.size()
	return [t:'string', v:value.substring((Integer)value.size()-count, (Integer)value.size())]
}

/** strlen returns the length of a string value				**/
/** Usage: strlen(string)						**/
private Map func_strlen(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting strlen(string)']
	}
	String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	return [t:'integer', v:(Integer)value.size()]
}
private Map func_length(Map rtD, List params){ return func_strlen(rtD, params)}

/** coalesce returns the first non-empty parameter				**/
/** Usage: coalesce(value1[, value2[, ..., valueN]])				**/
private Map func_coalesce(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting coalesce(value1[, value2[, ..., valueN]])']
	}
	for (i=0; i<(Integer)params.size(); i++){
		Map value=evaluateExpression(rtD, (Map)params[0])
		if(!(value.v==null || (value.v instanceof List ? value.v==[null] || value.v==[] || value.v==['null'] : false) || (String)value.t=='error' || value.v=='null' || (String)cast(rtD, value.v, 'string')=='')){
			return value
		}
	}
	return [t:'dynamic', v:null]
}

/** trim removes leading and trailing spaces from a string			**/
/** Usage: trim(value)								**/
private Map func_trim(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting trim(value)']
	}
	String t0=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String value=(String)t0.trim()
	return [t:'string', v:value]
}

/** trimleft removes leading spaces from a string				**/
/** Usage: trimLeft(value)							**/
private Map func_trimleft(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting trimLeft(value)']
	}
	String t0=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String value=(String)t0.replaceAll('^\\s+', '')
	return [t:'string', v:value]
}
private Map func_ltrim(Map rtD, List params){ return func_trimleft(rtD, params)}

/** trimright removes trailing spaces from a string				**/
/** Usage: trimRight(value)							**/
private Map func_trimright(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting trimRight(value)']
	}
	String t0=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String value=(String)t0.replaceAll('\\s+$', '')
	return [t:'string', v:value]
}
private Map func_rtrim(Map rtD, List params){ return func_trimright(rtD, params)}

/** substring returns a substring of a value					**/
/** Usage: substring(string, start, count)					**/
private Map func_substring(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting substring(string, start, count)']
	}
	String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	Integer start=(Integer)evaluateExpression(rtD, (Map)params[1], 'integer').v
	def count=(Integer)params.size()>2 ? (Integer)evaluateExpression(rtD, (Map)params[2], 'integer').v:null
	//def end=null
	String result=''
	if((start<(Integer)value.size())&& (start>-(Integer)value.size())){
		if(count!=null){
			if(count<0){
				//reverse
				start=start<0 ? -start:(Integer)value.size()-start
				count=-count
				value=value.reverse()
			}
			if(start>=0){
				if(count>(Integer)value.size()-start)count= (Integer)value.size()-start
			}else{
				if(count>-start)count=-start
			}
		}
		start=start>=0 ? start : (Integer)value.size()+start
		if(count>(Integer)value.size()-start)count=(Integer)value.size()-start
		result=(count==null) ? value.substring(start) : value.substring(start, start+count)
	}
	return [t:'string', v:result]
}
private Map func_substr(Map rtD, List params){ return func_substring(rtD, params)}
private Map func_mid(Map rtD, List params){ return func_substring(rtD, params)}

/** replace replaces a search text inside of a value				**/
/** Usage: replace(string, search, replace[, [..], search, replace])		**/
private Map func_replace(Map rtD, List params){
	if(!checkParams(rtD, params,3) || (Integer)params.size()%2 != 1){
		return [t:'error', v:'Expecting replace(string, search, replace[, [..], search, replace])']
	}
	String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	Integer cnt=Math.floor(((Integer)params.size()-1)/2)
	for (Integer i=0; i<cnt; i++){
		String search=(String)evaluateExpression(rtD, (Map)params[i*2+1], 'string').v
		String replace=(String)evaluateExpression(rtD, (Map)params[i*2+2], 'string').v
		if(((Integer)search.size()>2)&& search.startsWith('/')&& search.endsWith('/')){
			search=~search.substring(1, (Integer)search.size()-1)
			value=value.replaceAll(search, replace)
		}else{
			value=value.replace(search, replace)
		}
	}
	return [t:'string', v:value]
}

/** rangeValue returns the matching value in a range					**/
/** Usage: rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])	**/
private Map func_rangevalue(Map rtD, List params){
	if(!checkParams(rtD, params,2) || (Integer)params.size()%2!=0){
		return [t:'error', v:'Expecting rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])']
	}
	Double input=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	def value=params[1]
	Integer cnt=Math.floor(((Integer)params.size()-2)/2)
	for (Integer i=0; i<cnt; i++){
		Double point=(Double)evaluateExpression(rtD, (Map)params[i*2 +2], 'decimal').v
		if(input>=point)value=params[i*2 +3]
	}
	return value
}

/** rainbowValue returns the matching value in a range				**/
/** Usage: rainbowValue(input, minInput, minColor, maxInput, maxColor)		**/
private Map func_rainbowvalue(Map rtD, List params){
	if(!checkParams(rtD, params,5)){
		return [t:'error', v:'Expecting rainbowValue(input, minColor, minValue, maxInput, maxColor)']
	}
	Integer input=(Integer)evaluateExpression(rtD, (Map)params[0], 'integer').v
	Integer minInput=(Integer)evaluateExpression(rtD, (Map)params[1], 'integer').v
	Map minColor=getColor(rtD, (String)evaluateExpression(rtD, (Map)params[2], 'string').v)
	Integer maxInput=(Integer)evaluateExpression(rtD, (Map)params[3], 'integer').v
	Map maxColor=getColor(rtD, (String)evaluateExpression(rtD, (Map)params[4], 'string').v)
	if(minInput>maxInput){
		Integer x=minInput
		minInput=maxInput
		maxInput=x
		Map x1=minColor
		minColor=maxColor
		maxColor=x1
	}
	input=(input<minInput ? minInput:(input>maxInput ? maxInput:input))
	if((input==minInput)|| (minInput==maxInput))return [t:'string', v:(String)minColor.hex]
	if(input==maxInput)return [t:'string', v:(String)maxColor.hex]
	List start=hexToHsl((String)minColor.hex)
	List end=hexToHsl((String)maxColor.hex)
	Double alpha=1.0D*(input-minInput)/(maxInput-minInput+1)
	Integer h=Math.round(start[0]-((input-minInput)*(start[0]-end[0])/(maxInput-minInput)))
	Integer s=Math.round(start[1]+(end[1]-start[1])*alpha)
	Integer l=Math.round(start[2]+(end[2]-start[2])*alpha)
	return [t:'string', v:hslToHex(h,s,l)]
}

/** indexOf finds the first occurrence of a substring in a string		**/
/** Usage: indexOf(stringOrDeviceOrList, substringOrItem)			**/
private Map func_indexof(Map rtD, List params){
	if(!checkParams(rtD, params,2) || ((String)params[0].t!='device' && (Integer)params.size()!=2)){
		return [t:'error', v:'Expecting indexOf(stringOrDeviceOrList, substringOrItem)']
	}
	if(((String)params[0].t=='device')&& ((Integer)params.size()>2)){
		String item=(String)evaluateExpression(rtD, (Map)params[(Integer)params.size()-1], 'string').v
		for (Integer idx=0; idx<(Integer)params.size()-1; idx++){
			Map it=evaluateExpression(rtD, (Map)params[idx], 'string')
			if(it.v==item){
				return [t:'integer', v:idx]
			}
		}
		return [t:'integer', v:-1]
	}else if(params[0].v instanceof Map){
		def item=evaluateExpression(rtD, (Map)params[1], (String)params[0].t).v
		def key=params[0].v.find{ it.value==item }?.key
		return [t:'string', v:key]
	}else{
		String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
		String substring=(String)evaluateExpression(rtD, (Map)params[1], 'string').v
		return [t:'integer', v:(Integer)value.indexOf(substring)]
	}
}

/** lastIndexOf finds the first occurrence of a substring in a string		**/
/** Usage: lastIndexOf(string, substring)					**/
private Map func_lastindexof(Map rtD, List params){
	if(!checkParams(rtD, params,2) || ((String)params[0].t!='device' && (Integer)params.size()!=2)){
		return [t:'error', v:'Expecting lastIndexOf(string, substring)']
	}
	if(((String)params[0].t=='device')&& ((Integer)params.size()>2)){
		String item=(String)evaluateExpression(rtD, (Map)params[(Integer)params.size()-1], 'string').v
		for (Integer idx=(Integer)params.size()-2; idx>=0; idx--){
			Map it=evaluateExpression(rtD, (Map)params[idx], 'string')
			if(it.v==item){
				return [t:'integer', v:idx]
			}
		}
		return [t:'integer', v:-1]
	}else if(params[0].v instanceof Map){
		String item=evaluateExpression(rtD, (Map)params[1], (String)params[0].t).v
		def key=params[0].v.find{ it.value==item }?.key
		return [t:'string', v:key]
	}else{
		String value=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
		String substring=(String)evaluateExpression(rtD, (Map)params[1], 'string').v
		return [t:'integer', v:(Integer)value.lastIndexOf(substring)]
	}
}


/** lower returns a lower case value of a string				**/
/** Usage: lower(string)							**/
private Map func_lower(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting lower(string)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtD, param, 'string').v
	}
	return [t:'string', v:result.toLowerCase()]
}

/** upper returns a upper case value of a string				**/
/** Usage: upper(string)							**/
private Map func_upper(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting upper(string)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtD, param, 'string').v
	}
	return [t:'string', v:result.toUpperCase()]
}

/** title returns a title case value of a string				**/
/** Usage: title(string)							**/
private Map func_title(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting title(string)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtD, param, 'string').v
	}
	return [t:'string', v:result.tokenize(' ')*.toLowerCase()*.capitalize().join(' ')]
}

/** avg calculates the average of a series of numeric values			**/
/** Usage: avg(values)								**/
private Map func_avg(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting avg(value1, value2, ..., valueN)']
	}
	Double sum=0
	for (Map param in params){
		sum += (Double)evaluateExpression(rtD, param, 'decimal').v
	}
	return [t:'decimal', v:sum/(Integer)params.size()]
}

/** median returns the value in the middle of a sorted array			**/
/** Usage: median(values)							**/
private Map func_median(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting median(value1, value2, ..., valueN)']
	}
	List data=params.collect{ evaluateExpression(rtD, (Map)it, 'dynamic')}.sort{ it.v }
	if(data){
		return data[(Integer)Math.floor((Integer)data.size()/2)]
	}
	return [t:'dynamic', v:'']
}

/** least returns the value that is least found a series of numeric values	**/
/** Usage: least(values)							**/
private Map func_least(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting least(value1, value2, ..., valueN)']
	}
	Map data=[:]
	for (Map param in params){
		Map value=evaluateExpression(rtD, param, 'dynamic')
		data[value.v]=[t:(String)value.t, v:value.v, c:(data[value.v]?.c ?: 0)+1]
	}
	def value=data.sort{ it.value.c }.collect{ it.value }[0]
	return [t:(String)value.t, v:value.v]
}

/** most returns the value that is most found a series of numeric values	**/
/** Usage: most(values)								**/
private Map func_most(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting most(value1, value2, ..., valueN)']
	}
	Map data=[:]
	for (Map param in params){
		Map value=evaluateExpression(rtD, param, 'dynamic')
		data[value.v]=[t:(String)value.t, v:value.v, c:(data[value.v]?.c ?: 0)+1]
	}
	def value=data.sort{ -it.value.c }.collect{ it.value }[0]
	return [t:(String)value.t, v:value.v]
}

/** sum calculates the sum of a series of numeric values			**/
/** Usage: sum(values)								**/
private Map func_sum(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting sum(value1, value2, ..., valueN)']
	}
	Double sum=0
	for (Map param in params){
		sum += (Double)evaluateExpression(rtD, param, 'decimal').v
	}
	return [t:'decimal', v:sum]
}

/** variance calculates the standard deviation of a series of numeric values 	**/
/** Usage: stdev(values)							**/
private Map func_variance(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting variance(value1, value2, ..., valueN)']
	}
	Double sum=0
	List values=[]
	for (Map param in params){
		Double value=(Double)evaluateExpression(rtD, param, 'decimal').v
		Boolean a=values.push(value)
		sum += value
	}
	Double avg=sum/(Integer)values.size()
	sum=0
	for(Integer i=0; i<(Integer)values.size(); i++){
		sum += ((Double)values[i]-avg)**2
	}
	return [t:'decimal', v:sum/(Integer)values.size()]
}

/** stdev calculates the standard deviation of a series of numeric values	**/
/** Usage: stdev(values)							**/
private Map func_stdev(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting stdev(value1, value2, ..., valueN)']
	}
	Map result=func_variance(rtD, params)
	return [t:'decimal', v:Math.sqrt(result.v)]
}

/** min calculates the minimum of a series of numeric values			**/
/** Usage: min(values)								**/
private Map func_min(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting min(value1, value2, ..., valueN)']
	}
	List data=params.collect{ evaluateExpression(rtD, (Map)it, 'dynamic')}.sort{ it.v }
	if(data){
		return data[0]
	}
	return [t:'dynamic', v:'']
}

/** max calculates the maximum of a series of numeric values			**/
/** Usage: max(values)								**/
private Map func_max(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting max(value1, value2, ..., valueN)']
	}
	List data=params.collect{ evaluateExpression(rtD, (Map)it, 'dynamic')}.sort{ it.v }
	if(data){
		return data[(Integer)data.size()-1]
	}
	return [t:'dynamic', v:'']
}

/** abs calculates the absolute value of a number				**/
/** Usage: abs(number)								**/
private Map func_abs(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting abs(value)']
	}
	Double value=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	String dataType=(value==Math.round(value)? 'integer':'decimal')
	return [t:dataType, v:(Double)cast(rtD, Math.abs(value), dataType, 'decimal')]
}

/** hslToHex converts a hue/saturation/level trio to it hex #rrggbb representation	**/
/** Usage: hslToHex(hue, saturation, level)						**/
private Map func_hsltohex(Map rtD, List params){
	if(!checkParams(rtD, params,3)){
		return [t:'error', v:'Expecting hsl(hue, saturation, level)']
	}
	Double hue=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
	Double saturation=(Double)evaluateExpression(rtD, (Map)params[1], 'decimal').v
	Double level=(Double)evaluateExpression(rtD, (Map)params[2], 'decimal').v
	return [t:'string', v:hslToHex(hue, saturation, level)]
}

/** count calculates the number of true/non-zero/non-empty items in a series of numeric values		**/
/** Usage: count(values)										**/
private Map func_count(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'integer', v:0]
	}
	Integer count=0
	if((Integer)params.size()==1 && ((String)params[0].t=='string' || (String)params[0].t=='dynamic')){
		List list=((String)evaluateExpression(rtD, (Map)params[0], 'string').v).split(',').toList()
		for (Integer i=0; i<(Integer)list.size(); i++){
			count += (Boolean)cast(rtD, list[i], 'boolean')? 1:0
		}
	}else{
		for (Map param in params){
			count += (Boolean)evaluateExpression(rtD, param, 'boolean').v ? 1:0
		}
	}
	return [t:'integer', v:count]
}

/** size returns the number of values provided				**/
/** Usage: size(values)							**/
private Map func_size(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'integer', v:0]
	}
	Integer count=0
	if((Integer)params.size()==1 && ((String)params[0].t=='string' || (String)params[0].t=='dynamic')){
		List list=((String)evaluateExpression(rtD, (Map)params[0], 'string').v).split(',').toList()
		count=(Integer)list.size()
	}else{
		count=(Integer)params.size()
	}
	return [t:'integer', v:count]
}

/** age returns the number of milliseconds an attribute had the current value	**/
/** Usage: age([device:attribute])						**/
private Map func_age(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting age([device:attribute])']
	}
	Map param=evaluateExpression(rtD, (Map)params[0], 'device')
	if(((String)param.t=='device')&& ((String)param.a)&& (Integer)((List)param.v).size()){
		def device=getDevice(rtD, (String)((List)param.v)[0])
		if(device!=null){
			def dstate=device.currentState((String)param.a, true)
			if(dstate){
				Long result=now()-(Long)dstate.getDate().getTime()
				return [t:'long', v:result]
			}
		}
	}
	return [t:'error', v:'Invalid device']
}

/** previousAge returns the number of milliseconds an attribute had the  previous value		**/
/** Usage: previousAge([device:attribute])							**/
private Map func_previousage(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting previousAge([device:attribute])']
	}
	Map param=evaluateExpression(rtD, (Map)params[0], 'device')
	if((String)param.t=='device' && (String)param.a && (Integer)((List)param.v).size()){
		def device=getDevice(rtD, (String)((List)param.v)[0])
		if(device!=null && !isDeviceLocation(device)){
			List states=device.statesSince((String)param.a, new Date(now()-604500000L), [max:5])
			if((Integer)states.size()>1){
				def newValue=states[0].getValue()
				//some events get duplicated, so we really want to look for the last "different valued" state
				for(Integer i=1; i<(Integer)states.size(); i++){
					if(states[i].getValue()!=newValue){
						Long result=now()-(Long)states[i].getDate().getTime()
						return [t:'long', v:result]
					}
				}
			}
			//we're saying 7 days, though it may be wrong - but we have no data
			return [t:'long', v:604800000L]
		}
	}
	return [t:'error', v:'Invalid device']
}

/** previousValue returns the previous value of the attribute				**/
/** Usage: previousValue([device:attribute])						**/
private Map func_previousvalue(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting previousValue([device:attribute])']
	}
	Map param=evaluateExpression(rtD, (Map)params[0], 'device')
	if((String)param.t=='device' && (String)param.a && (Integer)((List)param.v).size()){
		Map attribute=Attributes()[(String)param.a]
		if(attribute!=null){
			def device=getDevice(rtD, (String)((List)param.v)[0])
			if(device!=null && !isDeviceLocation(device)){
				List states=device.statesSince((String)param.a, new Date(now()-604500000), [max:5])
				if((Integer)states.size()>1){
					def newValue=states[0].getValue()
					//some events get duplicated, so we really want to look for the last "different valued" state
					for(Integer i=1; i<(Integer)states.size(); i++){
						def result=states[i].getValue()
						if(result!=newValue){
							return [t:(String)attribute.t, v:cast(rtD, result, (String)attribute.t)]
						}
					}
				}
				//we're saying no value - we have no data
				return [t:'string', v:'']
			}
		}
	}
	return [t:'error', v:'Invalid device']
}

/** newer returns the number of devices whose attribute had the current		**/
/** value for less than the specified number of milliseconds			**/
/** Usage: newer([device:attribute] [,.., [device:attribute]], threshold)	**/
private Map func_newer(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting newer([device:attribute] [,.., [device:attribute]], threshold)']
	}
	Long threshold=(Long)evaluateExpression(rtD, (Map)params[(Integer)params.size()-1], 'long').v
	Integer result=0
	for (Integer i=0; i<(Integer)params.size()-1; i++){
		Map age=func_age(rtD, [params[i]])
		if((String)age.t!='error' && (Long)age.v<threshold)result++
	}
	return [t:'integer', v:result]
}

/** older returns the number of devices whose attribute had the current		**/
/** value for more than the specified number of milliseconds			**/
/** Usage: older([device:attribute] [,.., [device:attribute]], threshold)	**/
private Map func_older(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting older([device:attribute] [,.., [device:attribute]], threshold)']
	}
	Long threshold=(Long)evaluateExpression(rtD, (Map)params[(Integer)params.size()-1], 'long').v
	Integer result=0
	for (Integer i=0; i<(Integer)params.size()-1; i++){
		Map age=func_age(rtD, [params[i]])
		if((String)age.t!='error' && (Long)age.v>=threshold)result++
	}
	return [t:'integer', v:result]
}

/** startsWith returns true if a string starts with a substring			**/
/** Usage: startsWith(string, substring)					**/
private Map func_startswith(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting startsWith(string, substring)']
	}
	String string=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String substring=(String)evaluateExpression(rtD, (Map)params[1], 'string').v
	return [t:'boolean', v:string.startsWith(substring)]
}

/** endsWith returns true if a string ends with a substring				**/
/** Usage: endsWith(string, substring)							**/
private Map func_endswith(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting endsWith(string, substring)']
	}
	String string=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String substring=(String)evaluateExpression(rtD, (Map)params[1], 'string').v
	return [t:'boolean', v:string.endsWith(substring)]
}

/** contains returns true if a string contains a substring				**/
/** Usage: contains(string, substring)							**/
private Map func_contains(Map rtD, List params){
	if(!checkParams(rtD, params,2) || ((String)params[0].t!='device' && (Integer)params.size()!=2)){
		return [t:'error', v:'Expecting contains(string, substring)']
	}
	if((String)params[0].t=='device' && (Integer)params.size()>2){
		String item=evaluateExpression(rtD, (Map)params[(Integer)params.size()-1], 'string').v
		for (Integer idx=0; idx<(Integer)params.size()-1; idx++){
			Map it=evaluateExpression(rtD, (Map)params[idx], 'string')
			if(it.v==item){
				return [t:'boolean', v:true]
			}
		}
		return [t:'boolean', v:false]
	}else{
		String string=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
		String substring=(String)evaluateExpression(rtD, (Map)params[1], 'string').v
		return [t:'boolean', v:string.contains(substring)]
	}
}

/** matches returns true if a string matches a pattern					**/
/** Usage: matches(string, pattern)							**/
private Map func_matches(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting matches(string, pattern)']
	}
	String string=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String pattern=(String)evaluateExpression(rtD, (Map)params[1], 'string').v
	if(((Integer)pattern.size()>2)&& pattern.startsWith('/')&& pattern.endsWith('/')){
		pattern=~pattern.substring(1, (Integer)pattern.size()-1)
		return [t:'boolean', v: !!(string =~ pattern)]
	}
	return [t:'boolean', v:string.contains(pattern)]
}

/** eq returns true if two values are equal					**/
/** Usage: eq(value1, value2)							**/
private Map func_eq(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting eq(value1, value2)']
	}
	String t=(String)params[0].t=='device' ? (String)params[1].t:(String)params[0].t
	Map value1=evaluateExpression(rtD, (Map)params[0], t)
	Map value2=evaluateExpression(rtD, (Map)params[1], t)
	return [t:'boolean', v: value1.v==value2.v]
}

/** lt returns true if value1<value2						**/
/** Usage: lt(value1, value2)							**/
private Map func_lt(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting lt(value1, value2)']
	}
	Map value1=evaluateExpression(rtD, (Map)params[0])
	Map value2=evaluateExpression(rtD, (Map)params[1], (String)value1.t)
	return [t:'boolean', v: value1.v<value2.v]
}

/** le returns true if value1<=value2						**/
/** Usage: le(value1, value2)							**/
private Map func_le(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting le(value1, value2)']
	}
	Map value1=evaluateExpression(rtD, (Map)params[0])
	Map value2=evaluateExpression(rtD, (Map)params[1], (String)value1.t)
	return [t:'boolean', v: value1.v<=value2.v]
}

/** gt returns true if value1>value2						**/
/** Usage: gt(value1, value2)							**/
private Map func_gt(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting gt(value1, value2)']
	}
	Map value1=evaluateExpression(rtD, (Map)params[0])
	Map value2=evaluateExpression(rtD, (Map)params[1], (String)value1.t)
	return [t:'boolean', v: value1.v>value2.v]
}

/** ge returns true if value1>=value2						**/
/** Usage: ge(value1, value2)							**/
private Map func_ge(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting ge(value1, value2)']
	}
	Map value1=evaluateExpression(rtD, (Map)params[0])
	Map value2=evaluateExpression(rtD, (Map)params[1], (String)value1.t)
	return [t:'boolean', v: value1.v>=value2.v]
}

/** not returns the negative Boolean value					**/
/** Usage: not(value)								**/
private Map func_not(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting not(value)']
	}
	Boolean value=(Boolean)evaluateExpression(rtD, (Map)params[0], 'boolean').v
	return [t:'boolean', v: !value]
}

/** if evaluates a Boolean and returns value1 if true, or value2 otherwise	**/
/** Usage: if(condition, valueIfTrue, valueIfFalse)				**/
private Map func_if(Map rtD, List params){
	if(!checkParams(rtD, params,3)){
		return [t:'error', v:'Expecting if(condition, valueIfTrue, valueIfFalse)']
	}
	Boolean value=(Boolean)evaluateExpression(rtD, (Map)params[0], 'boolean').v
	return value ? evaluateExpression(rtD, (Map)params[1]) : evaluateExpression(rtD, (Map)params[2])
}

/** isEmpty returns true if the value is empty					**/
/** Usage: isEmpty(value)							**/
private Map func_isempty(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting isEmpty(value)']
	}
	Map value=evaluateExpression(rtD, (Map)params[0])
	Boolean result=value.v==null || (value.v instanceof List ? value.v==[null] || value.v==[] || value.v==['null'] : false) || (String)value.t=='error' || value.v=='null' || (String)cast(rtD, value.v, 'string')=='' || "$value.v".toString()==''
	return [t:'boolean', v:result]
}

/** datetime returns the value as a datetime type				**/
/** Usage: datetime([value])							**/
private Map func_datetime(Map rtD, List params){
	if(!checkParams(rtD, params,0) || (Integer)params.size()>1){
		return [t:'error', v:'Expecting datetime([value])']
	}
	Long value=(Integer)params.size()>0 ? (Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v:now()
	return [t:'datetime', v:value]
}

/** date returns the value as a date type					**/
/** Usage: date([value])							**/
private Map func_date(Map rtD, List params){
	if(!checkParams(rtD, params,0) || (Integer)params.size()>1){
		return [t:'error', v:'Expecting date([value])']
	}
	Long value=(Integer)params.size()>0 ? (Long)evaluateExpression(rtD, (Map)params[0], 'date').v:(Long)cast(rtD, now(), 'date', 'datetime')
	return [t:'date', v:value]
}

/** time returns the value as a time type					**/
/** Usage: time([value])							**/
private Map func_time(Map rtD, List params){
	if(!checkParams(rtD, params,0) || (Integer)params.size()>1){
		return [t:'error', v:'Expecting time([value])']
	}
	Long value=(Integer)params.size()>0 ? (Long)evaluateExpression(rtD, (Map)params[0], 'time').v:(Long)cast(rtD, now(), 'time', 'datetime')
	return [t:'time', v:value]
}

/** addSeconds returns the value as a time type						**/
/** Usage: addSeconds([dateTime, ]seconds)						**/
private Map func_addseconds(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>2){
		return [t:'error', v:'Expecting addSeconds([dateTime, ]seconds)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtD, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*1000L
	return [t:'datetime', v: value+delta]
}

/** addMinutes returns the value as a time type						**/
/** Usage: addMinutes([dateTime, ]minutes)						**/
private Map func_addminutes(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>2){
		return [t:'error', v:'Expecting addMinutes([dateTime, ]minutes)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtD, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*60000L
	return [t:'datetime', v: value+delta]
}

/** addHours returns the value as a time type						**/
/** Usage: addHours([dateTime, ]hours)							**/
private Map func_addhours(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>2){
		return [t:'error', v: 'Expecting addHours([dateTime, ]hours)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtD, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*3600000L
	return [t:'datetime', v: value+delta]
}

/** addDays returns the value as a time type						**/
/** Usage: addDays([dateTime, ]days)							**/
private Map func_adddays(Map rtD, List params){
	if(!checkParams(rtD, params,1)|| (Integer)params.size()>2){
		return [t:'error', v:'Expecting addDays([dateTime, ]days)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtD, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*86400000L
	return [t:'datetime', v: value+delta]
}

/** addWeeks returns the value as a time type						**/
/** Usage: addWeeks([dateTime, ]weeks)							**/
private Map func_addweeks(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>2){
		return [t:'error', v:'Expecting addWeeks([dateTime, ]weeks)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtD, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*604800000L
	return [t:'datetime', v: value+delta]
}

/** weekDayName returns the name of the week day					**/
/** Usage: weekDayName(dateTimeOrWeekDayIndex)						**/
private Map func_weekdayname(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting weekDayName(dateTimeOrWeekDayIndex)']
	}
	Long value=(Long)evaluateExpression(rtD, (Map)params[0], 'long').v
	Integer index=((value>=86400000L)? (Integer)utcToLocalDate(value).day:value) % 7
	return [t:'string', v:weekDays()[index]]
}

/** monthName returns the name of the month						**/
/** Usage: monthName(dateTimeOrMonthNumber)						**/
private Map func_monthname(Map rtD, List params){
	if(!checkParams(rtD, params,1)) return [t:'error', v:'Expecting monthName(dateTimeOrMonthNumber)']
	Long value=(Long)evaluateExpression(rtD, (Map)params[0], 'long').v
	Integer index=((value>=86400000L)? (Integer)utcToLocalDate(value).month:value-1)%12+1
	return [t:'string', v:yearMonths()[index]]
}

/** arrayItem returns the nth item in the parameter list				**/
/** Usage: arrayItem(index, item0[, item1[, .., itemN]])				**/
private Map func_arrayitem(Map rtD, List params){
	if(!checkParams(rtD, params,2)){
		return [t:'error', v:'Expecting arrayItem(index, item0[, item1[, .., itemN]])']
	}
	Integer index=(Integer)evaluateExpression(rtD, (Map)params[0], 'integer').v
	if((Integer)params.size()==2 && ((String)params[1].t=='string' || (String)params[1].t=='dynamic')){
		List list=((String)evaluateExpression(rtD, (Map)params[1], 'string').v).split(',').toList()
		if(index<0 || index>=(Integer)list.size()) return [t:'error', v:'Array item index is outside of bounds.']
		return [t:'string', v:list[index]]
	}
	Integer sz=(Integer)params.size()-1
	if(index<0 || index>=sz) return [t:'error', v:'Array item index is outside of bounds.']
	return params[index+1]
}

/** isBetween returns true if value>=startValue and value<=endValue		**/
/** Usage: isBetween(value, startValue, endValue)				**/
private Map func_isbetween(Map rtD, List params){
	if(!checkParams(rtD, params,3)) return [t:'error', v:'Expecting isBetween(value, startValue, endValue)']
	Map value=evaluateExpression(rtD, (Map)params[0])
	Map startValue=evaluateExpression(rtD, (Map)params[1], (String)value.t)
	Map endValue=evaluateExpression(rtD, (Map)params[2], (String)value.t)
	return [t:'boolean', v: value.v>=startValue.v && value.v<=endValue.v]
}

/** formatDuration returns a duration in a readable format					**/
/** Usage: formatDuration(value[, friendly=false[, granularity='s'[, showAdverbs=false]]])	**/
private Map func_formatduration(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>4){
		return [t:'error', v:"Expecting formatDuration(value[, friendly=false[, granularity='s'[, showAdverbs=false]]])"]
	}
	Long value=(Long)evaluateExpression(rtD, (Map)params[0], 'long').v
	Boolean friendly=(Integer)params.size()>1 ? (Boolean)evaluateExpression(rtD, (Map)params[1], 'boolean').v:false
	String granularity=(Integer)params.size()>2 ? (String)evaluateExpression(rtD, (Map)params[2], 'string').v:'s'
	Boolean showAdverbs=(Integer)params.size()>3 ? (Boolean)evaluateExpression(rtD, (Map)params[3], 'boolean').v:false

	Integer sign=(value>=0)? 1:-1
	if(sign<0)value=-value
	Integer ms=value%1000
	value=Math.floor((value-ms)/1000.0D)
	Integer s=value%60
	value=Math.floor((value-s)/60.0D)
	Integer m=value%60
	value=Math.floor((value-m)/60.0D)
	Integer h=value%24
	value=Math.floor((value-h)/24.0D)
	Integer d=value

	Integer parts=0
	String partName=''
	switch (granularity){
		case 'd': parts=1; partName='day'; break
		case 'h': parts=2; partName='hour'; break
		case 'm': parts=3; partName='minute'; break
		case 'ms': parts=5; partName='millisecond'; break
		default: parts=4; partName='second'; break
	}
	parts=friendly ? parts:(parts<3 ? 3:parts)
	String result=''
	if(friendly){
		List p=[]
		if(d)Boolean a=p.push("$d day"+(d>1 ? 's':''))
		if(parts>1 && h)Boolean a=p.push("$h hour"+(h>1 ? 's':''))
		if(parts>2 && m)Boolean a=p.push("$m minute"+(m>1 ? 's':''))
		if(parts>3 && s)Boolean a=p.push("$s second"+(s>1 ? 's':''))
		if(parts>4 && ms)Boolean a=p.push("$ms millisecond"+(ms>1 ? 's':''))
		switch ((Integer)p.size()){
			case 0:
				result=showAdverbs ? 'now':'0 '+partName+'s'
				break
			case 1:
				result=p[0]
				break
			default:
				result=''
				Integer sz=(Integer)p.size()
				for (Integer i=0; i<sz; i++){
					result += (i ? (sz>2 ? ', ':' '):'')+(i==sz-1 ? 'and ':'')+p[i]
				}
				result=(showAdverbs && (sign>0)? 'in ':'')+result+(showAdverbs && (sign<0)? ' ago':'')
				break
		}
	}else{
		result=(sign<0 ? '-':'')+(d>0 ? sprintf("%dd ", d):'')+sprintf("%02d:%02d", h, m)+(parts>3 ? sprintf(":%02d", s):'')+(parts>4 ? sprintf(".%03d", ms):'')
	}
	return [t:'string', v:result]
}

/** formatDateTime returns a datetime in a readable format				**/
/** Usage: formatDateTime(value[, format])						**/
private Map func_formatdatetime(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>2){
		return [t:'error', v:'Expecting formatDateTime(value[, format])']
	}
	Long value=(Long)evaluateExpression(rtD, (Map)params[0], 'datetime').v
	String format=(Integer)params.size()>1 ? (String)evaluateExpression(rtD, (Map)params[1], 'string').v:(String)null
	return [t:'string', v:(format ? formatLocalTime(value, format) : formatLocalTime(value))]
}

/** random returns a random value						**/
/** Usage: random([range | value1, value2[, ..,valueN]])			**/
private Map func_random(Map rtD, List params){
	Integer sz=params!=null && (params instanceof List) ? (Integer)params.size():0
	switch (sz){
		case 0:
			return [t:'decimal', v:Math.random()]
		case 1:
			Double range=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
			return [t:'integer', v:(Integer)Math.round(range*Math.random())]
		case 2:
			if(((String)params[0].t=='integer' || (String)params[0].t=='decimal') && ((String)params[1].t=='integer' || (String)params[1].t=='decimal')){
				Double min=(Double)evaluateExpression(rtD, (Map)params[0], 'decimal').v
				Double max=(Double)evaluateExpression(rtD, (Map)params[1], 'decimal').v
				if(min>max){
					Double swap=min
					min=max
					max=swap
				}
				return [t:'integer', v:(Integer)Math.round(min+(max-min)*Math.random())]
			}
	}
	Integer choice=(Integer)Math.round((sz-1)*Math.random())
	if(choice>=sz)choice=sz-1
	return (Map)params[choice]
}


/** distance returns a distance measurement							**/
/** Usage: distance((device | latitude, longitude), (device | latitude, longitude)[, unit])	**/
private Map func_distance(Map rtD, List params){
	if(!checkParams(rtD, params,2) || (Integer)params.size()>4){
		return [t:'error', v:'Expecting distance((device | latitude, longitude), (device | latitude, longitude)[, unit])']
	}
	Double lat1, lng1, lat2, lng2
	String unit
	Integer idx=0
	Integer pidx=0
	String errMsg=''
	while (pidx<(Integer)params.size()){
		if((String)params[pidx].t!='device' || ((String)params[pidx].t=='device' && !!params[pidx].a)){
			//a decimal or device attribute is provided
			switch (idx){
			case 0:
				lat1=(Double)evaluateExpression(rtD,(Map)params[pidx],'decimal').v
				break
			case 1:
				lng1=(Double)evaluateExpression(rtD,(Map)params[pidx],'decimal').v
				break
			case 2:
				lat2=(Double)evaluateExpression(rtD,(Map)params[pidx],'decimal').v
				break
			case 3:
				lng2=(Double)evaluateExpression(rtD,(Map)params[pidx],'decimal').v
				break
			case 4:
				unit=(String)evaluateExpression(rtD,(Map)params[pidx],'string').v
			}
			idx += 1
			pidx += 1
			continue
		}else{
			switch (idx){
			case 0:
			case 2:
				params[pidx].a='latitude'
				Double lat=(Double)evaluateExpression(rtD, (Map)params[pidx], 'decimal').v
				params[pidx].a='longitude'
				Double lng=(Double)evaluateExpression(rtD, (Map)params[pidx], 'decimal').v
				if(idx==0){
					lat1=lat
					lng1=lng
				}else{
					lat2=lat
					lng2=lng
				}
				idx += 2
				pidx += 1
				continue
			default:
				errMsg="Invalid parameter order. Expecting parameter #${idx+1} to be a decimal, not a device."
				pidx=-1
				break
			}
		}
		if(pidx==-1)break
	}
	if(errMsg!='')return [t:'error', v:errMsg]
	if(idx<4 || idx>5)return [t:'error', v:'Invalid parameter combination. Expecting either two devices, a device and two decimals, or four decimals, followed by an optional unit.']
	Double earthRadius=6371000.0D //meters
	Double dLat=Math.toRadians(lat2-lat1)
	Double dLng=Math.toRadians(lng2-lng1)
	Double a=Math.sin(dLat/2.0D)*Math.sin(dLat/2.0D)+
		Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
		Math.sin(dLng/2.0D)*Math.sin(dLng/2.0D)
	Double c=2.0D*Math.atan2(Math.sqrt(a), Math.sqrt(1.0D-a))
	Double dist=earthRadius*c
	switch (unit!=null ? unit:'m'){
		case 'km':
		case 'kilometer':
		case 'kilometers':
			return [t:'decimal', v:dist/1000.0D]
		case 'mi':
		case 'mile':
		case 'miles':
			return [t:'decimal', v:dist/1609.3440D]
		case 'ft':
		case 'foot':
		case 'feet':
			return [t:'decimal', v:dist/0.3048D]
		case 'yd':
		case 'yard':
		case 'yards':
			return [t:'decimal', v:dist/0.9144D]
	}
	return [t:'decimal', v:dist]
}

/** json encodes data as a JSON string							**/
/** Usage: json(value[, pretty])							**/
private Map func_json(Map rtD, List params){
	if(!checkParams(rtD, params,1) || (Integer)params.size()>2){
		return [t:'error', v:'Expecting json(value[, format])']
	}
	def builder=new groovy.json.JsonBuilder([params[0].v])
	String op=params[1] ? 'toPrettyString':'toString'
	String json=builder."${op}"()
	return [t:'string', v:json[1..-2].trim()]
}

/** urlencode encodes data for use in a URL						**/
/** Usage: urlencode(value)								**/
private Map func_urlencode(Map rtD, List params){
	if(!checkParams(rtD, params,1)){
		return [t:'error', v:'Expecting urlencode(value])']
	}
	// URLEncoder converts spaces to+which is then indistinguishable from any
	// actual+characters in the value. Match encodeURIComponent in ECMAScript
	// which encodes "a+b c" as "a+b%20c" rather than URLEncoder's "a+b+c"
	String t0=(String)evaluateExpression(rtD, (Map)params[0], 'string').v
	String value=(t0!=null ? t0:'').replaceAll('\\+', '__wc_plus__')
	return [t:'string', v:URLEncoder.encode(value, 'UTF-8').replaceAll('\\+', '%20').replaceAll('__wc_plus__', '+')]
}
private Map func_encodeuricomponent(Map rtD, List params){ return func_urlencode(rtD, params)}

/** COMMON PUBLISHED METHODS							**/

private String mem(Boolean showBytes=true){
	String mbytes=new groovy.json.JsonOutput().toJson(state)
	Integer bytes=(Integer)mbytes.length()
	return Math.round(100.0D*(bytes/100000.0D))+"%${showBytes ? " ($bytes bytes)".toString() : ""}"
}

private String runTimeHis(Map rtD){
	String myId=(String)rtD.id
	return 'Total run history: '+(theCacheFLD[myId].runTimeHis).toString()+'<br>' +
		'Last run details: '+(theCacheFLD[myId].runStats).toString()
}

/** UTILITIES									**/

private String md5(String md5){
	java.security.MessageDigest md=java.security.MessageDigest.getInstance('MD5')
	byte[] array=md.digest(md5.getBytes())
	String result=''
	for (Integer i=0; i<array.length; ++i){
		result += Integer.toHexString((array[i] & 0xFF)| 0x100).substring(1,3)
	}
	return result
}

@Field static Map theHashMapFLD

private String hashId(id, Boolean updateCache=true){
	String result
	String myId=id.toString()
	if(theHashMapFLD!=null) result=(String)theHashMapFLD[myId]
	else theHashMapFLD=[:]
	if(result==(String)null){
		result=':'+md5('core.'+myId)+':'
		if(updateCache) theHashMapFLD[myId]=result
	}
	return result
}

private getThreeAxisOrientation(value, Boolean getIndex=false){
	if(value instanceof Map){
		if((value.x!=null)&& (value.y!=null)&& (value.z!=null)){
			Integer x=Math.abs(value.x)
			Integer y=Math.abs(value.y)
			Integer z=Math.abs(value.z)
			Integer side=(x>y ? (x>z ? 0:2):(y>z ? 1:2))
			side+= ( (side==0 && value.x<0) || (side==1 && value.y<0) || (side==2 && value.z<0) ? 3:0 )
			List orientations=['rear', 'down', 'left', 'front', 'up', 'right']
			def result=getIndex ? side : (String)orientations[side]+' side up'
			return result
		}
	}
	return value
}

private Long getTimeToday(Long time){
	Long t0=getMidnightTime()
	Long result=time+t0
	//we need to adjust for time overlapping during DST changes
	return Math.round(result+((Integer)location.timeZone.getOffset(t0)-(Integer)location.timeZone.getOffset(result))*1.0D)
}

@Field final List trueStrings= [ '1', 'true',  "on",  "open",   "locked",   "active",   "wet",             "detected",     "present",     "occupied",     "muted",   "sleeping"]
@Field final List falseStrings=[ '0', 'false', "off", "closed", "unlocked", "inactive", "dry", "clear",    "not detected", "not present", "not occupied", "unmuted", "not sleeping", "null"]

private cast(Map rtD, value, String dataType, String srcDataType=(String)null){
	if(dataType=='dynamic')return value
	if(value==null){
		value=''
		srcDataType='string'
	}
	value=(value instanceof GString)? "$value".toString():value //get rid of GStrings
	if(srcDataType==(String)null || (Integer)srcDataType.length()==0 || srcDataType=='boolean' || srcDataType=='dynamic'){
		if(value instanceof List){srcDataType='device'}else
		if(value instanceof Boolean){srcDataType='boolean'}else
		if(value instanceof String){srcDataType='string'}else
		if(value instanceof Integer){srcDataType='integer'}else
		if(value instanceof BigInteger){srcDataType='long'}else
		if(value instanceof Long){srcDataType='long'}else
		if(value instanceof Double){srcDataType='decimal'}else
		if(value instanceof Float){srcDataType='decimal'}else
		if(value instanceof BigDecimal){srcDataType='decimal'}else
		if(value instanceof Map && value.x!=null && value.y!=null && value.z!=null){srcDataType='vector3'}else{
			value="$value".toString()
			srcDataType='string'
		}
	}
	//overrides
	switch (srcDataType){
		case 'bool': srcDataType='boolean'; break
		case 'number': srcDataType='decimal'; break
		case 'enum': srcDataType='string'; break
	}
	switch (dataType){
		case 'bool': dataType='boolean'; break
		case 'number': dataType='decimal'; break
		case 'enum': dataType='string'; break
	}
	if((Boolean)rtD.eric) myDetail rtD, "cast $srcDataType $value as $dataType"
	switch (dataType){
		case 'string':
		case 'text':
			switch (srcDataType){
				case 'boolean': return value ? 'true':'false'
				case 'decimal':
					//if(value instanceof Double)return sprintf('%f', value)
					// strip trailing zeroes (e.g. 5.00 to 5 and 5.030 to 5.03)
					return value.toString().replaceFirst(/(?:\.|(\.\d*?))0+$/, '$1')
				case 'integer':
				case 'long': break
				case 'time': return formatLocalTime(value, 'h:mm:ss a z')
				case 'date': return formatLocalTime(value, 'EEE, MMM d yyyy')
				case 'datetime': return formatLocalTime(value)
				case 'device': return buildDeviceList(rtD, value)
			}
			return "$value".toString()
		case 'integer':
			switch (srcDataType){
				case 'string':
					value=value.replaceAll(/[^-\d.-E]/, '')
					if(value.isInteger())
						return value.toInteger()
					if(value.isFloat())
						return (Integer)Math.floor(value.toDouble())
					if(value in trueStrings)
						return (Integer)1
					break
				case 'boolean': return (Integer)(value ? 1:0)
			}
			Integer result=0
			try{
				result=(Integer)value
			}catch(all){
				result=0
			}
			return result
		case 'long':
			switch (srcDataType){
				case 'string':
					value=value.replaceAll(/[^-\d.-E]/, '')
					if(value.isLong())
						return value.toLong()
					if(value.isInteger())
						return (Long)value.toInteger()
					if(value.isFloat())
						return (Long)Math.floor(value.toDouble())
					if(value in trueStrings)
						return 1L
					break
				case 'boolean': return (value ? 1L:0L)
			}
			Long result=0L
			try{
				result=(Long)value
			}catch(all){
				result=0L
			}
			return result
		case 'decimal':
			switch (srcDataType){
				case 'string':
					value=value.replaceAll(/[^-\d.-E]/, '')
					if(value.isDouble())
						return (Double)value.toDouble()
					if(value.isFloat())
						return (Double)value.toDouble()
					if(value.isLong())
						return (Double)value.toLong()
					if(value.isInteger())
						return (Double)value.toInteger()
					if(value in trueStrings)
						return 1.0D
					break
				case 'boolean': return (Double)(value ? 1.0D:0.0D)
			}
			Double result=0.0D
			try{
				result=(Double)value
			}catch(all){
			}
			return result
		case 'boolean':
			switch (srcDataType){
				case 'integer':
				case 'decimal':
				case 'boolean':
					return !!value
				case 'device':
					return value instanceof List && (Integer)value.size()>0
			}
			if(value){
				if("$value".toLowerCase().trim() in trueStrings)return true
				if("$value".toLowerCase().trim() in falseStrings)return false
			}
			return !!value
		case 'time':
			if("$value".isNumber() && value<86400000) return value
			Long d= srcDataType=='string' ? stringToTime(value):(Long)value // (Long)cast(rtD, value, 'long')
			Date t1=new Date(d)
			Long t2=Math.round(((Integer)t1.hours*3600.0D+(Integer)t1.minutes*60.0D+(Integer)t1.seconds)*1000.0D)
			return t2
		case 'date':
			if(srcDataType=='time' && value<86400000) value=getTimeToday(value)
			Long d=(srcDataType=='string')? stringToTime(value):(Long)value // (Long)cast(rtD, value, 'long')
			Date t1=new Date(d)
			Long t2=Math.round((Math.floor(d/1000.0D)*1000.0D)-(((Integer)t1.hours*3600.0D+(Integer)t1.minutes*60.0D+(Integer)t1.seconds)*1000.0D)) // take ms off and first guess at midnight (could be earlier/later depending if DST change day
			Long t3=Math.round(t2-(1.0D*3600000.0D)) // guess at 11 PM
			Long t4=Math.round(t2+(4.0D*3600000.0D)) // guess at 04 AM
			Long t5=Math.round(t2+(3.0D*3600000.0D)+((Integer)location.timeZone.getOffset(t3)-(Integer)location.timeZone.getOffset(t4))) // normalize to 3:00 AM for DST
			return t5
		case 'datetime':
			if(srcDataType=='time' && value<86400000) return getTimeToday(value)
			return (srcDataType=='string')? stringToTime(value):(Long)value // (Long)cast(rtD, value, 'long')
		case 'vector3':
			return value instanceof Map && value.x!=null && value.y!=null && value.z!=null ? value : [x:0, y:0, z:0]
		case 'orientation':
			return getThreeAxisOrientation(value)
		case 'ms':
		case 's':
		case 'm':
		case 'h':
		case 'd':
		case 'w':
		case 'n':
		case 'y':
			Long t1=0L
			switch (srcDataType){
				case 'integer':
				case 'long':
					t1=value; break
				default:
					t1=(Long)cast(rtD, value, 'long')
			}
			switch (dataType){
				case 'ms': return t1
				case 's': return Math.round(t1*1000.0D)
				case 'm': return Math.round(t1*60000.0D)
				case 'h': return Math.round(t1*3600000.0D)
				case 'd': return Math.round(t1*86400000.0D)
				case 'w': return Math.round(t1*604800000.0D)
				case 'n': return Math.round(t1*2592000000.0D)
				case 'y': return Math.round(t1*31536000000.0D)
			}
		case 'device':
		//device type is an array of device Ids
			if(value instanceof List){
				Boolean a=value.removeAll{ !it }
				return value
			}
			String v=(String)cast(rtD, value, 'string')
			if(v!=(String)null)return [v]
			return []
	}
	//anything else...
	return value
}

private Date utcToLocalDate(dateOrTimeOrString=null){ // this is really cast something to Date
	if(dateOrTimeOrString instanceof String){
		dateOrTimeOrString=stringToTime((String)dateOrTimeOrString)
	}
	if(dateOrTimeOrString instanceof Date){
		//get unix time
		dateOrTimeOrString=(Long)dateOrTimeOrString.getTime()
	}
	if(dateOrTimeOrString==null || dateOrTimeOrString==0L){
		dateOrTimeOrString=now()
	}
	if(dateOrTimeOrString instanceof Long){
		//HE adjusts Date fields (except for getTime()to local timezone of hub)
		return new Date((Long)dateOrTimeOrString)
	}
	return null
}

private Date localDate(){ return utcToLocalDate()}

private Long localTime(){ return now()} //utcToLocalTime()}

private Long stringToTime(dateOrTimeOrString){ // this is convert something to time
	if(dateOrTimeOrString instanceof String){
		Long result

		try{
			result=(new Date()).parse(dateOrTimeOrString)
			return result
		}catch (all0){
		}

		try{
			//get unix time
			if(!(dateOrTimeOrString =~ /(\s[A-Z]{3}((\+|\-)[0-9]{2}\:[0-9]{2}|\s[0-9]{4})?$)/)){
				def newDate=(new Date()).parse(dateOrTimeOrString+' '+formatLocalTime(now(), 'Z'))
				result=newDate
				return result
			}
			result=(new Date()).parse(dateOrTimeOrString)
			return result
		}catch (all){
		}

		try{
			Date tt1=toDateTime(dateOrTimeOrString)
			result=(Long)tt1.getTime()
			return result
		}catch(all3){
		}

		try{
			def tz=location.timeZone
			if(dateOrTimeOrString =~ /\s[A-Z]{3}$/){ // this is not the timezone... strings like CET are not unique.
				try{
					tz=TimeZone.getTimeZone(dateOrTimeOrString[-3..-1])
					dateOrTimeOrString=dateOrTimeOrString.take((Integer)dateOrTimeOrString.size()-3).trim()
				}catch (all4){
				}
			}

			String t0=dateOrTimeOrString?.trim()?: ''
			Boolean hasMeridian=false
			Boolean hasAM=false
			if(t0.toLowerCase().endsWith('am')){
				hasMeridian=true
				hasAM=true
			}
			if(t0.toLowerCase().endsWith('pm')){
				hasMeridian=true
				hasAM=false
			}
			if(hasMeridian)t0=t0[0..-3].trim()

			Long time=timeToday(t0, tz).getTime()//DST

			if(hasMeridian){
				Date t1=new Date(time)
				Integer hr=(Integer)t1.hours
				Integer min=(Integer)t1.minutes
				Boolean twelve=hr==12 ? true:false
				if(twelve && hasAM)hr -= 12
				if(!twelve && !hasAM)hr += 12
				String str1="${hr}".toString()
				String str2="${min}".toString()
				if(hr<10)str1=String.format('%02d', hr)
				if(min<10)str2=String.format('%02d', min)
				String str=str1+':'+str2
				time=timeToday(str, tz).getTime()
			}
			result=time
			return result
		}catch (all5){
		}

		result=(new Date()).getTime()
		return result
	}

	if(dateOrTimeOrString instanceof Date){
		dateOrTimeOrString=(Long)dateOrTimeOrString.getTime()
	}
	if("$dateOrTimeOrString".isNumber()){
		if(dateOrTimeOrString<86400000)dateOrTimeOrString=getTimeToday(dateOrTimeOrString)
		return dateOrTimeOrString
	}
	return 0L
}

private String formatLocalTime(time, String format='EEE, MMM d yyyy @ h:mm:ss a z'){
	def nTime=time
	if("$time".isNumber()){
		Long ltime=time.toLong()
		if(ltime<86400000L)ltime=getTimeToday(ltime)
// deal with a time in sec (vs. ms)
		if(ltime<Math.round(now()/1000.0D+86400.0D*365.0D))ltime=Math.round(time*1000.0D)
		nTime=new Date(ltime)
	}else if(time instanceof String){
		//get time
		nTime=new Date(stringToTime((String)time))
	}
	if(!(nTime instanceof Date)){
		return (String)null
	}
	def formatter=new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
	return formatter.format(nTime)
}

private Map hexToColor(String hex){
	hex=hex!=null ? hex:'000000'
	if(hex.startsWith('#'))hex=hex.substring(1)
	if((Integer)hex.size()!=6)hex='000000'
	List myHsl=hexToHsl(hex)
	return [
		hue: Math.round(myHsl[0]),
		saturation: myHsl[1],
		level: myHsl[2],
		hex: '#'+hex
	]
}

private static Double _hue2rgb(Double p, Double q, Double t){
	if(t<0.0D)t += 1.0D
	if(t>=1.0D)t -= 1.0D
	if(t<1.0D/6.0D)return p+(q-p)*6.0D*t
	if(t<1.0D/2.0D)return q
	if(t<2.0D/3.0D)return p+(q-p)*(2.0D/3.0D-t)*6.0D
	return p
}

private String hslToHex(hue, saturation, level){
	Double h=hue/360.0D
	Double s=saturation/100.0D
	Double l=level/100.0D
// argument checking for user calls
	if(h<0.0D)h=0.0D
	if(h>1.0D)h=1.0D
	if(s<0.0D)s=0.0D
	if(s>1.0D)s=1.0D
	if(l<0.0D)l=0.0D
	if(l>1.0D)l=1.0D

	Double r, g, b
	if(s==0.0D){
		r=g=b=l // achromatic
	}else{
		Double q=l<0.5D ? l*(1.0D+s) : l+s-(l*s)
		Double p=2.0D*l-q
		r=_hue2rgb(p, q, h+1.0D/3.0D)
		g=_hue2rgb(p, q, h)
		b=_hue2rgb(p, q, h-1.0D/3.0D)
	}

	return sprintf('#%02X%02X%02X', Math.round(r*255.0D), Math.round(g*255.0D), Math.round(b*255.0D))
}

private Map hexToRgb(String hex){
	hex=hex!=null ? hex:'000000'
	if(hex.startsWith('#'))hex=hex.substring(1)
	if((Integer)hex.size()!=6)hex='000000'
	Integer r1=Integer.parseInt(hex.substring(0, 2), 16)
	Integer g1=Integer.parseInt(hex.substring(2, 4), 16)
	Integer b1=Integer.parseInt(hex.substring(4, 6), 16)
	return [r:r1, g:g1, b:b1]
}

private List hexToHsl(String hex){
	hex=hex!=null ? hex:'000000'
	if(hex.startsWith('#'))hex=hex.substring(1)
	if((Integer)hex.size()!=6)hex='000000'
	Double r=Integer.parseInt(hex.substring(0, 2), 16)/255.0D
	Double g=Integer.parseInt(hex.substring(2, 4), 16)/255.0D
	Double b=Integer.parseInt(hex.substring(4, 6), 16)/255.0D

	Double max=Math.max(Math.max(r, g), b)
	Double min=Math.min(Math.min(r, g), b)
	Double h, s, l=(max+min)/2.0D

	if(max==min){
		h=s=0.0D // achromatic
	}else{
		Double d=max-min
		s=l>0.5D ? d/(2.0D-max-min) : d/(max+min)
		switch(max){
			case r: h=(g-b)/d+(g<b ? 6.0D:0.0D); break
			case g: h=(b-r)/d+2.0D; break
			case b: h=(r-g)/d+4.0D; break
		}
		h /= 6.0D
	}
	return [Math.round(h*360.0D), Math.round(s*100.0D), Math.round(l*100.0D)]
}

//hubitat device ids can be the same as the location id
private Boolean isDeviceLocation(device){
	return (String)device.id.toString()==(String)location.id.toString() && (device?.hubs?.size()?: 0)>0
}

/**							**/
/** DEBUG FUNCTIONS					**/
/**							**/
private void myDetail(Map rtD, String msg, Integer shift=-2){
	Map a=log(msg, rtD, shift, null, 'warn', true, false)
}

private Map log(message, Map rtD, Integer shift=-2, err=null, String cmd=(String)null, Boolean force=false, Boolean svLog=true){
	if(cmd=='timer'){
		return [m:message.toString(), t:now(), s:shift, e:err]
	}
	if(message instanceof Map){
		shift=message.s
		err=message.e
		message=(String)message.m+" (${now()-(Long)message.t}ms)".toString()
	}
	String myMsg=message.toString()
	cmd=cmd ? cmd:'debug'
	//shift is
	// 0 - initialize level, level set to 1
	// 1 - start of routine, level up
	// -1 - end of routine, level down
	// anything else - nothing happens
	Integer maxLevel=4
	Integer level=rtD?.debugLevel ? (Integer)rtD.debugLevel:0
	String prefix="║"
	String prefix2="║"
	String pad="" //"░"
	switch (shift){
		case 0:
			level=0
		case 1:
			level += 1
			prefix="╚"
			prefix2="╔"
			pad="═"
			break
		case -1:
			level -= 1
			pad="═"
			prefix="╔"
			prefix2="╚"
			break
	}

	if(level>0){
		prefix=prefix.padLeft(level+(shift==-1 ? 1:0), "║")
		prefix2=prefix2.padLeft(level+(shift==-1 ? 1:0), "║")
	}

	rtD.debugLevel=level
	Boolean hasErr=(err!=null && !!err)

	if(svLog && rtD!=null && rtD instanceof Map && rtD.logs instanceof List){
		myMsg=myMsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, "\r")
		if((Integer)myMsg.size()>1024){
			myMsg=myMsg[0..1023]+'...[TRUNCATED]'
		}
		List msgs=!hasErr ? myMsg.tokenize("\r"):[myMsg]
		for(msg in msgs){
			Boolean a=((List)rtD.logs).push([o: now()-(Long)rtD.timestamp, p: prefix2, m: msg+(hasErr ? " $err".toString() : ''), c: cmd])
		}
	}
	if(hasErr) log."$cmd" "$prefix $myMsg $err"
	else{
		if((cmd=='error' || cmd=='warn')|| force || !svLog || (Boolean)rtD.logsToHE || (Boolean)rtD.eric)log."$cmd" "$prefix $myMsg".toString()
	}
	return [:]
}

private void info(message, Map rtD, Integer shift=-2, err=null){ Map a=log(message, rtD, shift, err, 'info')}
private void trace(message, Map rtD, Integer shift=-2, err=null){ Map a=log(message, rtD, shift, err, 'trace')}
private void debug(message, Map rtD, Integer shift=-2, err=null){ Map a=log(message, rtD, shift, err, 'debug')}
private void warn(message, Map rtD, Integer shift=-2, err=null){ Map a=log(message, rtD, shift, err, 'warn')}
private void error(message, Map rtD, Integer shift=-2, err=null){ Map a=log(message, rtD, shift, err, 'error')}
private Map timer(String message, Map rtD, Integer shift=-2, err=null){ log(message, rtD, shift, err, 'timer')}

private void tracePoint(Map rtD, String objectId, Long duration, value){
	if(objectId!=(String)null && rtD!=null && rtD.trace!=null){
		rtD.trace.points[objectId]=[o: Math.round(1.0D*now()-(Long)rtD.trace.t-duration), d: duration, v: value]
	}else{
		error "Invalid object ID $objectID for trace point...", rtD
	}
}

private static Map weekDays(){
	return [
		0: 'Sunday',
		1: 'Monday',
		2: 'Tuesday',
		3: 'Wednesday',
		4: 'Thursday',
		5: 'Friday',
		6: 'Saturday'
	]
}

private static Map yearMonths(){
	return [
		1: 'January',
		2: 'February',
		3: 'March',
		4: 'April',
		5: 'May',
		6: 'June',
		7: 'July',
		8: 'August',
		9: 'September',
		10: 'October',
		11: 'November',
		12: 'December'
	]
}

@Field static Map svSunTFLD

private void initSunriseAndSunset(Map rtD){
	Map t0=svSunTFLD
	Long t=now()
	if(t0!=null){
		if(t<(Long)t0.nextM){
			rtD.sunTimes=[:]+t0
		}else{ t0=null; svSunTFLD=null; rtD.nextsunrise==null; rtD.nextsunset=null }
	}
	if(t0==null){
		Map sunTimes=app.getSunriseAndSunset()
		if(sunTimes.sunrise==null){
			warn 'Actual sunrise and sunset times are unavailable; please reset the location for your hub', rtD
			Long t1=getMidnightTime()
			sunTimes.sunrise=new Date(Math.round(t1+7.0D*3600000.0D))
			sunTimes.sunset=new Date(Math.round(t1+19.0D*3600000.0D))
			t=0L
		}
		t0=[
			sunrise: (Long)sunTimes.sunrise.getTime(),
			sunset: (Long)sunTimes.sunset.getTime(),
			updated: t,
			nextM: getNextMidnightTime()
		]
		rtD.sunTimes=t0
		if(t)svSunTFLD=t0
		if(eric())log.debug 'updating global sunrise'
	}
	rtD.sunrise=rtD.sunTimes.sunrise
	rtD.sunset=rtD.sunTimes.sunset
}

private Long getSunriseTime(Map rtD){
	initSunriseAndSunset(rtD)
	return (Long)rtD.sunrise
}

private Long getSunsetTime(Map rtD){
	initSunriseAndSunset(rtD)
	return (Long)rtD.sunset
}

private Long getNextSunriseTime(Map rtD){
	if(rtD.nextsunrise==null)rtD.nextsunrise=getNextOccurance(rtD, 'Sunrise')
	return (Long)rtD.nextsunrise
}

private Long getNextSunsetTime(Map rtD){
	if(rtD.nextsunset==null)rtD.nextsunset=getNextOccurance(rtD, 'Sunset')
	return (Long)rtD.nextsunset
}

// This is trying to ensure we don't fire sunsets or sunrises twice in same day by ensuring we fire a bit later than actual sunrise or sunset
private Long getNextOccurance(Map rtD, String ttyp){
	Long t0=(Long)"get${ttyp}Time"(rtD)
	if(now()>t0){
		List t1=getLocationEventsSince("${ttyp.toLowerCase()}Time", new Date()-2)
		def t2
		if((Integer)t1.size()>0) t2=t1[0]
		if(t2!=null && t2.value){
			Long a=Math.round(stringToTime((String)t2.value)+1000L*1.0D)
			if(a>now())return a
		}
	}
	Long t4=Math.round(t0+86400000.0D)
	t4=Math.round(t4+((Integer)location.timeZone.getOffset(t0)-(Integer)location.timeZone.getOffset(t4))*1.0D)

	Date t1=new Date(t4)
	Integer curMon=(Integer)t1.month
	curMon=location.latitude>0 ? curMon:((curMon+6)%12) // normalize for southern hemisphere

	Integer addr=0
	if((curMon>5 && ttyp=='Sunset')|| (curMon<=5 && ttyp=='Sunrise'))addr=1000 // minimize skew when sunrise or sunset moving earlier in day
	else{
		Integer t2=Math.abs(location.latitude)
		Integer t3=curMon%6
		Integer t5=(Integer)Math.round(t3*365.0D/12.0D+(Integer)t1.date) // days into period
		addr=Math.round((t5>37 && t5<(182-37)? t2*2.8D:t2*1.9D)*1000.0D)
	}
	return t4+addr
}

private Long getMidnightTime(){
	return timeToday('00:00', location.timeZone).getTime()
}

private Long getNextMidnightTime(){
	return timeTodayAfter('23:59', '00:00', location.timeZone).getTime()
}

private Long getNoonTime(Map rtD=null){
	return timeToday('12:00', location.timeZone).getTime()
}

private Long getNextNoonTime(Map rtD=null){
	return timeTodayAfter('23:59', '12:00', location.timeZone).getTime()
}

private void getLocalVariables(Map rtD, List vars, Map atomState){
	rtD.localVars=[:]
	Map values=atomState.vars
	for (var in vars){
		String t0=(String)var.t
		def t1=values[(String)var.n]
		Map variable=[t:t0, v:var.v!=null ? var.v:(t0.endsWith(']') ? (t1 instanceof Map ? t1:[:]):cast(rtD, t1, t0)), f: !!var.v] //f means fixed value - we won't save this to the state
		if(var.v!=null && (String)var.a=='s' && !t0.endsWith(']')) variable.v=evaluateExpression(rtD, (Map)evaluateOperand(rtD, null, (Map)var.v), t0).v
		rtD.localVars[(String)var.n]=variable
	}
}

private Map getSystemVariablesAndValues(Map rtD){
	Map result=[:]+getSystemVariables
	for(variable in result){
		String keyt1=(String)variable.key
		if(variable.value.d!=null && (Boolean)variable.value.d) variable.value.v=getSystemVariableValue(rtD, keyt1)
		else{
			Map t0=rtD.cachePersist
			t0=t0!=null ? t0:[:]
			if(t0[keyt1]!=null)variable.value.v=t0[keyt1].v
		}
	}
	return result
}
// UI will not display anything that starts with $current or $previous;  variables without d: true will not display variable value
@Field final Map getSystemVariables=[
		'$args':[t:'dynamic', v:null],
		'$json':[t:'dynamic', d:true],
		'$places':[t:'dynamic', d:true],
		'$response':[t:'dynamic', d:true],
		'$nfl':[t:'dynamic', d:true],
		'$weather':[t:'dynamic', d:true],
		'$incidents':[t:'dynamic', d:true],
		'$hsmTripped':[t:'boolean', d:true],
		'$hsmStatus':[t:'string', d:true],
		'$httpContentType':[t:'string', v:null],
		'$httpStatusCode':[t:'integer', v:null],
		'$httpStatusOk':[t:'boolean', v:null],
		'$currentEventAttribute':[t:'string', v:null],
		'$currentEventDescription':[t:'string', v:null],
		'$currentEventDate':[t:'datetime', v:null],
		'$currentEventDelay':[t:'integer', v:null],
		'$currentEventDevice':[t:'device', v:null],
		'$currentEventDeviceIndex':[t:'integer', v:null],
		'$currentEventDevicePhysical':[t:'boolean', v:null],
//		'$currentEventReceived':[t:'datetime', v:null],
		'$currentEventValue':[t:'dynamic', v:null],
		'$currentEventUnit':[t:'string', v:null],
//		'$currentState':[t:'string', v:null],
//		'$currentStateDuration':[t:'string', v:null],
//		'$currentStateSince':[t:'datetime', v:null],
//		'$nextScheduledTime':[t:'datetime', v:null],
		'$name':[t:'string', d:true],
		'$state':[t:'string', d:true],
		'$device':[t:'device', v:null],
		'$devices':[t:'device', v:null],
		'$index':[t:'decimal', v:null],
		'$iftttStatusCode':[t:'integer', v:null],
		'$iftttStatusOk':[t:'boolean', v:null],
		'$location':[t:'device', v:null],
		'$locationMode':[t:'string', d:true],
		'$localNow':[t:'datetime', d:true],
		'$now':[t:'datetime', d:true],
		'$hour':[t:'integer', d:true],
		'$hour24':[t:'integer', d:true],
		'$minute':[t:'integer', d:true],
		'$second':[t:'integer', d:true],
		'$meridian':[t:'string', d:true],
		'$meridianWithDots':[t:'string', d:true],
		'$day':[t:'integer', d:true],
		'$dayOfWeek':[t:'integer', d:true],
		'$dayOfWeekName':[t:'string', d:true],
		'$month':[t:'integer', d:true],
		'$monthName':[t:'string', d:true],
		'$year':[t:'integer', d:true],
		'$midnight':[t:'datetime', d:true],
		'$noon':[t:'datetime', d:true],
		'$sunrise':[t:'datetime', d:true],
		'$sunset':[t:'datetime', d:true],
		'$nextMidnight':[t:'datetime', d:true],
		'$nextNoon':[t:'datetime', d:true],
		'$nextSunrise':[t:'datetime', d:true],
		'$nextSunset':[t:'datetime', d:true],
		'$time':[t:'string', d:true],
		'$time24':[t:'string', d:true],
		'$utc':[t:'datetime', d:true],
		'$mediaId':[t:'string', d:true],
		'$mediaUrl':[t:'string', d:true],
		'$mediaType':[t:'string', d:true],
		'$mediaSize':[t:'integer', d:true],
		'$previousEventAttribute':[t:'string', v:null],
		'$previousEventDescription':[t:'string', v:null],
		'$previousEventDate':[t:'datetime', v:null],
		'$previousEventDelay':[t:'integer', v:null],
		'$previousEventDevice':[t:'device', v:null],
		'$previousEventDeviceIndex':[t:'integer', v:null],
		'$previousEventDevicePhysical':[t:'boolean', v:null],
//		'$previousEventExecutionTime':[t:'integer', v:null],
//		'$previousEventReceived':[t:'datetime', v:null],
		'$previousEventValue':[t:'dynamic', v:null],
		'$previousEventUnit':[t:'string', v:null],
//		'$previousState':[t:'string', v:null],
//		'$previousStateDuration':[t:'string', v:null],
//		'$previousStateSince':[t:'datetime', v:null],
		'$random':[t:'decimal', d:true],
		'$randomColor':[t:'string', d:true],
		'$randomColorName':[t:'string', d:true],
		'$randomLevel':[t:'integer', d:true],
		'$randomSaturation':[t:'integer', d:true],
		'$randomHue':[t:'integer', d:true],
		'$temperatureScale':[t:'string', d:true],
		'$version':[t:'string', d:true],
		'$versionH':[t:'string', d:true]
	]

private getSystemVariableValue(Map rtD, String name){
	switch (name){
	//case '$args': return "${rtD.args}".toString()
	case '$json': return "${rtD.json}".toString()
	case '$places': return "${rtD.settings?.places}".toString()
	case '$response': return "${rtD.response}".toString()
	case '$weather': return "${rtD.weather}".toString()
	case '$nfl': return "${rtD.nfl}".toString()
	case '$incidents': return "${rtD.incidents}".toString()
	case '$hsmTripped': return rtD.incidents instanceof List && (Integer)rtD.incidents.size()>0
	case '$hsmStatus': return (String)location.hsmStatus
	case '$mediaId': return rtD.mediaId
	case '$mediaUrl': return (String)rtD.mediaUrl
	case '$mediaType': return (String)rtD.mediaType
	case '$mediaSize': return (rtD.mediaData!=null ? (Integer)rtD.mediaData.size():0)
	case '$name': return (String)app.label
	case '$state': return (String)rtD.state?.new
	case '$version': return version()
	case '$versionH': return HEversion()
	case '$now': return (Long)now()
	case '$utc': return (Long)now()
	case '$localNow': return (Long)localTime()
	case '$hour': Integer h=(Integer)localDate().hours; return (h==0 ? 12:(h>12 ? h-12:h))
	case '$hour24': return (Integer)localDate().hours
	case '$minute': return (Integer)localDate().minutes
	case '$second': return (Integer)localDate().seconds
	case '$meridian': Integer h=(Integer)localDate().hours; return (h<12 ? 'AM':'PM')
	case '$meridianWithDots': Integer h=(Integer)localDate().hours; return (h<12 ? 'A.M.':'P.M.')
	case '$day': return (Integer)localDate().date
	case '$dayOfWeek': return (Integer)localDate().day
	case '$dayOfWeekName': return (String)weekDays()[(Integer)localDate().day]
	case '$month': return (Integer)localDate().month+1
	case '$monthName': return (String)yearMonths()[(Integer)localDate().month+1]
	case '$year': return (Integer)localDate().year+1900
	case '$midnight': return getMidnightTime()
	case '$noon': return getNoonTime()
	case '$sunrise': return getSunriseTime(rtD)
	case '$sunset': return getSunsetTime(rtD)
	case '$nextMidnight': return getNextMidnightTime()
	case '$nextNoon': return getNextNoonTime()
	case '$nextSunrise': return getNextSunriseTime(rtD)
	case '$nextSunset': return getNextSunsetTime(rtD)
	case '$time': Date t=localDate(); Integer h=(Integer)t.hours; Integer m=(Integer)t.minutes; return ((h==0 ? 12:(h>12 ? h-12:h))+':'+(m<10 ? "0$m":"$m")+" "+(h <12 ? 'A.M.':'P.M.')).toString()
	case '$time24': Date t=localDate(); Integer h=(Integer)t.hours; Integer m=(Integer)t.minutes; return (h+':'+(m<10 ? "0$m":"$m")).toString()
	case '$random':
		def tresult=getRandomValue(rtD, name)
		Double result
		if(tresult!=null)result=(Double)tresult
		else{
			result=(Double)Math.random()
			setRandomValue(rtD, name, result)
		}
		return result
	case '$randomColor':
		def tresult=getRandomValue(rtD, name)
		String result
		if(tresult!=null)result=(String)tresult
		else{
			result=(String)(getRandomColor(rtD))?.rgb
			setRandomValue(rtD, name, result)
		}
		return result
	case '$randomColorName':
		def tresult=getRandomValue(rtD, name)
		String result
		if(tresult!=null)result=(String)tresult
		else{
			result=(String)(getRandomColor(rtD))?.name
			setRandomValue(rtD, name, result)
		}
		return result
	case '$randomLevel':
		def tresult=getRandomValue(rtD, name)
		Integer result
		if(tresult!=null)result=(Integer)tresult
		else{
			result=(Integer)Math.round(100.0D*Math.random())
			setRandomValue(rtD, name, result)
		}
		return result
	case '$randomSaturation':
		def tresult=getRandomValue(rtD, name)
		Integer result
		if(tresult!=null)result=(Integer)tresult
		else{
			result=(Integer)Math.round(50.0D+50.0D*Math.random())
			setRandomValue(rtD, name, result)
		}
		return result
	case '$randomHue':
		def tresult=getRandomValue(rtD, name)
		Integer result
		if(tresult!=null)result=(Integer)tresult
		else{
			result=(Integer)Math.round(360.0D*Math.random())
			setRandomValue(rtD, name, result)
		}
		return result
	case '$locationMode':return (String)location.getMode()
	case '$temperatureScale':return (String)location.getTemperatureScale()
	}
}

private static void setSystemVariableValue(Map rtD, String name, value, Boolean cachePersist=true){
//	if(name==null || !(name.startsWith('$')))return
	Map var=rtD.systemVars[name]
	if(var==null || var.d!=null)return
	rtD.systemVars[name].v=value

	if(cachePersist){
		if(name in [
			'$args',
			'$httpContentType',
			'$httpStatusCode',
			'$httpStatusOk',
			'$iftttStatusCode',
			'$iftttStatusOk' ]){

			Map t0=rtD.cachePersist
			t0=t0!=null ? t0:[:]
			t0[name]=[:]+var+[v:value]
			if(name=='$args')t0[name]=[:]+var+[v:"${value}".toString()]
			else t0[name]=[:]+var+[v:value]
			rtD.cachePersist=t0
		}
	}
}

private static getRandomValue(Map rtD, String name){
	rtD.temp=rtD.temp!=null ? rtD.temp:[randoms:[:]]
	return rtD.temp.randoms[name]
}

private static void setRandomValue(Map rtD, String name, value){
	rtD.temp=rtD.temp!=null ? rtD.temp:[randoms:[:]]
	rtD.temp.randoms[name]=value
}

private static void resetRandomValues(Map rtD){
	rtD.temp=[randoms:[:]]
}

private Map getColorByName(Map rtD, String name){
	Map t1=getColors().find{ (String)it.name==name }
	Map t2
	if(t1!=null){ t2=[:]+t1; return t2 }
	return t1
}

private Map getRandomColor(Map rtD){
	Integer random=Math.round(Math.random()*(Integer)getColors().size()*1.0D)
	Map t1=getColors()[random]
	Map t2
	if(t1!=null){ t2=[:]+t1; return t2 }
	return t1
}

private static Class HubActionClass(){
	return 'hubitat.device.HubAction' as Class
}

private static Class HubProtocolClass(){
	return 'hubitat.device.Protocol' as Class
}

private Boolean isHubitat(){
	return hubUID!=null
}

@Field static Map theAttributesFLD

//uses i, p, t, m
private Map Attributes(){
	Map result=null
	String mStr="getting attributes from parent"
	if(theAttributesFLD==null){
		result=(Map)parent.getChildAttributes()
		theAttributesFLD=result
		if(eric())log.debug mStr
	}
	result=theAttributesFLD
	if(result==null){
		if(eric())log.error "NO "+mStr
		result=[:]
	}
	return result
}

@Field static Map theComparisonsFLD

//uses p, t
private Map Comparisons(){
	Map result=null
	String mStr="getting comparisons from parent"
	if(theComparisonsFLD==null){
		Long rP=Math.round(1000.0D*Math.random())
		pauseExecution(rP)
		if(theComparisonsFLD==null){
			result=(Map)parent.getChildComparisons()
			theComparisonsFLD=result
			if(eric())log.debug mStr
		}
	}
	result=theComparisonsFLD
	if(result==null){
		if(eric())log.error "NO "+mStr
		result=[:]
	}
	return result
}

@Field static Map theVirtCommandsFLD

//uses o (override phys command), a (aggregate commands)
private Map VirtualCommands(){
	Map result=null
	String mStr="getting virt commands from parent"
	if(theVirtCommandsFLD==null){
		result=(Map)parent.getChildVirtCommands()
		theVirtCommandsFLD=result
		if(eric())log.debug mStr
	}
	result=theVirtCommandsFLD
	if(result==null){
		if(eric())log.error "NO "+mStr
		result=[:]
	}
	return result
}

//uses c and r
// the physical command r: is replaced with command c. If the VirtualCommand c exists and has o: true we will use that virtual command; otherwise it will be replaced with a physical command
@Field final Map CommandsOverrides=[
		push:[c:"push",	s:null, r:"pushMomentary"],
		flash:[c:"flash",	s:null, r:"flashNative"] //flash native command conflicts with flash emulated command. Also needs "o" option on command described later
]

@Field static Map theVirtDevicesFLD

//uses ac, o
private Map VirtualDevices(){
	Map result=null
	String mStr="getting virt devices from parent"
	if(theVirtDevicesFLD==null){
		if(eric())log.debug mStr
		result=(Map)parent.getChildVirtDevices()
		theVirtDevicesFLD=result
	}
	result=theVirtDevicesFLD
	if(result==null){
		if(eric())log.error 'NO '+mStr
		result=[:]
	}
	return result
}

@Field static Map thePhysCommandsFLD

//uses a, v
private Map PhysicalCommands(){
	Map result=null
	String mStr="getting commands from parent"
	if(thePhysCommandsFLD==null){
		result=(Map)parent.getChildCommands()
		thePhysCommandsFLD=result
		if(eric())log.debug mStr
	}
	result=thePhysCommandsFLD
	if(result==null){
		if(eric())log.error 'NO '+mStr
		result=[:]
	}
	return result
}

@Field static List theColorsFLD

private List getColors(){
	List result=null
	String mStr="getting colors from parent"
	if(theColorsFLD==null){
		result=(List)parent.getColors()
		theColorsFLD=result
		if(eric())log.debug mStr
	}
	result=theColorsFLD
	if(result==null){
		if(eric())log.error 'NO '+mStr
		result=[]
	}
	return result
}
