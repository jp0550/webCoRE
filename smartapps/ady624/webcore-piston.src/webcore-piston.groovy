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
 * Last update February 10, 2020 for Hubitat
*/
private static String version(){ return 'v0.3.110.20191009' }
private static String HEversion(){ return 'v0.3.110.20200210_HE' }

/*** webCoRE DEFINITION					***/

private static String handle(){ return 'webCoRE' }

import groovy.json.*
import hubitat.helper.RMUtils
import groovy.transform.Field

definition(
	name:"${handle()} Piston",
	namespace:'ady624',
	author:'Adrian Caramaliu',
	description:'Do not install this directly, use webCoRE instead',
	category:'Convenience',
	parent:"ady624:${handle()}",
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

private static Boolean beta(){ return false }
private static Boolean eric(){ return false }

/*** CONFIGURATION PAGES				***/

def pageMain(){
	return dynamicPage(name:'pageMain', title:'', install:true, uninstall: !!state.build){
		if(parent==null || !parent.isInstalled()){
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
				}else{
					paragraph 'Sorry your dashboard does not seem to be enabled; please go to the parent app and enable the dashboard.'
				}
			}

			section(sectionTitleStr('Application Info')){
				Map rtData=getTemporaryRunTimeData(now())
				if((Boolean)rtData.disabled){
					paragraph 'Piston is disabled by webCoRE'
				}
				if(!(Boolean)rtData.active){
					paragraph 'Piston is paused'
				}
				if(rtData.bin!=null){
					paragraph 'Automatic backup bin code: '+(String)rtData.bin
				}
				paragraph 'Version: '+version()
				paragraph 'VersionH: '+HEversion()
				paragraph 'Memory Usage: '+mem()
				paragraph 'RunTime History: '+runTimeHis(rtData)

			}

			section(sectionTitleStr('Recovery')){
				href 'pageRun', title:'Force-run this piston'
				href 'pageClear', title:'Clear all data except local variables', description:'You will lose all logs, trace points, statistics, but no variables'
				href 'pageClearAll', title:'Clear all data', description:'You will lose all data stored in any local variables'
			}

			section(){
				input 'dev', "capability.*", title:'Devices', description:'Piston devices', multiple:true
				input 'logging', 'enum', title:'Logging Level', options:[0:"None", 1:"Minimal", 2:"Medium", 3:"Full"], description:'Piston logging', defaultValue:state.logging?:0
				input 'logsToHE', 'bool', title:'Piston logs are also displayed in HE console logs?', description:"Logs are available in webCoRE console; also display in HE console 'Logs'?", defaultValue:false
				input 'maxStats', 'number', title:'Max number of timing history stats', description:'Max number of stats', defaultValue:100
				input 'maxLogs', 'number', title:'Max number of history logs', description:'Max number of logs', defaultValue:100
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

private void clear1(Boolean all=false){
	state.logs=[]
	state.stats=[:]
	state.trace=[:]
	if(all){
		state.cache=[:]
		state.vars=[:]
		state.store=[:]
		state.pauses=0L
	}

	String appStr=(app.id).toString()
	String tsemaphoreName='sph'+appStr
	if(theSempahoresFLD!=null)theSemaphoresFLD[tsemaphoreName]=0L
	String queueName='aevQ'+appStr
	if(theQueuesFLD!=null)theQueuesFLD[queueName]=[]

	cleanState()
	clearMyCache('clear1')
}

def pageClearAll(){
	clear1(true)
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
	Map rtData=getRunTimeData(null, null, false, false)
	String message=getMapDescStr(rtData.piston)
	return dynamicPage(name:'pageDumpPiston', title:'', uninstall:false){
		section('Piston dump'){
			paragraph message
		}
	}
}

/*** PUBLIC METHODS					***/

public Boolean isInstalled(){
	return !!state.created
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

void initialize(){
	if(settings.logging==null)Map a=setLoggingLevel(state.logging? state.logging.toString():'0')
	else if(settings.logging!=state.logging)Map a=setLoggingLevel(settings.logging.toString())
	cleanState()
	clearMyCache('initialize')

	if((Boolean)state.active){
		Map b=resume()
	}
}

private void cleanState(){
//cleanups between releases
	for(sph in state.findAll{ ((String)it.key).startsWith('sph')}){
		state.remove(sph.key.toString())
	}
	state.remove('hash')
	state.remove('piston')
	state.remove('cVersion')
	state.remove('hVersion')
	state.remove('disabled')
	state.remove('logPExec')
	state.remove('settings')
	state.remove('svSunT')
	state.remove('temp')
	state.remove('debugLevel')
}

public Map get(Boolean minimal=false){ // minimal is backup
	Map rtData=getRunTimeData()
	return [
		meta: [
			id: (String)rtData.id,
			author: (String)rtData.author,
			name: (String)rtData.svLabel ?: (String)app.label,
			created: (Long)rtData.created,
			modified: (Long)rtData.modified,
			build: (Integer)rtData.build,
			bin: (String)rtData.bin,
			active: (Boolean)rtData.active,
			category: rtData.category
		],
		piston: rtData.piston
	]+(minimal ? [:]:[ // use state as getRunTimeData re-initializes these
		systemVars: getSystemVariablesAndValues(rtData),
		subscriptions: state.subscriptions,
		state: state.state,
		logging: (Integer)state.logging ?: 0,
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
	if(t0==[:])return t0
	List logs=[]+(List)t0.logs
	Integer lsz=(Integer)logs.size()
	Long llt=lastLogTimestamp!=null && lastLogTimestamp instanceof String && ((String)lastLogTimestamp).isLong()? (Long)((String)lastLogTimestamp).toLong():0L
	Integer index=(llt!=0L && lsz>0)? logs.findIndexOf{ it?.t==llt }:0
	index=index>0 ? index:(llt!=0L ? 0:lsz)
	return [
		name: (String)t0.name,
		state: (Map)t0.state,
		logs: index ? logs[0..index-1]:[],
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
		if(pData!=null)thePistonCacheFLD[pisName].pis=null
	}
	if(eric())log.debug 'clearing piston-code-cache '+meth
}

private LinkedHashMap recreatePiston(Boolean shorten=false, Boolean useCache=true){
	if(shorten && useCache){
		if(thePistonCacheFLD==null)thePistonCacheFLD=[:]
		String pisName=(app.id).toString()
		Map pData=thePistonCacheFLD[pisName]
		if(pData==null || pData.cnt==null){
			pData=[cnt:0, pis:null]
			thePistonCacheFLD[pisName]=[:]+pData
			pData=thePistonCacheFLD[pisName]
		}
		Integer myCnt=(Integer)pData.cnt+1
		thePistonCacheFLD[pisName].cnt=myCnt
		if(pData.pis!=null){
			return [cache:true]+pData.pis
		}
	}

	String sdata=''
	Integer i=0
	while(true){
		String s=(String)settings?."chunk:$i"
		if(s!=null){
			sdata += s
		}else{ break }
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
	clear1(false)
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
	for(chunk in chunks){
		app.updateSetting((String)chunk.key, [type:'text', value:chunk.value])
	}
	app.updateSetting('bin', [type:'text', value:(String)state.bin ?: ''])
	app.updateSetting('author', [type:'text', value:(String)state.author ?: ''])

	state.pep=piston.o?.pep ? true:false

	if(data.n!=null && (Integer)data.n.length()>0){
		if(state.svLabel!=null && state.svLabel!=(String)null){
			String res=(String)state.svLabel
			app.updateLabel(res)
		}
		state.svLabel=(String)null
		app.updateLabel((String)data.n)
	}
	state.schedules=[]
	state.vars=state.vars ?: [:]
	state.modifiedVersion=version()
	clearMyCache(meth)

	Map rtData=[:]
	rtData.piston=piston
	if((Integer)state.build==1 || (Boolean)state.active){
		rtData=resume(piston)
	}else checkLabel()
	return [active:(Boolean)state.active, build:(Integer)state.build, modified:(Long)state.modified, state:state.state, rtData:rtData]
}

private void clearMsetIds(node){
	if(item==null)return
	for (list in node.findAll{ it.value instanceof List }){
		for (item in list.value.findAll{ it instanceof Map }){
			clearMsetIds(item)
		}
	}
	if(node instanceof Map){
		if(node['$']!=null)node.remove('$')
	}
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
			node.ei.removeAll{ !it.c && !it.s }
			for (elseIf in node.ei){
				id=elseIf['$']!=null ? (Integer)elseIf['$']:0
				if(id==0 || existingIds[id]!=null){
					Boolean a=requiringIds.push(elseIf)
				}else{
					maxId=(maxId<id)? id:maxId
					existingIds[id]=id
				}
			}
		}
		if(nodeT=='switch' && node.cs){
			for (_case in node.cs){
				id=_case['$']!=null ? (Integer)_case['$']:0
				if(id==0 || existingIds[id]!=null){
					Boolean a=requiringIds.push(_case)
				}else{
					maxId=(maxId<id)? id:maxId
					existingIds[id]=id
				}
			}
		}
		if(nodeT=='action' && node.k){
			for (task in (List)node.k){
				id=task['$']!=null ? (Integer)task['$']:0
				if(id==0 || existingIds[id]!=null){
					Boolean a=requiringIds.push(task)
				}else{
					maxId=(maxId<id)? id:maxId
					existingIds[id]=id
				}
			}
		}
	}
	for (list in node.findAll{ it.value instanceof List }){
		for (item in list.value.findAll{ it instanceof Map }){
			maxId=msetIds(shorten, item, maxId, existingIds, requiringIds, level+1)
		}
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
		for (itemA in list.value.findAll{ it instanceof Map }){
			cleanCode(itemA)
		}
	}
}

public Map deletePiston(){
	String meth='deletePiston'
	if(eric())log.debug meth
	clear1(true)	// calls clearMyCache(meth)
	clearParentCache(meth)
	clearMyPiston(meth)
	return [:]
}

public void settingsToState(String myKey, setval){
	String meth='setting to state '+myKey
	if(eric())log.debug meth
	if(setval){
		atomicState[myKey.toString()]=setval
	}else state.remove([myKey.toString()])
	clearParentCache(meth)
	clearMyCache(meth)
	clearMyPiston(meth)
	runIn(5, 'checkLabel')
}

private void checkLabel(Map rtData=null){
	if(rtData==null)rtData=getTemporaryRunTimeData(now())
	Boolean act=(Boolean)rtData.active
	Boolean dis=(Boolean)rtData.disabled
	Boolean found=state.svLabel==null || state.svLabel==(String)null || match((String)app.label, '<span')
	String meth='checkLabel'
	if(!act || dis){
		if(!found){
			state.svLabel=(String)app.label
			rtData.svLabel=(String)app.label
			String tstr=''
			if(!act){
				tstr='(Paused)'
			}
			if(dis){
				tstr='(Disabled) Kill switch is active'
			}
			String res=(String)app.label+" <span style='color:orange'>${tstr}</span>"
			app.updateLabel(res)
			clearMyCache(meth)
		}
	}else{
		if(found && state.svLabel!=null){
			String res=(String)state.svLabel
			app.updateLabel(res)
			state.svLabel=(String)null
			rtData.svLabel=(String)null
			clearMyCache(meth)
		}
	}
}

public void config(Map data){ // creates a new piston
	if(data==null){
		return
	}
	if((String)data.bin!=(String)null){
		state.bin=(String)data.bin
		app.updateSetting('bin', [type:'text', value:(String)state.bin])
	}
	if((String)data.author!=null){
		state.author=(String)data.author
		app.updateSetting('author', [type:'text', value:(String)state.author])
	}
	if((String)data.initialVersion!=null){
		state.initialVersion=(String)data.initialVersion
	}
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

	Map rtData=getRunTimeData()
	Map msg=timer 'Piston successfully stopped', rtData, -1
	if((Integer)rtData.logging>0)info 'Stopping piston...', rtData, 0
	state.schedules=[]
	rtData.stats.nextSchedule=0L
	state.nextSchedule=0L
	unsubscribe()
	unschedule()
	app.clearSetting('dev')
	state.trace=[:]
	state.subscriptions=[:]
	checkLabel(rtData)
	if((Integer)rtData.logging>0)info msg, rtData
	updateLogs(rtData)
	state.active=false
	clearMyCache('pauseP1')
	return rtData
}

public Map resume(LinkedHashMap piston=null){
	state.active=true
	state.subscriptions=[:]
	state.schedules=[]
	clearMyCache('resumeP')

	Map tempRtData=getTemporaryRunTimeData(now())
	Map msg=timer 'Piston successfully started', tempRtData, -1
	if(piston!=null)tempRtData.piston=piston
	Map rtData=getRunTimeData(tempRtData, null, true, false) //performs subscribeAll(rtData)
	if((Integer)rtData.logging>0)info 'Starting piston... ('+HEversion()+')', rtData, 0
	checkVersion(rtData)
	checkLabel(rtData)
	if((Integer)rtData.logging>0)info msg, rtData
	updateLogs(rtData)
	rtData.result=[active:true, subscriptions:state.subscriptions]
	state.active=true
	clearMyCache('resumeP1')
	return rtData
}

public Map setLoggingLevel(String level){
	Integer mlogging=level.isInteger()? level.toInteger():0
	if(mlogging<0)mlogging=0
	if(mlogging>3)mlogging=3
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
private Map lockOrQueueSemaphore(String semaphore, event, Boolean queue, Map rtData){
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
			def t0=theSemaphoresFLD[tsemaphoreName]
			Long tt0=t0!=null ? (Long)t0.toLong():0L
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
						t: (Long)event.date.getTime(),
						name: (String)event.name,
						value: event.value,
						descriptionText: (String)event.descriptionText,
						unit: event?.unit,
						physical: !!event.physical,
						jsonData: event?.jsonData,
					]+(event instanceof com.hubitat.hub.domain.Event ? [:]:[
						index: event?.index,
						recovery: event?.recovery,
						schedule: event?.schedule,
						contentType: (String)event?.contentType,
						responseData: event?.responseData,
						responseCode: event?.responseCode,
						setRtData: event?.setRtData
					])
					if(event.device!=null){
						myEvent.device=[id:event.device?.id, name:event.device?.name, label:event.device?.label]
						if(event.device?.hubs!=null){
							myEvent.device.hubs=[t:'tt']
						}
					}
					def mt0=theQueuesFLD[queueName]
					List eventlst=mt0!=null ? (List)mt0:[]
					Boolean a=eventlst.push(myEvent)
					theQueuesFLD[queueName]=eventlst
					didQ=true

					Integer qsize=(Integer)eventlst.size()
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
	Map rtData=[:]
	Map t0=getDSCache()
	Map t1=getParentCache()
	rtData=[:]+t0+t1
	rtData.temporary=true
	rtData.timestamp=startTime
	rtData.logs=[[t:startTime]]
	rtData.debugLevel=0
	rtData.eric=eric() && (Integer)rtData.logging>2
	return rtData
}

@Field static Map theCacheFLD // each piston has a map in here

private void clearMyCache(String meth=(String)null){
	String myId=hashId(app.id)
	if(!myId)return
	if(theCacheFLD!=null){
		theCacheFLD[myId]=null
	}
	if(eric())log.debug 'clearing cache '+meth
}

private Map getCachedMaps(Boolean retry=true){
	def result=[:]
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
	return [:]
}

private Map getDSCache(){
	Map result=[:]
	String appId=hashId(app.id)
	String myId=appId
	if(myId.length()<8){
		log.error 'getDSCache: no id '+myId
		return [:]
	}
	if(theCacheFLD==null)theCacheFLD=[:]
	def tresult=theCacheFLD[myId]
	if(tresult!=null){ result=(Map)tresult; result.stateAccess=null }

	if(result==[:] || result==null){
		Long stateStart=now()
		if(state.pep==null){ // upgrades of older pistons
			LinkedHashMap piston=recreatePiston()
			state.pep=piston.o?.pep ? true:false
		}
		Map t1=[
			id: appId,
			logging: state.logging!=null ? (Integer)state.logging:0,
			svLabel: (String)state.svLabel,
			name: state.svLabel!=null ? (String)state.svLabel:(String)app.label,
			active: (Boolean)state.active,
			category: state.category ?: 0,
			pep: (Boolean)state.pep,
			created: (Long)state.created,
			modified: (Long)state.modified,
			build: (Integer)state.build,
			author: (String)state.author,
			bin: (String)state.bin,
			logsToHE: settings?.logsToHE ? true:false,
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

		t1.devices=(settings.dev && (settings.dev instanceof List)? settings.dev.collectEntries{[(hashId(it.id)): it]}:[:])
		result=[:]+t1

		theCacheFLD[myId]=result
		if(eric())log.debug 'creating piston cache'
	}
	if(result==[:] || result==null){
		log.error 'no result getDSCache'
		return [:]
	}
	return [:]+result
}

@Field static Map theParentCacheFLD

public void clearParentCache(String meth=(String)null){
	theParentCacheFLD=null
	if(eric())log.debug "clearing parent cache $meth"
}

private Map getParentCache(){
	Map result=[:]
	String mStr='gathering parent cache'
	if(theParentCacheFLD==null)theParentCacheFLD=[:]
	result=theParentCacheFLD
	if(result==[:] || result==null){
		if(eric())log.debug mStr
		Map t0=(Map)parent.getChildPstate()
		Map t1=[
			coreVersion: (String)t0.sCv,
			hcoreVersion: (String)t0.sHv,
			powerSource: (String)t0.powerSource,
			region: (String)t0.region,
			instanceId: (String)t0.instanceId,
			settings: t0.stsetting,
			enabled: !!t0.enabled,
			disabled: !t0.enabled,
			logPExec: !!t0.logPExec,
			locationId: (String)t0.locationId,
			oldLocationId: hashId(location.id+'L'), //backwards compatibility
			incidents: (List)t0.incidents
		]
		result=[:]+t1
		theParentCacheFLD=result
	}
	if(result==[:] || result==null){
		log.error 'NO '+mStr
	}
	return [:]+result
}

private Map getRunTimeData(Map rtData=null, Map retSt=null, Boolean fetchWrappers=false, Boolean shorten=false){
	Long timestamp=now()
	Long started=timestamp
	List logs=[]
	LinkedHashMap piston
	Long lstarted=0L
	Long lended=0L
	Integer dbgLevel=0
	if(rtData!=null){
		timestamp=(Long)rtData.timestamp
		logs=rtData.logs!=null ? (List)rtData.logs:[]
		piston=rtData.piston!=null ? rtData.piston:null
		lstarted=rtData.lstarted!=null ? (Long)rtData.lstarted:0L
		lended=rtData.lended!=null ? (Long)rtData.lended:0L
		dbgLevel=rtData.debugLevel!=null ? (Integer)rtData.debugLevel:0
	}else{
		rtData=getTemporaryRunTimeData(timestamp)
	}
	if(rtData.temporary!=null){
		rtData.remove('temporary')
	}

	Map m1=[semaphore:0L, semaphoreName:(String)null, semaphoreDelay:0L, wAtSem:false]
	if(retSt!=null){
		m1.semaphore=(Long)retSt.semaphore
		m1.semaphoreName=(String)retSt.semaphoreName
		m1.semaphoreDelay=(Long)retSt.semaphoreDelay
		m1.wAtSem=(Long)retSt.semaphoreDelay>0L ? true:false
	}
	rtData=rtData+m1

	def mode=location.getCurrentMode()
	rtData.locationModeId=mode!=null ? hashId((Long)mode.getId()):null

	rtData.timestamp=timestamp
	rtData.lstarted=lstarted
	rtData.lended=lended
	rtData.logs=[]
	if(logs!=[] && (Integer)logs.size()>0){
		rtData.logs=(List)rtData.logs+logs
	} else rtData.logs=[[t: timestamp]]
	rtData.debugLevel=dbgLevel

	rtData.trace=[t:timestamp, points:[:]]
	rtData.stats=[nextSchedule:0L]
	rtData.newCache=[:]
	rtData.schedules=[]
	rtData.cancelations=[statements:[], conditions:[], all:false]
	rtData.updateDevices=false
	rtData.systemVars=[:]+getSystemVariables

	Map atomState=getCachedMaps()
	Map st=atomState.state
	rtData.state=st!=null && st!=[:] ? [:]+st:[old:'', new:'']
	rtData.state.old=(String)rtData.state.new

	rtData.pStart=now()
	Boolean doSubScribe=false
	if(piston==null){
		piston=recreatePiston(shorten)
		doSubScribe=!piston.cache
	}
	rtData.piston=piston
	getLocalVariables(rtData, piston.v, atomState)

	if(doSubScribe || fetchWrappers){
		subscribeAll(rtData, fetchWrappers)
		String pisName=(app.id).toString()
		if(thePistonCacheFLD==null)thePistonCacheFLD=[:]
		Map pData=thePistonCacheFLD[pisName]
		if(shorten && pisName!='' && pData!=null && pData.pis==null){
			if(eric())log.debug 'creating piston-code-cache'
			pData.pis=[:]+rtData.piston

			thePistonCacheFLD[pisName]=[:]+pData
			if(eric()){
				Map pL=[:]+thePistonCacheFLD
				Integer t0=(Integer)pL.size()
				Integer t1=(Integer)"${pL}".size()
				String mStr=" piston plist is ${t0} elements, and ${t1} bytes".toString()
				if(eric())log.debug "saving"+mStr
				if(t1>40000000){
					thePistonCacheFLD=[:]
					if(eric())log.warn "clearing"+mStr
				}
			}
		}
	}
	Long t0=now()
	rtData.pEnd=t0
	rtData.ended=t0
	rtData.generatedIn=t0-started
	return rtData
}

private void checkVersion(Map rtData){
	String ver=HEversion()
	String t0=(String)rtData.hcoreVersion
	if(ver!=t0){
		String tt0="child app's version($ver)".toString()
		String tt1="parent app's version($t0)".toString()
		String tt2=' is newer than the '
		String msg
		if(ver>t0) msg=tt0+tt2+tt1
		else msg=tt1+tt2+tt0
		warn "WARNING: Results may be unreliable because the "+msg+". Please consider updating both apps to the same version.", rtData
	}
	if(location.timeZone==null){
		error 'Your location is not setup correctly - timezone information is missing. Please select your location by placing the pin and radius on the map, then tap Save, and then tap Done. You may encounter error or incorrect timing until this is fixed.', rtData
	}
}

/*** EVENT HANDLING								***/

void deviceHandler(event){
	handleEvents(event)
}

void timeHandler(event){
	timeHelper(event, false)
}

void timeHelper(event, Boolean recovery){
	handleEvents([date:new Date((Long)event.t), device:location, name:'time', value:(Long)event.t, schedule:event, recovery:recovery], !recovery)
}

/*
void timeRecoveryHandler(event){
	if(event){
		timeHelper(event, true)
	}else{
		timeHelper([t:now()], true)
	}
}
*/

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
	taskMaxDelay: 2000L,
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
		String recStr=evntName=='time' && event.recovery ? '/recovery':''
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
		if(dis){
			msg.m='Kill switch is'+tstr
		}
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
		if((Boolean)retSt.wAtSem){
			warn 'Piston waited at a semaphore for '+(Long)retSt.semaphoreDelay+'ms', tempRtData
		}
	}
	tempRtData.lended=now()

//measure how Long first state access takes
	Long stAccess
	if(tempRtData.stateAccess==null){
		Long stStart=now()
		Long b=(Long)state.nextSchedule
		def a=state.schedules
		Long stEnd=now()
		stAccess=stEnd-stStart
	}else stAccess=(Long)tempRtData.stateAccess

	tempRtData.cachePersist=null
	Map rtData=getRunTimeData(tempRtData, retSt, false, true)
	checkVersion(rtData)

	Long theend=now()
	Long t0=theend-startTime
	Long t1=(Long)rtData.lended-(Long)rtData.lstarted
	Long t2=(Long)rtData.generatedIn
	Long t3=(Long)rtData.pEnd-(Long)rtData.pStart
	Long missing=t0-t1-t2
	Long t4=(Long)rtData.lended-startTime
	Long t5=theend-(Long)rtData.lended
	if((Integer)rtData.logging>1){
		if((Integer)rtData.logging>2)debug "RunTime initialize > ${t0} LockT > ${t1}ms > rtDataT > ${t2}ms > pistonT > ${t3}ms (first state access ${missing} $t4 $t5)".toString(), rtData
		String adMsg=''
		if(eric())adMsg=" (Init: $t0, Lock: $t1, pistonT $t3 first state access $missing ($t4 $t5) $stAccess".toString()
		if((Integer)rtData.logging>1)trace "Runtime (${(Integer)"$rtData".size()} bytes) successfully initialized in ${t2}ms (${HEversion()})".toString()+adMsg, rtData
	}
	rtData.curStat=[i:t0, l:t1, r:t2, p:t3, s:stAccess]

	rtData.temp=[randoms:[:]] // equivalent of resetRandomValues()
	rtData.tPause=0L
	rtData.stats.timing=[t:startTime, d:eventDelay>0L ? eventDelay:0L, l:Math.round(1.0D*now()-startTime)]

	startTime=now()
	Map msg2=timer "Execution stage complete.", rtData, -1
	Boolean success=true
	Boolean firstTime=true
	if(evntName!='time' && evntName!='wc_async_reply'){
		if((Integer)rtData.logging>0)info "Execution stage started", rtData, 1
		success=executeEvent(rtData, event)
		firstTime=false
	}

	Boolean syncTime=true
	String myId=(String)rtData.id
	while (success && (Long)getPistonLimits.executionTime+(Long)rtData.timestamp-now()>(Long)getPistonLimits.schedule){
		List schedules
		Map tt0=getCachedMaps()
		if(tt0!=[:])schedules=[]+tt0.schedules
		else{ schedules=myPep ? (List)atomicState.schedules:(List)state.schedules }
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
		if(event.schedule==null)break

		schedules.remove(event.schedule)

		if(tt0!=[:])theCacheFLD[myId].schedules=schedules
		if(myPep)atomicState.schedules=schedules
		else state.schedules=schedules

		if(evntName=='wc_async_reply'){
			Integer responseCode=(Integer)event.responseCode
			Boolean statOk=responseCode>=200 && responseCode<=299
			if(evntVal=='httpRequest'){
				if(event.schedule.stack!=null){
					event.schedule.stack.response=event.responseData
					event.schedule.stack.json=event.jsonData
				}
				setSystemVariableValue(rtData, '$httpContentType', (String)event.contentType)
			}
			if(evntVal=='httpRequest' || evntVal=='storeMedia'){
				if(event.setRtData){
					for(item in event.setRtData){
						rtData[(String)item.key]=item.value
					}
				}
				setSystemVariableValue(rtData, '$httpStatusCode', responseCode)
				setSystemVariableValue(rtData, '$httpStatusOk', statOk)
			}
			evntName='time'
			event.name=evntName
			event.value=t
			evntVal=t.toString()
		}else{
			if((String)event.schedule.d=='httpRequest'){
				setSystemVariableValue(rtData, '$httpContentType', '')
				setSystemVariableValue(rtData, '$httpStatusCode', 408)
				setSystemVariableValue(rtData, '$httpStatusOk', false)
				if(event.schedule.stack!=null){
					event.schedule.stack.response=null
				}
				error "Timeout Error httpRequest", rtData
				syncTime=true
			}
			if((String)event.schedule.d=='sendEmail'){
				error "Timeout Error sendEmail", rtData
				syncTime=true
			}
			if((String)event.schedule.d=='storeMedia'){
				setSystemVariableValue(rtData, '$httpStatusCode', 408)
				setSystemVariableValue(rtData, '$httpStatusOk', false)
				error "Timeout Error storeMedia", rtData
				syncTime=true
			}
		}
		//if we have any other pending -3 events (device schedules), we cancel them all
		//if(event.schedule.i>0)schedules.removeAll{ (it.s==event.schedule.s) && (it.i==-3)}
		if(syncTime && strictSync){
			Long delay=Math.round((Long)event.schedule.t-1.0D*now())
			if(delay>0L && delay<(Long)getPistonLimits.scheduleVariance){
				if((Integer)rtData.logging>1)trace "Synchronizing scheduled event, waiting for ${delay}ms".toString(), rtData
				pauseExecution(delay)
			}
		}
		if(firstTime){
			msg2=timer "Execution stage complete.", rtData, -1
			if((Integer)rtData.logging>0)info "Execution stage started", rtData, 1
		}
		success=executeEvent(rtData, event)
		syncTime=true
		firstTime=false
	}

	rtData.stats.timing.e=Math.round(1.0D*now()-startTime)
	if((Integer)rtData.logging>0)info msg2, rtData
	if(!success)msg.m='Event processing failed'
	if(eric())msg.m=(String)msg.m+' Total Pauses ms: '+((Long)rtData.tPause).toString()
	finalizeEvent(rtData, msg, success)

	if((Boolean)rtData.logPExec && rtData.currentEvent!=null){
		String desc='webCore piston \''+(String)app.label+'\' was executed'
		sendLocationEvent(name:'webCoRE', value:'pistonExecuted', isStateChange:true, displayed:false, linkText:desc, descriptionText:desc, data:[
			id:appId,
			name:(String)app.label,
			event:[date:new Date((Long)rtData.currentEvent.date), delay:(Long)rtData.currentEvent.delay, duration:now()-(Long)rtData.currentEvent.date, device:"${rtData.event.device}".toString(), name:(String)rtData.currentEvent.name, value:rtData.currentEvent.value, physical:(Boolean)rtData.currentEvent.physical, index:rtData.currentEvent.index],
			state:[old:(String)rtData.state.old, new:(String)rtData.state.new]
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
		if(qsize>8){ log.error "large queue size ${qsize}".toString() }
		theEvent.date=new Date((Long)theEvent.t)
		handleEvents(theEvent, false, true, (LinkedHashMap)rtData.piston)
	}

	String msgt='Exiting'
	String semName=(String)rtData.semaphoreName
	if(doSerialization && semName!=(String)null && (Long)theSemaphoresFLD[semName]<=(Long)rtData.semaphore){
		msgt='Released Lock and exiting'
		theSemaphoresFLD[semName]=0L
	}
	if((Integer)rtData.logging>2){ log.debug msgt }
	data=rtData.collect{ it.key }
	for(item in data)rtData.remove((String)item)
}

private Boolean executeEvent(Map rtData, event){
	myDetail rtData, 'executeEvent', 1
	try{
		rtData.event=event
		rtData.previousEvent=state.lastEvent
		if(rtData.previousEvent==null)rtData.previousEvent=[:]
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
		rtData.args=[:]
		if(event!=null){
			rtData.args= evntName=='time' && event.schedule!=null && event.schedule.args!=null && event.schedule.args instanceof Map ? event.schedule.args:(event.jsonData!=null ? event.jsonData:[:])
			if(evntName=='time' && event.schedule!=null){
				srcEvent=event.schedule.evt!=null ? event.schedule.evt:null
				Map tMap=event.schedule.stack
				if(tMap!=null){
					rtData.systemVars['$index'].v=tMap.index
					rtData.systemVars['$device'].v=tMap.device
					rtData.systemVars['$devices'].v=tMap.devices
					rtData.json=tMap.json!=null ? tMap.json:[:]
					rtData.response=tMap.response!=null ? tMap.response:[:]
					index=srcEvent?.index!=null ? srcEvent.index:0
// more to restore here?
				}
			}
		}
		setSystemVariableValue(rtData, '$args', rtData.args)

		def theDevice=srcEvent!=null ? srcEvent.device:null
		def theDevice1=theDevice==null && event.device ? event.device.id:null
		String theDevice2=theDevice1==null ? (String)rtData.locationId:null
		def theFinalDevice=theDevice!=null ? theDevice : (theDevice1!=null ? (!isDeviceLocation(event.device) ? hashId(theDevice1) : (String)rtData.locationId) : (theDevice2!=(String)null ? theDevice2 : null))
		rtData.currentEvent=[
			date:(Long)event.date.getTime(),
			delay:rtData.stats?.timing?.d ? (Long)rtData.stats.timing.d : 0L,
			device:theFinalDevice,
			name:srcEvent!=null ? (String)srcEvent.name:evntName,
			value:srcEvent!=null ? srcEvent.value:event.value,
			descriptionText:srcEvent!=null ? (String)srcEvent.descriptionText : (String)event.descriptionText,
			unit:srcEvent!=null ? srcEvent.unit:event.unit,
			physical:srcEvent!=null ? (Boolean)srcEvent.physical : !!event.physical,
			index:index
		]
		state.lastEvent=rtData.currentEvent

		rtData.conditionStateChanged=false
		rtData.pistonStateChanged=false
		rtData.fastForwardTo=0
		rtData.statementLevel=0
		rtData.break=false
		rtData.resumed=false
		rtData.terminated=false
		if(evntName=='time'){
			rtData.fastForwardTo=(Integer)event.schedule.i
		}
		rtData.systemVars['$previousEventDate'].v=rtData.previousEvent?.date ?: now()
		rtData.systemVars['$previousEventDelay'].v=rtData.previousEvent?.delay ?: 0L
		rtData.systemVars['$previousEventDevice'].v=[rtData.previousEvent?.device]
		rtData.systemVars['$previousEventDeviceIndex'].v=rtData.previousEvent?.index ?: 0
		rtData.systemVars['$previousEventAttribute'].v=(String)rtData.previousEvent?.name ?: ''
		rtData.systemVars['$previousEventDescription'].v=(String)rtData.previousEvent?.descriptionText ?: ''
		rtData.systemVars['$previousEventValue'].v=rtData.previousEvent?.value ?: ''
		rtData.systemVars['$previousEventUnit'].v=rtData.previousEvent?.unit ?: ''
		rtData.systemVars['$previousEventDevicePhysical'].v=!!rtData.previousEvent?.physical

		rtData.systemVars['$currentEventDate'].v=(Long)rtData.currentEvent.date
		rtData.systemVars['$currentEventDelay'].v=(Long)rtData.currentEvent.delay
		rtData.systemVars['$currentEventDevice'].v=[rtData.currentEvent?.device]
		rtData.systemVars['$currentEventDeviceIndex'].v=rtData.currentEvent.index!='' && rtData.currentEvent.index!=null? (Integer)rtData.currentEvent.index:0
		rtData.systemVars['$currentEventAttribute'].v=(String)rtData.currentEvent.name
		rtData.systemVars['$currentEventDescription'].v=(String)rtData.currentEvent.descriptionText
		rtData.systemVars['$currentEventValue'].v=rtData.currentEvent.value
		rtData.systemVars['$currentEventUnit'].v=rtData.currentEvent.unit
		rtData.systemVars['$currentEventDevicePhysical'].v=(Boolean)rtData.currentEvent.physical

		rtData.stack=[c: 0, s: 0, cs:[], ss:[]]
		Boolean ended=false
		try{
			Boolean allowed=!rtData.piston.r || rtData.piston.r.length==0 || evaluateConditions(rtData, (Map)rtData.piston, 'r', true)
			rtData.restricted=!rtData.piston.o?.aps && !allowed
			if(allowed || (Integer)rtData.fastForwardTo!=0){
				if((Integer)rtData.fastForwardTo==-3){
					//device related time schedules
					if(!(Boolean)rtData.restricted){
						def data=event.schedule.d
						if(data!=null && (String)data.d && (String)data.c){
							//we have a device schedule, execute it
							def device=getDevice(rtData, (String)data.d)
							if(device!=null){
								//executing scheduled physical command
								//used by fades, flashes, etc.
								executePhysicalCommand(rtData, device, (String)data.c, data.p, 0, (String)null, true)
							}
						}
					}
				}else{
					if(executeStatements(rtData, (List)rtData.piston.s)){
						ended=true
						tracePoint(rtData, 'end', 0L, 0)
					}
					processSchedules rtData
				}
			}else{
				warn 'Piston execution aborted due to restrictions in effect', rtData
				//we need to run through all to update stuff
				rtData.fastForwardTo=-9
				Boolean a=executeStatements(rtData, (List)rtData.piston.s)
				ended=true
				tracePoint(rtData, 'end', 0L, 0)
				processSchedules rtData
			}
			if(!ended)tracePoint(rtData, 'break', 0L, 0)
		} catch (all){
			error 'An error occurred while executing the event: ', rtData, -2, all
		}
		myDetail rtData, 'executeEvent Result: TRUE', -1
		return true
	} catch(all){
		error 'An error occurred within executeEvent: ', rtData, -2, all
	}
	processSchedules rtData
	return false
}

private void finalizeEvent(Map rtData, Map initialMsg, Boolean success=true){
	Long startTime=now()
	Boolean myPep=(Boolean)rtData.pep

	processSchedules(rtData, true)

	if(success){
		if((Integer)rtData.logging>0)info initialMsg, rtData
	}else{
		error initialMsg, rtData
	}

	updateLogs(rtData, (Long)rtData.timestamp)

	Map t0=getCachedMaps()
	String myId=(String)rtData.id
	if(t0!=[:])theCacheFLD[myId].state=[:]+rtData.state
	state.state=rtData.state

	rtData.trace.d=Math.round(1.0D*now()-(Long)rtData.trace.t)
	if(t0!=[:])theCacheFLD[myId].trace=[:]+rtData.trace
	state.trace=rtData.trace

	//flush the new cache value
	for(item in (Map)rtData.newCache)rtData.cache[(String)item.key]=item.value

	//overwrite state, might have changed meanwhile
	if(t0!=[:]){
		theCacheFLD[myId].cache=[:]+rtData.cache
		theCacheFLD[myId].store=[:]+rtData.store
	}
	if(myPep){
		atomicState.cache=rtData.cache
		atomicState.store=rtData.store
	}else{
		state.cache=rtData.cache
		state.store=rtData.store
	}

//remove large stuff
	List data=[ 'lstarted', 'lended', 'pStart', 'pEnd', 'generatedIn', 'ended', 'wAtSem', 'semaphoreDelay', 'vars', 'stateAccess', 'author', 'bin', 'build', 'newCache', 'mediaData', 'weather', 'logs', 'trace', 'systemVars', 'localVars', 'currentAction', 'previousEvent', 'json', 'response', 'cache', 'store', 'settings', 'locationModeId', 'locationId', 'coreVersion', 'hcoreVersion', 'cancelations', 'conditionStateChanged', 'pistonStateChanged', 'fastForwardTo', 'resumed', 'terminated', 'instanceId', 'wakingUp', 'statementLevel', 'args', 'nfl', 'temp' ]
	for (foo in data){
		rtData.remove((String)foo)
	}
	if(!(rtData?.event instanceof com.hubitat.hub.domain.Event)){
		if(rtData?.event?.responseData)rtData.event.responseData=[:]
		if(rtData?.event?.jsonData)rtData.event.jsonData=[:]
		if(rtData?.event?.setRtData)rtData.event.setRtData=[:]
		if(rtData?.event?.schedule?.stack)rtData.event.schedule.stack=[:]
	}

	if(rtData.gvCache!=null || rtData.gvStoreCache!=null){
		unschedule(finishUIupdate)
		def tpiston=[:]+rtData.piston
		rtData.piston=[:]
		rtData.piston.z=(String)tpiston.z

		if(rtData.gvCache!=null){
			Map vars=globalVarsFLD
			for(var in rtData.gvCache){
				String varName=(String)var.key
				if(varName && varName.startsWith('@') && vars[varName] && var.value.v!=vars[varName].v){
					globalVarsFLD[varName].v=var.value.v
				}
			}
		}

		parent.updateRunTimeData(rtData)

		rtData.piston=tpiston

		rtData.remove('gvCache')
		rtData.remove('gvStoreCache')
	}else{
		// schedule to update UI for state
		def st=[:]+state.state
		st.remove('old')
		Map myRt=[
			id:(String)rtData.id,
			active:(Boolean)rtData.active,
			category:rtData.category,
			stats:[
				nextSchedule:(Long)rtData.stats.nextSchedule
			],
			piston:[
				z:(String)rtData.piston.z
			],
			state:st
		]
		runIn(2, finishUIupdate, [data: myRt])
	}

	//update graph data
	rtData.stats.timing.u=Math.round(1.0D*now()-startTime)
	Map stats
	if(myPep)stats=atomicState.stats
	else stats=state.stats
	stats=stats ?: [:]

	stats.timing=(List)stats.timing ?: []
	Boolean a=((List)stats.timing).push([:]+(Map)rtData.stats.timing)
	Integer t1=settings.maxStats!=null ? settings.maxStats: (Integer)getPistonLimits.maxStats
	if(t1<=0)t1=(Integer)getPistonLimits.maxStats
	Integer t2=(Integer)((List)stats.timing).size()
	if(t2>t1){
		stats.timing=stats.timing[t2-t1..t2-1]
	}
	if(myPep)atomicState.stats=stats
	else state.stats=stats
	rtData.stats.timing=null

	if(t0!=[:]){
		theCacheFLD[myId].mem=mem()
		theCacheFLD[myId].runStats=[:]+rtData.curStat
		List hisList=theCacheFLD[myId].runTimeHis
		Long totTime=Math.round(now()*1.0D-(Long)rtData.timestamp)
		Boolean b=hisList.push(totTime)
		t1=20
		t2=(Integer)hisList.size()
		if(t2>t1){
			hisList=hisList[t2-t1..t2-1]
		}
		theCacheFLD[myId].runTimeHis=hisList
	}

	if((Boolean)rtData.updateDevices){
		updateDeviceList(rtData, rtData.devices*.value.id) // this will clear the cache
	}
	rtData.remove('devices')
}

public void finishUIupdate(myRt){
	parent.updateRunTimeData(myRt)
}

private void processSchedules(Map rtData, Boolean scheduleJob=false){
	Boolean myPep=(Boolean)rtData.pep

	List schedules
	Map t0=getCachedMaps()
	if(t0!=[:])schedules=[]+(List)t0.schedules
	else{ schedules=myPep ? (List)atomicState.schedules:(List)state.schedules }

	//if automatic piston states, we set it based on the autoNew - if any
	if(rtData.piston.o?.mps==null || !rtData.piston.o.mps){
		rtData.state.new=(String)rtData.state.autoNew ?: 'true'
	}
	rtData.state.old=(String)rtData.state.new

	if((Boolean)rtData.cancelations.all){
		schedules.removeAll{ (Integer)it.i>0 }
	}
	//cancel statements
	schedules.removeAll{ Map schedule -> !!((List)rtData.cancelations.statements).find{ Map cancelation -> (Integer)cancelation.id==(Integer)schedule.s && (!cancelation.data || ((String)cancelation.data==(String)schedule.d))}}

	//cancel on conditions
	for(Integer cid in (List)rtData.cancelations.conditions){
		schedules.removeAll{ cid in (List)it.cs }
	}

	//cancel on piston state change
	if((Boolean)rtData.pistonStateChanged){
		schedules.removeAll{ (Integer)it.ps!=0 }
	}

	rtData.cancelations=[statements:[], conditions:[], all:false]
	schedules=(schedules+(List)rtData.schedules)//.sort{ (Long)it.t }

	String myId=(String)rtData.id
	if(t0!=[:])theCacheFLD[myId].schedules=[]+schedules
	if(myPep)atomicState.schedules=schedules
	else state.schedules=[]+schedules

	if(scheduleJob){
		Long nextT=0L
		if((Integer)schedules.size()>0){
			Map tnext=schedules.sort{ (Long)it.t }[0]
			nextT=(Long)tnext.t
			Long t=Math.round((nextT-now())/1000.0D)
			t=(t<1L ? 1L:t)
			if((Integer)rtData.logging>0) info 'Setting up scheduled job for '+formatLocalTime(nextT)+' (in '+t.toString()+'s)' + ((Integer)schedules.size()>1 ? ', with ' + ((Integer)schedules.size()-1).toString() + ' more job' + ((Integer)schedules.size()>2 ? 's' : '') + ' pending' : ''), rtData
			Integer t1=Math.round(t)

			runIn(t1, timeHandler, [data: tnext])
		}
		if(nextT==0L && (Long)rtData.nextSchedule!=0L){
			unschedule(timeHandler)
			rtData.nextSchedule=0L
		}

		rtData.stats.nextSchedule=nextT
		if(t0!=[:])theCacheFLD[myId].nextSchedule=nextT
		state.nextSchedule=nextT
	}
	rtData.schedules=[]
}

private void updateLogs(Map rtData, Long lastExecute=null){
	if(!rtData || !rtData.logs)return

	String myId=(String)rtData.id
	Map cacheMap=getCachedMaps()
	if(cacheMap!=[:] && lastExecute!=null){
		theCacheFLD[myId].lastExecuted=lastExecute
		state.lastExecuted=(Long)lastExecute
		theCacheFLD[myId].temp=rtData.temp
		theCacheFLD[myId].cachePersist=rtData.cachePersist
	}

	//we only save the logs if we got some
	if((Integer)((List)rtData.logs).size()<2)return

	Boolean myPep=(Boolean)rtData.pep
	Integer t1=settings.maxLogs!=null ? settings.maxLogs:(Integer)getPistonLimits.maxLogs
	if(t1<=0)t1=(Integer)getPistonLimits.maxLogs

	List t0

	if(cacheMap!=[:])t0=[]+(List)cacheMap.logs
	else{ t0=myPep ? atomicState.logs:state.logs }
	List logs=[]+(List)rtData.logs+t0
	while (t1>=0){
		Integer t2=(Integer)logs.size()
		if(t1==0 || t2==0){ logs=[]; break }
		if(t1<(t2-1)){
			logs=logs[0..t1]
			state.logs=logs
		}
		if(t1>5 && (Integer)state.toString().size()>75000){
			t1 -= Math.min(50, Math.round(t1/2.0D))
		}else{ break }
	}
	if(cacheMap!=[:])theCacheFLD[myId].logs=logs
	if(myPep)atomicState.logs=logs
	else state.logs=logs
	rtData.logs=[]
}

private Boolean executeStatements(Map rtData, List statements, Boolean async=false){
	rtData.statementLevel=(Integer)rtData.statementLevel+1
	for(Map statement in statements){
		//only execute statements that are enabled
		Boolean disab=statement.di!=null && (Boolean)statement.di
		if(!disab && !executeStatement(rtData, statement, async)){
			//stop processing
			rtData.statementLevel=(Integer)rtData.statementLevel-1
			return false
		}
	}
	//continue processing
	rtData.statementLevel=(Integer)rtData.statementLevel-1
	return true
}

private Boolean executeStatement(Map rtData, Map statement, Boolean async=false){
	//if rtData.fastForwardTo is a positive, non-zero number, we need to fast forward through all
	//branches until we find the task with an id equal to that number, then we play nicely after that
	if(statement==null)return false
	Integer statementNum=statement.$!=null ? (Integer)statement.$:0
	if((Integer)rtData.fastForwardTo==0){
		String sMsg="Skipping execution for statement #${statementNum} because "
		switch ((String)statement.tep){ // Task Execution Policy
		case 'c':
			if(!(Boolean)rtData.conditionStateChanged){
				if((Integer)rtData.logging>2)debug sMsg+'condition state did not change', rtData
				return true
			}
			break
		case 'p':
			if(!(Boolean)rtData.pistonStateChanged){
				if((Integer)rtData.logging>2)debug sMsg+'piston state did not change', rtData
				return true
			}
			break
		case 'b':
			if( !(Boolean)rtData.conditionStateChanged && !(Boolean)rtData.pistonStateChanged){
				if((Integer)rtData.logging>2)debug sMsg+'neither condition state nor piston state changed', rtData
				return true
			}
			break
		}
	}
	myDetail rtData, "executeStatement ${(String)statement.t}", 1
	Boolean a=((List)rtData.stack.ss).push((Integer)rtData.stack.s)
	rtData.stack.s=statementNum
	Long t=now()
	Boolean value=true
	Integer c=(Integer)rtData.stack.c
	Boolean stacked=true /* cancelable on condition change */
	if(stacked)a=((List)rtData.stack.cs).push(c)
	Boolean parentConditionStateChanged=(Boolean)rtData.conditionStateChanged
	//def parentAsync=async
	Double parentIndex=rtData.systemVars['$index'].v
	def parentDevice=rtData.systemVars['$device'].v
	Boolean selfAsync=((String)statement.a=='1')|| ((String)statement.t=='every')|| ((String)statement.t=='on') // execution method
	async=async || selfAsync
	Boolean myPep=(Boolean)rtData.pep
	Boolean perform=false
	Boolean repeat=true
	Double index=null
	Boolean allowed=!statement.r || statement.r.length==0 || evaluateConditions(rtData, statement, 'r', async)
	if(allowed || (Integer)rtData.fastForwardTo!=0){
		while (repeat){
			switch ((String)statement.t){
			case 'every':
				//we override current condition so that child statements can cancel on it
				Boolean ownEvent= rtData.event!=null && (String)rtData.event.name=='time' && rtData.event.schedule!=null && (Integer)rtData.event.schedule.s==statementNum && (Integer)rtData.event.schedule.i<0

				List schedules
				Map t0=getCachedMaps()
				if(t0!=[:])schedules=[]+t0.schedules
				else{ schedules=myPep ? (List)atomicState.schedules:(List)state.schedules }
				if(ownEvent || !schedules.find{ (Integer)it.s==statementNum }){
					//if the time has come for our timer, schedule the next timer
					//if no next time is found quick enough, a new schedule with i=-2 will be setup so that a new attempt can be made at a later time
					if(ownEvent)rtData.fastForwardTo=0
					scheduleTimer(rtData, statement, ownEvent ? (Long)rtData.event.schedule.t:0L)
				}
				rtData.stack.c=statementNum
				if(ownEvent)rtData.fastForwardTo=0
				if((Integer)rtData.fastForwardTo!=0 || (ownEvent && allowed && !(Boolean)rtData.restricted)){
					//we don't want to run this if there are piston restrictions in effect
					//we only execute the every if i=-1 (for rapid timers with large restrictions i.e. every second, but only on Mondays)we need to make sure we don't block execution while trying
					//to find the next execution scheduled time, so we give up after too many attempts and schedule a rerun with i=-2 to give us the chance to try again at that later time
					if((Integer)rtData.fastForwardTo!=0 || (Integer)rtData.event.schedule.i==-1)a=executeStatements(rtData, (List)statement.s, true)
					//we always exit a timer, this only runs on its own schedule, nothing else is executed
					if(ownEvent)rtData.terminated=true
					value=false
					break
				}
				value=true
				break
			case 'repeat':
				//we override current condition so that child statements can cancel on it
				rtData.stack.c=statementNum
				if(!executeStatements(rtData, (List)statement.s, async)){
					//stop processing
					value=false
					if((Integer)rtData.fastForwardTo==0)break
				}
				value=true
				perform=(evaluateConditions(rtData, statement, 'c', async)==false)
				break
			case 'on':
				perform=false
				if((Integer)rtData.fastForwardTo==0){
					//look to see if any of the event matches
					String deviceId= rtData.event.device!=null ? hashId(rtData.event.device.id):(String)null
					for (event in statement.c){
						def operand=event.lo
						if(operand!=null && (String)operand.t){
							switch ((String)operand.t){
							case 'p':
								if(deviceId!=(String)null && (String)rtData.event.name==(String)operand.a && operand.d!=[] && deviceId in expandDeviceList(rtData, (List)operand.d, true)) perform=true
								break
							case 'v':
								if((String)rtData.event.name==(String)operand.v) perform=true
								break
							case 'x':
								String operX=(String)operand.x
								if(rtData.event.value==operX && (String)rtData.event.name==(String)rtData.instanceId+'.'+operX) perform=true
								break
							}
						}
						if(perform)break
					}
				}
				value= (Integer)rtData.fastForwardTo!=0 || perform ? executeStatements(rtData, (List)statement.s, async):true
				break
			case 'if':
			case 'while':
				//check conditions for if and while
				perform=evaluateConditions(rtData, statement, 'c', async)
				//we override current condition so that child statements can cancel on it
				rtData.stack.c=statementNum
				if((Integer)rtData.fastForwardTo==0 && !rtData.piston.o?.mps && (String)statement.t=='if' && (Integer)rtData.statementLevel==1 && perform){
					//automatic piston state
					rtData.state.autoNew='true'
				}
				if(perform || (Integer)rtData.fastForwardTo!=0){
					if((String)statement.t in ['if', 'while']){
						if(!executeStatements(rtData, (List)statement.s, async)){
							//stop processing
							value=false
							if((Integer)rtData.fastForwardTo==0)break
						}
						value=true
						if((Integer)rtData.fastForwardTo==0)break
					}
				}
				if(!perform || (Integer)rtData.fastForwardTo!=0){
					if((String)statement.t=='if'){
						//look for else-ifs
						for (elseIf in statement.ei){
							perform=evaluateConditions(rtData, elseIf, 'c', async)
							if(perform || (Integer)rtData.fastForwardTo!=0){
								if(!executeStatements(rtData, (List)elseIf.s, async)){
									//stop processing
									value=false
									if((Integer)rtData.fastForwardTo==0)break
								}
								value=true
								if((Integer)rtData.fastForwardTo==0)break
							}
						}
						if((Integer)rtData.fastForwardTo==0 && !rtData.piston.o?.mps && (Integer)rtData.statementLevel==1){
							//automatic piston state
								rtData.state.autoNew='false'
						}
						if((!perform || (Integer)rtData.fastForwardTo!=0) && !executeStatements(rtData, (List)statement.e, async)){
							//stop processing
							value=false
							if((Integer)rtData.fastForwardTo==0)break
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
					def t0=evaluateOperand(rtData, null, (Map)statement.lo).v
					devices=t0 ?: []
					endValue=(Integer)devices.size()-1.0D
				}else{
					startValue=(Double)evaluateScalarOperand(rtData, statement, (Map)statement.lo, null, 'decimal').v
					endValue=(Double)evaluateScalarOperand(rtData, statement, (Map)statement.lo2, null, 'decimal').v
					Double t0=(Double)evaluateScalarOperand(rtData, statement, (Map)statement.lo3, null, 'decimal').v
					stepValue=t0 ?: 1.0D
				}
				String counterVariable=(String)getVariable(rtData, (String)statement.x).t!='error' ? (String)statement.x:(String)null
				if( (startValue<=endValue && stepValue>0.0D) || (startValue>=endValue && stepValue<0.0D) || (Integer)rtData.fastForwardTo!=0){
					//initialize the for loop
					if((Integer)rtData.fastForwardTo!=0)index=(Double)cast(rtData, rtData.cache["f:${statementNum}".toString()], 'decimal')
					if(index==null){
						index=(Double)cast(rtData, startValue, 'decimal')
						//index=startValue
						rtData.cache["f:${statementNum}".toString()]=index
					}
					rtData.systemVars['$index'].v=index
					if((String)statement.t=='each' && (Integer)rtData.fastForwardTo==0)setSystemVariableValue(rtData, '$device', index<(Integer)devices.size() ? [devices[(Integer)index]]:[])
					if(counterVariable!=(String)null && (Integer)rtData.fastForwardTo==0)def m=setVariable(rtData, counterVariable, (String)statement.t=='each' ? (index<(Integer)devices.size() ? [devices[(Integer)index]]:[]):index)
					//do the loop
					perform=executeStatements(rtData, (List)statement.s, async)
					if(!perform){
						//stop processing
						value=false
						if((Boolean)rtData.break){
							//we reached a break, so we really want to continue execution outside of the for
							value=true
							rtData.break=false
							//perform=false
						}
						break
					}
					//don't do the rest if we're fast forwarding
					if((Integer)rtData.fastForwardTo!=0)break
					index=index+stepValue
					rtData.systemVars['$index'].v=index
					if((String)statement.t=='each' && (Integer)rtData.fastForwardTo==0)setSystemVariableValue(rtData, '$device', index<(Integer)devices.size() ? [devices[(Integer)index]]:[])
					if(counterVariable!=(String)null && (Integer)rtData.fastForwardTo==0)def n=setVariable(rtData, counterVariable, (String)statement.t=='each' ? (index<(Integer)devices.size()? [devices[(Integer)index]]:[]):index)
					rtData.cache["f:${statementNum}".toString()]=index
					if((stepValue>0.0D && index>endValue) || (stepValue<0.0D && index<endValue)){
						perform=false
						break
					}
				}
				break
			case 'switch':
				Map lo=[operand: (Map)statement.lo, values: evaluateOperand(rtData, statement, (Map)statement.lo)]
				//go through all cases
				Boolean found=false
				Boolean implicitBreaks=((String)statement.ctp=='i')
				Boolean fallThrough=!implicitBreaks
				perform=false
				if((Integer)rtData.logging>2)debug "Evaluating switch with values $lo.values", rtData
				for (Map _case in statement.cs){
					Map ro=[operand: (Map)_case.ro, values: evaluateOperand(rtData, _case, (Map)_case.ro)]
					Map ro2=((String)_case.t=='r')? [operand: (Map)_case.ro2, values: evaluateOperand(rtData, _case, (Map)_case.ro2, null, false, true)]:null
					perform=perform || evaluateComparison(rtData, ((String)_case.t=='r' ? 'is_inside_of_range':'is'), lo, ro, ro2)
					found=found || perform
					if(perform || (found && fallThrough)|| (Integer)rtData.fastForwardTo!=0){
						Integer fastForwardTo=(Integer)rtData.fastForwardTo
						if(!executeStatements(rtData, (List)_case.s, async)){
							//stop processing
							value=false
							if((Boolean)rtData.break){
								//we reached a break, so we really want to continue execution outside of the switch
								value=true
								found=true
								fallThrough=false
								rtData.break=false
							}
							if((Integer)rtData.fastForwardTo==0){
								break
							}
						}
						//if we determine that the fast forwarding ended during this execution, we assume found is true
						found=found || (fastForwardTo!=(Integer)rtData.fastForwardTo)
						value=true
						//if implicit breaks
						if(implicitBreaks && (Integer)rtData.fastForwardTo==0){
							fallThrough=false
							break
						}
					}
				}
				if(statement.e && statement.e.length && (value || (Integer)rtData.fastForwardTo!=0) && (!found || fallThrough || (Integer)rtData.fastForwardTo!=0)){
					//no case found, let's do the default
					if(!executeStatements(rtData, (List)statement.e, async)){
						//stop processing
						value=false
						if((Boolean)rtData.break){
							//we reached a break, so we really want to continue execution outside of the switch
							value=true
							rtData.break=false
						}
						if((Integer)rtData.fastForwardTo==0)break
					}
				}
				break
			case 'action':
				value=executeAction(rtData, statement, async)
				break
			case 'do':
				value=executeStatements(rtData, (List)statement.s, async)
				break
			case 'break':
				if((Integer)rtData.fastForwardTo==0){
					rtData.break=true
				}
				value=false
				break
			case 'exit':
				if((Integer)rtData.fastForwardTo==0){
					vcmd_setState(rtData, null, [(String)cast(rtData, evaluateOperand(rtData, null, (Map)statement.lo).v, 'string')])
					rtData.terminated=true
				}
				value=false
				break
			}
			//break the loop
			if((Integer)rtData.fastForwardTo!=0 || (String)statement.t=='if')perform=false

			//is this statement a loop
			Boolean loop=((String)statement.t in ['while', 'repeat', 'for', 'each'])
			if(loop && !value && (Boolean)rtData.break){
				//someone requested a break from the loop, we're doing it
				rtData.break=false
				//but we're allowing the rest to continue
				value=true
				perform=false
			}
			//do we repeat the loop?
			repeat=perform && value && loop && (Integer)rtData.fastForwardTo==0

			Long overBy=checkForSlowdown(rtData)
			if(overBy>0L){
				Long delay=(Long)getPistonLimits.taskShortDelay
				if(overBy>(Long)getPistonLimits.useBigDelay){
					delay=(Long)getPistonLimits.taskLongDelay
				}
				String mstr="executeStatement: Execution time exceeded by ${overBy}ms, "
				if(repeat && overBy>(Long)getPistonLimits.executionTime){
					error mstr+'Terminating', rtData
					rtData.terminated=true
					repeat=false
				}else{
					Long b=doPause(mstr+'Waiting for '+delay+'ms', delay, rtData)
				}
			}
		}
	}
	if((Integer)rtData.fastForwardTo==0){
		def schedule
		if((String)statement.t=='every'){
			def t0=((List)rtData.schedules).find{ (Integer)it.s==statementNum}
			if(!t0){
				List schedules
				Map t1=getCachedMaps()
				if(t1!=[:])schedules=[]+(List)t1.schedules
				else{ schedules=myPep ? (List)atomicState.schedules:(List)state.schedules }
				schedule=schedules.find{ (Integer)it.s==statementNum }
			} else schedule=t0
		}
		if(schedule){
			//timers need to show the remaining time
			tracePoint(rtData, "s:${statementNum}".toString(), Math.round(1.0D*now()-t), Math.round(1.0D*now()-(Long)schedule.t))
		}else{
			tracePoint(rtData, "s:${statementNum}".toString(), Math.round(1.0D*now()-t), value)
		}
	}
	//if(statement.a=='1'){
		//when an async action requests the thread termination, we continue to execute the parent
		//when an async action terminates as a result of a time event, we exit completely
//		value=(rtData.event.name!='time')
	//}
	if(selfAsync){
		//if running in async mode, we return true (to continue execution)
		value=!(Boolean)rtData.resumed
		rtData.resumed=false
	}
	if((Boolean)rtData.terminated){
		value=false
	}
	//restore current condition
	rtData.stack.c=c
	if(stacked){
		def tc=((List)rtData.stack.cs).pop()
	}
	rtData.stack.s=(Integer)((List)rtData.stack.ss).pop()
	rtData.systemVars['$index'].v=parentIndex
	rtData.systemVars['$device'].v=parentDevice
	rtData.conditionStateChanged=parentConditionStateChanged
	Boolean ret=value || (Integer)rtData.fastForwardTo!=0
	myDetail rtData, "executeStatement ${(String)statement.t} result: $ret", -1
	return ret
}

private Long checkForSlowdown(Map rtData){
	//return how Long over the time limit we are
	Long overBy=0L
	Long curRunTime=Math.round(1.0D*now()-(Long)rtData.timestamp-(Long)getPistonLimits.slTime)
	if(curRunTime>overBy){
		overBy=curRunTime
	}
	return overBy
}

private Long doPause(String mstr, Long delay, Map rtData){
	Long actDelay=0L
	Long t0=now()
	if(!rtData.lastPause || (t0-(Long)rtData.lastPause)>1000L){
		if((Integer)rtData.logging>1)trace mstr+'; lastPause: '+rtData.lastPause, rtData
		rtData.lastPause=t0
		pauseExecution(delay)
		Long t1=now()
		actDelay=t1-t0
		Long t2=rtData.tPause ? (Long)rtData.tPause : 0L
		rtData.tPause=t2+actDelay
		rtData.lastPause=t1
		t2=state.pauses ? (Long)state.pauses : 0L
		state.pauses=t2+1L
	}
	return actDelay
}

private Boolean executeAction(Map rtData, Map statement, Boolean async){
	myDetail rtData, "executeAction", 1
	def parentDevicesVar=rtData.systemVars['$devices'].v
	//if override
	if((Integer)rtData.fastForwardTo==0 && (String)statement.tsp!='a'){
		cancelStatementSchedules(rtData, (Integer)statement.$)
	}
	Boolean result=true
	List deviceIds=expandDeviceList(rtData, (List)statement.d)
	List devices=deviceIds.collect{ getDevice(rtData, (String)it)}
	rtData.currentAction=statement
	for (Map task in (List)statement.k){
		if(task.$!=null && (Integer)task.$==(Integer)rtData.fastForwardTo){
			//resuming a waiting task, we need to bring back the devices
			if(rtData.event && rtData.event.schedule && rtData.event.schedule.stack){
				rtData.systemVars['$index'].v=rtData.event.schedule.stack.index
				rtData.systemVars['$device'].v=rtData.event.schedule.stack.device
				if(rtData.event.schedule.stack.devices instanceof List){
					deviceIds=(List)rtData.event.schedule.stack.devices
					rtData.systemVars['$devices'].v=deviceIds
					devices=deviceIds.collect{ getDevice(rtData, (String)it)}
				}
			}
		}
		rtData.systemVars['$devices'].v=deviceIds
		result=executeTask(rtData, devices, statement, task, async)
		if(!result && (Integer)rtData.fastForwardTo==0){
			break
		}
	}
	rtData.systemVars['$devices'].v=parentDevicesVar
	myDetail rtData, "executeAction result: $result", -1
	return result
}

private Boolean executeTask(Map rtData, List devices, Map statement, Map task, Boolean async){
	//parse parameters
	def virtualDevice=(Integer)devices.size()!=0 ? null:location
	Long t=now()
	if((Integer)rtData.fastForwardTo!=0){
		if((Integer)task.$==(Integer)rtData.fastForwardTo){
			//finally found the resuming point, play nicely from hereon
			tracePoint(rtData, "t:${(Integer)task.$}", Math.round(1.0D*now()-t), null)
			rtData.fastForwardTo=0
			//restore $device and $devices
			rtData.resumed=true
		}
		//we're not doing anything, we're fast forwarding...
		return true
	}
	if(task.m!=null && task.m instanceof List && (Integer)((List)task.m).size()>0){
		if(!((String)rtData.locationModeId in (List)task.m)){
			if((Integer)rtData.logging>2)debug "Skipping task ${(Integer)task.$} because of mode restrictions", rtData
			return true
		}
	}
	myDetail rtData, "executeTask ${(String)task.c}", 1
	List params=[]
	for (Map param in (List)task.p){
		def p
		switch ((String)param.vt){
		case 'variable':
			p=param.x instanceof List ? (List)param.x:((String)param.x+((String)param.xi!=(String)null ? '['+(String)param.xi+']':''))
			break
		default:
			Map v=evaluateOperand(rtData, null, param)
			//if not selected, we want to return null
			String tt1=(String)param.vt //?: (String)v.vt
			def t0=v.v
			Boolean match=(tt1!=null && ((tt1==(String)v.t)|| (t0 instanceof String && tt1 in ['string', 'enum', 'text'])||
					(t0 instanceof Integer && tt1=='integer')||
					(t0 instanceof Long && tt1=='long')||
					(t0 instanceof Double && tt1=='decimal')||
					(t0 instanceof BigDecimal && tt1=='decimal')))
			p=(t0!=null)? (!match ? evaluateExpression(rtData, v, tt1).v:t0):null
		}
		//ensure value type is successfuly passed through
		params.push p
	}

	//handle duplicate command "push" which was replaced with fake command "pushMomentary"
	def override=CommandsOverrides.find{ (String)it.value.r==(String)task.c }
	String command=override ? (String)override.value.c:(String)task.c

	Map vcmd=VirtualCommands()[command]
	Long delay=0L
	for (device in (virtualDevice!=null ? [virtualDevice]:devices)){
		if(virtualDevice==null && device.hasCommand(command) && !(vcmd && vcmd.o /*virtual command overrides physical command*/)){
			Map msg=timer "Executed [$device].${command}", rtData
			try{
				delay="cmd_${command}"(rtData, device, params)
			} catch(all){
				executePhysicalCommand(rtData, device, command, params)
			}
			if((Integer)rtData.logging>1)trace msg, rtData
		}else{
			if(vcmd!=null){
				delay=executeVirtualCommand(rtData, vcmd.a ? devices:device, command, params)
				//aggregate commands only run once, for all devices at the same time
				if(vcmd.a)break
			}
		}
	}
	//if we don't have to wait, we're home free

	//negative delays force us to reschedule, no sleeping on this one
	Boolean reschedule=(delay<0L)
	delay=reschedule ? -delay:delay

	if(delay){
		//get remaining piston time
		if(delay>(Long)getPistonLimits.taskMaxDelay){
			reschedule=true
		}
		if(reschedule || async){
			//schedule a wake up
			Long sec=Math.round(delay/1000.0D)
			if((Integer)rtData.logging>1)trace "Requesting a wake up for ${formatLocalTime(Math.round(now()*1.0D+delay))} (in ${sec}s)", rtData
			tracePoint(rtData, "t:${(Integer)task.$}", Math.round(1.0D*now()-t), -delay)
			requestWakeUp(rtData, statement, task, delay, (String)task.c)
			myDetail rtData, "executeTask ${(String)task.c} result: FALSE", -1
			return false
		}else{
			if((Integer)rtData.logging>1)trace "executeTask: Waiting for ${delay}ms", rtData
			pauseExecution(delay)
		}
	}
	tracePoint(rtData, "t:${(Integer)task.$}", Math.round(1.0D*now()-t), delay)

	//get remaining piston time
	Long overBy=checkForSlowdown(rtData)
	if(overBy>0L){
		Long mdelay=(Long)getPistonLimits.taskShortDelay
		if(overBy>(Long)getPistonLimits.useBigDelay){
			mdelay=(Long)getPistonLimits.taskLongDelay
		}
		Long actDelay=doPause("executeTask: Execution time exceeded by ${overBy}ms, Waiting for ${mdelay}ms", mdelay, rtData)
	}
	myDetail rtData, "executeTask ${(String)task.c} result: TRUE", -1
	return true
}

private Long executeVirtualCommand(Map rtData, devices, String command, List params){
	Map msg=timer "Executed virtual command ${devices ? (devices instanceof List ? "$devices.":"[$devices]."):""}${command}", rtData
	Long delay=0
	try{
		delay="vcmd_${command}"(rtData, devices, params)
		if((Integer)rtData.logging>1)trace msg, rtData
	} catch(all){
		msg.m="Error executing virtual command ${devices instanceof List ? "$devices":"[$devices]"}.${command}:"
		msg.e=all
		error msg, rtData
	}
	return delay
}

private void executePhysicalCommand(Map rtData, device, String command, params=[], Integer delay=0, String scheduleDevice=(String)null, Boolean disableCommandOptimization=false){
	if(delay!=0 && scheduleDevice!=(String)null){
		//delay without schedules is not supported in hubitat
		//scheduleDevice=hashId(device.id)
		//we're using schedules instead
		Map statement=(Map)rtData.currentAction
		List cs=[]+(((String)statement.tcp=='b')|| ((String)statement.tcp=='c')? (rtData.stack?.cs ? (List)rtData.stack.cs:[]):[]) // task cancelation policy
		Integer ps=((String)statement.tcp=='b')|| ((String)statement.tcp=='p')? 1:0
		cs.removeAll{ it==0 }
		Map schedule=[
			t: Math.round(now()*1.0D+delay),
			s: (Integer)statement.$,
			i: -3,
			cs: cs,
			ps: ps,
			d: [
				d: scheduleDevice,
				c: command,
				p: params
			]
		]
		Boolean a=((List)rtData.schedules).push(schedule)
	}else{
		List nparams=(params instanceof List)? (List)params:(params!=null ? [params]:[])
		try{
			//cleanup the params so that SONOS works
			while ((Integer)nparams.size()>0 && nparams[(Integer)nparams.size()-1]==null)def a=nparams.pop()
			Map msg=timer '', rtData
			Boolean skip=false
			if(!rtData.piston.o?.dco && !disableCommandOptimization && !(command in ['setColorTemperature', 'setColor', 'setHue', 'setSaturation'])){
				def cmd=PhysicalCommands()[command]
				if(cmd!=null && (String)cmd.a!=(String)null){
					if(cmd.v!=null && (Integer)nparams.size()==0){
						//commands with no parameter that set an attribute to a preset value
						if(getDeviceAttributeValue(rtData, device, (String)cmd.a)==(String)cmd.v){
							skip=true
						}
					}else if((Integer)nparams.size()==1){
						if(getDeviceAttributeValue(rtData, device, (String)cmd.a)==nparams[0]){
							skip=(command in ['setLevel', 'setInfraredLevel'] ? getDeviceAttributeValue(rtData, device, 'switch')=='on':true)
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
				if(delay>(Long)getPistonLimits.taskMaxDelay)delay=1000
				Long ldelay=delay
				if(delay>0){
					pauseExecution(ldelay) //simulated in hubitat
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
			if((Integer)rtData.logging>2)debug msg, rtData
		} catch(all){
			error "Error while executing physical command $device.$command($nparams):", rtData, -2, all
		}
		Long t0=rtData.piston.o?.ced!=null ? (Integer)rtData.piston.o.ced:0L
		if(t0!=0L){
			if(t0>(Long)getPistonLimits.taskMaxDelay)t0=1000L
			pauseExecution(t0)
			if((Integer)rtData.logging>1)trace "Injected a ${t0}ms delay after [$device].$command(${nparams ? "$nparams":''})", rtData
		}
	}
}

private void scheduleTimer(Map rtData, Map timer, Long lastRun=0L){
	//if already scheduled once during this run, don't do it again
	if(((List)rtData.schedules).find{ (Integer)it.s==(Integer)timer.$ })return
	myDetail rtData, "scheduleTimer $timer     $lastRun", 1
	//complicated stuff follows...
	Long t=now()
	String tinterval="${evaluateOperand(rtData, null, (Map)timer.lo).v}".toString()
	if(!tinterval.isInteger()){
		myDetail rtData, "scheduleTimer", -1
		return
	}
	Integer interval=tinterval.toInteger()
	if(interval<=0){
		myDetail rtData, "scheduleTimer", -1
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
		time=(Long)evaluateExpression(rtData, evaluateOperand(rtData, null, (Map)timer.lo2), 'datetime').v
		if((String)timer.lo2.t!='c'){
			def offset=evaluateOperand(rtData, null, (Map)timer.lo3)
			time += (Long)evaluateExpression(rtData, [t:'duration', v:offset.v, vt:(String)offset.vt], 'long').v
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
		Long min=(Long)cast(rtData, timer.lo.om, 'long')
		nextSchedule=Math.round(3600000.0D*Math.floor(nextSchedule/3600000.0D)+(min*60000.0D))
	}

	//next date
	Integer cycles=100
	while (cycles!=0){
		if(delta!=0L){
			if(nextSchedule<(rightNow-delta)){
				//we're behind, let's fast forward to where the next occurrence happens in the future
				Long count=Math.floor((rightNow-nextSchedule)/delta*1.0D)
				//if((Integer)rtData.logging>2)debug "Timer fell behind by $count interval${count>1 ? 's':''}, catching up...", rtData
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
					Long requiredDay=(Long)cast(rtData, timer.lo.odw, 'long')
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
							day=(result<=lastDayOfMonth)? day:0
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
			Long offset=checkTimeRestrictions(rtData, timer.lo, nextSchedule, level, interval)
			if(offset==0L)break
			if(offset>0L)nextSchedule += offset
		}
		time=nextSchedule
		priorActivity=true
		cycles -= 1
	}

	if(nextSchedule>lastRun){
		((List)rtData.schedules).removeAll{ (Integer)it.s==(Integer)timer.$ }
		requestWakeUp(rtData, timer, [$: -1], nextSchedule)
	}
	myDetail rtData, "scheduleTimer", -1
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

private void scheduleTimeCondition(Map rtData, Map condition){
	myDetail rtData, "scheduleTimeCondition", 1
	Integer conditionNum=(Integer)condition.$
	//if already scheduled once during this run, don't do it again
	if(((List)rtData.schedules).find{ (Integer)it.s==conditionNum && (Integer)it.i==0 })return
	Map comparison=Comparisons().conditions[(String)condition.co]
	Boolean trigger=false
	if(comparison==null){
		comparison=Comparisons().triggers[(String)condition.co]
		if(comparison==null)return
		trigger=true
	}
	cancelStatementSchedules(rtData, conditionNum)
	if(!comparison.p)return
	def tv1=condition.ro!=null && ((String)condition.ro.t!='c')? evaluateOperand(rtData, null, (Map)condition.to):null

	def v1a=evaluateOperand(rtData, null, (Map)condition.ro)
	Long tt0=(Long)v1a.v
	Long v1b=( (String)v1a.t=='time' && tt0<86400000L) ? getTimeToday(tt0):tt0
	Long v1c=(tv1 ? (((String)tv1.t=='integer' && (Integer)tv1.v==0)? 0L:(Long)evaluateExpression(rtData, [t:'duration', v:tv1.v, vt:(String)tv1.vt], 'long').v):0L)
	Long v1=v1b+v1c

	def tv2=condition.ro2!=null && (String)condition.ro2.t!='c' && (Integer)comparison.p>1 ? evaluateOperand(rtData, null, (Map)condition.to2):null
	Long v2=trigger ? v1 : ((Integer)comparison.p>1 ? ((Long)evaluateExpression(rtData, evaluateOperand(rtData, null, (Map)condition.ro2, null, false, true), 'datetime').v + (tv2 ? evaluateExpression(rtData, [t:'duration', v:tv2.v, vt:(String)tv2.vt]).v : 0)) : ((String)condition.lo.v=='time' ? getMidnightTime():v1))
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
		if((Integer)rtData.logging>2)debug "Requesting time schedule wake up at ${formatLocalTime(n)}", rtData
		requestWakeUp(rtData, condition, [$:0], n)
	}
	myDetail rtData, "scheduleTimeCondition", -1
}

private Long checkTimeRestrictions(Map rtData, Map operand, Long time, Integer level, Integer interval){
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
				odm.removeAll{ it<1 }
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


private void requestWakeUp(Map rtData, Map statement, Map task, Long timeOrDelay, String data=(String)null){
	Long time=timeOrDelay>9999999999 ? timeOrDelay:now()+timeOrDelay
	List cs=[]+(((String)statement.tcp=='b')|| ((String)statement.tcp=='c')? (rtData.stack?.cs!=null ? (List)rtData.stack.cs:[]):[]) // task cancelation policy
	Integer ps=((String)statement.tcp=='b')|| ((String)statement.tcp=='p')? 1:0
	cs.removeAll{ it==0 }
// state to save across a sleep
	Map schedule=[
		t:time,
		s:(Integer)statement.$,
		i:task?.$ ? (Integer)task.$:0,
		cs:cs,
		ps:ps,
		d:data,
		evt:rtData.currentEvent,
		args:rtData.args,
		stack:[
			index:rtData.systemVars['$index'].v,
			device:rtData.systemVars['$device'].v,
			devices:rtData.systemVars['$devices'].v,
			json:rtData.json ?: [:],
			response:rtData.response ?: [:]
// what about previousEvent httpContentType httpStatusCode httpStatusOk iftttStatusCode iftttStatusOk "\$mediaId" "\$mediaUrl" "\$mediaType" mediaData (big)
// currentEvent in case of httpRequest
		]
	]
	Boolean a=((List)rtData.schedules).push(schedule)
}

private Long do_setLevel(Map rtData, device, List params, String attr, val=null){
	Integer arg=val!=null ? (Integer)val:(Integer)params[0]
	String mstate=(Integer)params.size()>1 ? (String)params[1]:(String)null
	if(mstate!=(String)null && (getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	Integer delay=(Integer)params.size()>2 ? (Integer)params[2]:0
	executePhysicalCommand(rtData, device, attr, arg, delay)
	return 0L
}

private Long cmd_setLevel(Map rtData, device, List params){
	return do_setLevel(rtData, device, params, 'setLevel')
}

private Long cmd_setInfraredLevel(Map rtData, device, List params){
	return do_setLevel(rtData, device, params, 'setInfraredLevel')
}

private Long cmd_setHue(Map rtData, device, List params){
	Integer hue=(Integer)cast(rtData, Math.round(params[0]/3.6D), 'integer')
	return do_setLevel(rtData, device, params, 'setHue', hue)
}

private Long cmd_setSaturation(Map rtData, device, List params){
	return do_setLevel(rtData, device, params, 'setSaturation')
}

private Long cmd_setColorTemperature(Map rtData, device, List params){
	return do_setLevel(rtData, device, params, 'setColorTemperature')
}

private Map getColor(Map rtData, String colorValue){
	Map color=(colorValue=='Random')? getRandomColor(rtData):getColorByName(rtData, colorValue)
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

private Long cmd_setColor(Map rtData, device, List params){
	def color=getColor(rtData, (String)params[0])
	if(!color){
		error "ERROR: Invalid color $params", rtData
		return 0L
	}
	String mstate=(Integer)params.size()>1 ? (String)params[1]:(String)null
	if(mstate!=(String)null && (getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	Integer delay=(Integer)params.size()>2 ? (Integer)params[2]:0
	executePhysicalCommand(rtData, device, 'setColor', color, delay)
	return 0L
}

private Long cmd_setAdjustedColor(Map rtData, device, List params){
	def color=getColor(rtData, (String)params[0])
	if(!color){
		error "ERROR: Invalid color $params", rtData
		return 0L
	}
	Long duration=(Long)cast(rtData, params[1], 'long')
	String mstate=(Integer)params.size()>2 ? (String)params[2]:(String)null
	if(mstate!=(String)null && ((String)getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	Integer delay=(Integer)params.size()>3 ? (Integer)params[3]:0
	executePhysicalCommand(rtData, device, 'setAdjustedColor', [color, duration], delay)
	return 0L
}

private Long cmd_setAdjustedHSLColor(Map rtData, device, List params){
	Integer hue=(Integer)cast(rtData, Math.round(params[0]/3.6D), 'integer')
	Integer saturation=(Integer)params[1]
	Integer level=(Integer)params[2]
	def color=[
		hue: hue,
		saturation: saturation,
		level: level
	]
	Long duration=(Long)cast(rtData, params[3], 'long')
	String mstate=(Integer)params.size()>4 ? (String)params[4]:(String)null
	Integer delay=(Integer)params.size()>5 ? (Integer)params[5]:0
	if(mstate!=(String)null && ((String)getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	executePhysicalCommand(rtData, device, 'setAdjustedColor', [color, duration], delay)
	return 0L
}

private Long cmd_setLoopDuration(Map rtData, device, List params){
	Integer duration=(Integer)Math.round((Long)cast(rtData, params[0], 'long')/1000)
	executePhysicalCommand(rtData, device, 'setLoopDuration', duration)
	return 0L
}

private Long cmd_setVideoLength(Map rtData, device, List params){
	Integer duration=(Integer)Math.round((Long)cast(rtData, params[0], 'long')/1000)
	executePhysicalCommand(rtData, device, 'setVideoLength', duration)
	return 0L
}


private Long vcmd_log(Map rtData, device, List params){
	String command=params[0] ? (String)params[0]:''
	String message=(String)params[1]
	Map a=log(message, rtData, -2, null, command.toLowerCase().trim(), true)
	return 0L
}

private Long vcmd_setState(Map rtData, device, List params){
	String value=params[0]
	if(rtData.piston.o?.mps){
		rtData.state.new=value
		rtData.pistonStateChanged=(Boolean)rtData.pistonStateChanged || ((String)rtData.state.old!=(String)rtData.state.new)
	}else{
		error "Cannot set the piston state while in automatic mode. Please edit the piston settings to disable the automatic piston state if you want to manually control the state.", rtData
	}
	return 0L
}

private Long vcmd_setTileColor(Map rtData, device, List params){
	Integer index=(Integer)cast(rtData, params[0], 'integer')
	if((index<1)|| (index>16))return 0L
	rtData.state["c$index"]=(String)getColor(rtData, (String)params[1])?.hex
	rtData.state["b$index"]=(String)getColor(rtData, (String)params[2])?.hex
	rtData.state["f$index"]=!!params[3]
	return 0L
}

private Long vcmd_setTileTitle(Map rtData, device, List params){
	return helper_setTile(rtData, 'i', params)
}

private Long vcmd_setTileText(Map rtData, device, List params){
	return helper_setTile(rtData, 't', params)
}

private Long vcmd_setTileFooter(Map rtData, device, List params){
	return helper_setTile(rtData, 'o', params)
}

private Long vcmd_setTileOTitle(Map rtData, device, List params){
	return helper_setTile(rtData, 'p', params)
}

private Long helper_setTile(Map rtData, String typ, List params){
	Integer index=(Integer)cast(rtData, params[0], 'integer')
	if((index<1)|| (index>16))return 0L
	rtData.state["${typ}$index"]=(String)params[1]
	return 0L
}

private Long vcmd_setTile(Map rtData, device, List params){
	Integer index=(Integer)cast(rtData, params[0], 'integer')
	if((index<1)|| (index>16))return 0L
	rtData.state["i$index".toString()]=(String)params[1]
	rtData.state["t$index".toString()]=(String)params[2]
	rtData.state["o$index".toString()]=(String)params[3]
	rtData.state["c$index".toString()]=(String)getColor(rtData, (String)params[4])?.hex
	rtData.state["b$index".toString()]=(String)getColor(rtData, (String)params[5])?.hex
	rtData.state["f$index".toString()]=!!params[6]
	return 0L
}

private Long vcmd_clearTile(Map rtData, device, List params){
	Integer index=(Integer)cast(rtData, params[0], 'integer')
	if((index<1)|| (index>16))return 0L
	Map t0=rtData.state
	t0.remove("i$index".toString())
	t0.remove("t$index".toString())
	t0.remove("c$index".toString())
	t0.remove("o$index".toString())
	t0.remove("b$index".toString())
	t0.remove("f$index".toString())
	t0.remove("p$index".toString())
	rtData.state=t0
	return 0L
}

private Long vcmd_setLocationMode(Map rtData, device, List params){
	String modeIdOrName=(String)params[0]
	def mode=location.getModes()?.find{ (hashId((Long)it.id)==modeIdOrName)|| ((String)it.name==modeIdOrName)}
	if(mode){
		location.setMode((String)mode.name)
	}else{
		error "Error setting location mode. Mode '$modeIdOrName' does not exist.", rtData
	}
	return 0L
}

private Long vcmd_setAlarmSystemStatus(Map rtData, device, List params){
	String statusIdOrName=(String)params[0]
	def dev=VirtualDevices()['alarmSystemStatus']
	def options=dev?.ac
	def status=options?.find{ (String)it.key==statusIdOrName || (String)it.value==statusIdOrName }?.collect{ [id: (String)it.key, name: it.value] }

	if(status && (Integer)status.size()!=0){
		sendLocationEvent(name:'hsmSetArm', value: status[0].id)
	}else{
		error "Error setting HSM status. Status '$statusIdOrName' does not exist.", rtData
	}
	return 0L
}

private Long vcmd_sendEmail(Map rtData, device, List params){
	def data=[
		i: (String)rtData.id,
		n: (String)app.label,
		t: (String)params[0],
		s: (String)params[1],
		m: (String)params[2]
	]

	Map requestParams=[
		uri: "https://api.webcore.co/email/send/${(String)rtData.locationId}".toString(),
		query: null,
		headers: (auth ? [Authorization: auth]:[:]),
		requestContentType: "application/json",
		body: data
	]
	String msg='Unknown error'

	try{
		asynchttpPost('ahttpRequestHandler', requestParams, [command:'sendEmail', em: data])
		return 24000
	} catch (all){
		error "Error sending email to ${data.t}: $msg", rtData
	}
	return 0L
}

private static Long vcmd_noop(Map rtData, device, List params){
	return 0L
}

private Long vcmd_wait(Map rtData, device, List params){
	return (Long)cast(rtData, params[0], 'long')
}

private Long vcmd_waitRandom(Map rtData, device, List params){
	Long min=(Long)cast(rtData, params[0], 'long')
	Long max=(Long)cast(rtData, params[1], 'long')
	if(max<min){
		Long v=max
		max=min
		min=v
	}
	return min+(Integer)Math.round((max-min)*Math.random())
}

private Long vcmd_waitForTime(Map rtData, device, List params){
	Long time
	time=(Long)cast(rtData, (Long)cast(rtData, params[0], 'time'), 'datetime', 'time')
	Long rightNow=now()
	time=pushTimeAhead(time, rightNow)
	return time-rightNow
}

private Long vcmd_waitForDateTime(Map rtData, device, List params){
	Long time=(Long)cast(rtData, params[0], 'datetime')
	Long rightNow=now()
	return (time>rightNow)? time-rightNow:0
}

private Long vcmd_setSwitch(Map rtData, device, List params){
	if((Boolean)cast(rtData, params[0], 'boolean')){
		executePhysicalCommand(rtData, device, 'on')
	}else{
		executePhysicalCommand(rtData, device, 'off')
	}
	return 0L
}

private Long vcmd_toggle(Map rtData, device, List params){
	if((String)getDeviceAttributeValue(rtData, device, 'switch')=='off'){
		executePhysicalCommand(rtData, device, 'on')
	}else{
		executePhysicalCommand(rtData, device, 'off')
	}
	return 0L
}

private Long vcmd_toggleRandom(Map rtData, device, List params){
	Integer probability=(Integer)cast(rtData, (Integer)params.size()==1 ? params[0]:50, 'integer')
	if(probability<=0)probability=50
	if(Math.round(100*Math.random())<=probability){
		executePhysicalCommand(rtData, device, 'on')
	}else{
		executePhysicalCommand(rtData, device, 'off')
	}
	return 0L
}

private Long vcmd_toggleLevel(Map rtData, device, List params){
	Integer level=params[0]
	if(getDeviceAttributeValue(rtData, device, 'level')==level){
		executePhysicalCommand(rtData, device, 'setLevel', 0)
	}else{
		executePhysicalCommand(rtData, device, 'setLevel', level)
	}
	return 0L
}

private Long do_adjustLevel(Map rtData, device, List params, String attr, String attr1, val=null, Boolean big=false){
	Integer arg=val!=null ? (Integer)val:(Integer)cast(rtData, params[0], 'integer')
	String mstate=(Integer)params.size()>1 ? (String)params[1]:(String)null
	Integer delay=(Integer)params.size()>2 ? (Integer)params[2]:0
	if(mstate!=(String)null && (getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	arg=arg+(Integer)cast(rtData, getDeviceAttributeValue(rtData, device, attr), 'integer')
	Integer low=big ? 1000:0
	Integer hi=big ? 30000:100
	arg=(arg<low)? low:((arg>hi)? hi:arg)
	executePhysicalCommand(rtData, device, attr1, arg, delay)
	return 0L
}

private Long vcmd_adjustLevel(Map rtData, device, List params){
	return do_adjustLevel(rtData, device, params, 'level', 'setLevel')
}

private Long vcmd_adjustInfraredLevel(Map rtData, device, List params){
	return do_adjustLevel(rtData, device, params, 'infraredLevel', 'setInfraredLevel')
}

private Long vcmd_adjustSaturation(Map rtData, device, List params){
	return do_adjustLevel(rtData, device, params, 'saturation', 'setSaturation')
}

private Long vcmd_adjustHue(Map rtData, device, List params){
	Integer hue=(Integer)cast(rtData, Math.round(params[0]/3.6D), 'integer')
	return do_adjustLevel(rtData, device, params, 'hue', 'setHue', hue)
}

private Long vcmd_adjustColorTemperature(Map rtData, device, List params){
	return do_adjustLevel(rtData, device, params, 'colorTemperature', 'setColorTemperature', null, true)
}

private Long do_fadeLevel(Map rtData, device, List params, String attr, String attr1, val=null, val1=null, Boolean big=false){
	Integer startlevel
	Integer endLevel
	if(val==null){
		startLevel=(params[0]!=null)? (Integer)cast(rtData, params[0], 'integer'):(Integer)cast(rtData, getDeviceAttributeValue(rtData, device, attr), 'integer')
		endLevel=(Integer)cast(rtData, params[1], 'integer')
	}else{
		startlevel=(Integer)val
		endLevel=(Integer)val1
	}
	Long duration=(Long)cast(rtData, params[2], 'long')
	String mstate=(Integer)params.size()>3 ? (String)params[3]:(String)null
	if(mstate!=(String)null && (getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	Integer low=big ? 1000:0
	Integer hi=big ? 30000:100
	startLevel=(startLevel<low)? low:((startLevel>hi)? hi:startLevel)
	endLevel=(endLevel<low)? low:((endLevel>hi)? hi:endLevel)
	return vcmd_internal_fade(rtData, device, attr1, startLevel, endLevel, duration)
}

private Long vcmd_fadeLevel(Map rtData, device, List params){
	return do_fadeLevel(rtData, device, params, 'level', 'setLevel')
}

private Long vcmd_fadeInfraredLevel(Map rtData, device, List params){
	return do_fadeLevel(rtData, device, params, 'infraredLevel', 'setInfraredLevel')
}

private Long vcmd_fadeSaturation(Map rtData, device, List params){
	return do_fadeLevel(rtData, device, params, 'saturation', 'setSaturation')
}

private Long vcmd_fadeHue(Map rtData, device, List params){
	Integer startLevel=(params[0]!=null)? (Integer)cast(rtData, Math.round((Integer)params[0]/3.6D), 'integer'):(Integer)cast(rtData, getDeviceAttributeValue(rtData, device, 'hue'), 'integer')
	Integer endLevel=(Integer)cast(rtData, Math.round((Integer)params[1]/3.6D), 'integer')
	return do_fadeLevel(rtData, device, params, 'hue', 'setHue', startLevel, endLevel)
}

private Long vcmd_fadeColorTemperature(Map rtData, device, List params){
	return do_fadeLevel(rtData, device, params, 'colorTemperature', 'setColorTemperature', null, null, true)
}

private Long vcmd_internal_fade(Map rtData, device, String command, Integer startLevel, Integer endLevel, Long duration){
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
		executePhysicalCommand(rtData, device, command, endLevel)
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
	String scheduleDevice=(duration>10000L)? hashId(device.id):(String)null
	Integer oldLevel=startLevel
	executePhysicalCommand(rtData, device, command, startLevel)
	for(Integer i=1; i<=steps; i++){
		Integer newLevel=Math.round(startLevel+delta*i/steps*1.0D)
		if(oldLevel!=newLevel){
			executePhysicalCommand(rtData, device, command, newLevel, i*interval, scheduleDevice, true)
		}
		oldLevel=newLevel
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, command, endLevel, duration+99L, scheduleDevice, true)
	return duration+100L
}

private Long vcmd_emulatedFlash(Map rtData, device, List params){
	vcmd_flash(rtData, device, params)
}

private Long vcmd_flash(Map rtData, device, List params){
	Long onDuration=(Long)cast(rtData, params[0], 'long')
	Long offDuration=(Long)cast(rtData, params[1], 'long')
	Integer cycles=(Integer)cast(rtData, params[2], 'integer')
	String mstate=(Integer)params.size()>3 ? (String)params[3]:(String)null
	String currentState=getDeviceAttributeValue(rtData, device, 'switch')
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
	String scheduleDevice=(duration>10000L)? hashId(device.id):(String)null
	Long dur=0L
	for(Integer i=1; i<=cycles; i++){
		executePhysicalCommand(rtData, device, firstCommand, [], dur, scheduleDevice, true)
		dur += firstDuration
		executePhysicalCommand(rtData, device, secondCommand, [], dur, scheduleDevice, true)
		dur += secondDuration
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, currentState, [], duration+99L, scheduleDevice, true)
	return duration+100L
}

private Long vcmd_flashLevel(Map rtData, device, List params){
	Integer level1=(Integer)cast(rtData, params[0], 'integer')
	Long duration1=(Long)cast(rtData, params[1], 'long')
	Integer level2=(Integer)cast(rtData, params[2], 'integer')
	Long duration2=(Long)cast(rtData, params[3], 'long')
	Integer cycles=(Integer)cast(rtData, params[4], 'integer')
	String mstate=(Integer)params.size()>5 ? (String)params[5]:(String)null
	String currentState=getDeviceAttributeValue(rtData, device, 'switch')
	if(mstate!=(String)null && (currentState!=mstate)){
		return 0L
	}
	def currentLevel=getDeviceAttributeValue(rtData, device, 'level')
	Long duration=Math.round((duration1+duration2)*cycles*1.0D)
	if(duration<=500L){
		//if the flash is too fast, ignore it
		return 0L
	}
	String scheduleDevice=(duration>10000L)? hashId(device.id):(String)null
	Long dur=0L
	for(Integer i=1; i<=cycles; i++){
		executePhysicalCommand(rtData, device, 'setLevel', [level1], dur, scheduleDevice, true)
		dur += duration1
		executePhysicalCommand(rtData, device, 'setLevel', [level2], dur, scheduleDevice, true)
		dur += duration2
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, 'setLevel', [currentLevel], duration+98L, scheduleDevice, true)
	executePhysicalCommand(rtData, device, currentState, [], duration+99L, scheduleDevice, true)
	return duration+100L
}

private Long vcmd_flashColor(Map rtData, device, List params){
	def color1=getColor(rtData, (String)params[0])
	Long duration1=(Long)cast(rtData, params[1], 'long')
	def color2=getColor(rtData, (String)params[2])
	Long duration2=cast(rtData, params[3], 'long')
	Integer cycles=(Integer)cast(rtData, params[4], 'integer')
	String mstate=(Integer)params.size()>5 ? (String)params[5]:(String)null
	String currentState=getDeviceAttributeValue(rtData, device, 'switch')
	if(mstate!=(String)null && (currentState!=mstate)){
		return 0L
	}
	Long duration=Math.round((duration1+duration2)*cycles*1.0D)
	if(duration<=500L){
		//if the flash is too fast, ignore it
		return 0L
	}
	String scheduleDevice=(duration>10000L)? hashId(device.id):(String)null
	Long dur=0
	for(Integer i=1; i<=cycles; i++){
		executePhysicalCommand(rtData, device, 'setColor', [color1], dur, scheduleDevice, true)
		dur += duration1
		executePhysicalCommand(rtData, device, 'setColor', [color2], dur, scheduleDevice, true)
		dur += duration2
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, currentState, [], duration+99L, scheduleDevice, true)
	return duration+100L
}

private Long vcmd_sendNotification(Map rtData, device, List params){
	def message="Hubitat does not support sendNotification "+params[0]
	Map a=log(message, rtData, -2, "Err", 'warn', true)
	//sendNotificationEvent(message)
	return 0L
}

private Long vcmd_sendPushNotification(Map rtData, device, List params){
	String message=(String)params[0]
	if(rtData.initPush==null){
		rtData.pushDev=(List)parent.getPushDev()
		rtData.initPush=true
	}
	List t0=(List)rtData.pushDev
	try{
		t0*.deviceNotification(message)
	} catch (all){
		message="Default push device not set properly in webCoRE "+params[0]
		error message, rtData
	}
	return 0L
}

private Long vcmd_sendSMSNotification(Map rtData, device, List params){
	String message=(String)params[0]
	String msg="HE SMS notifications are being removed, please convert to a notification device "+params[0]
	warn msg, rtData
	return 0L
}

private Long vcmd_sendNotificationToContacts(Map rtData, device, List params){
	// Contact Book has been disabled and we're falling back onto PUSH notifications, if the option is on
	String message=(String)params[0]
	def save=!!params[2]
	return vcmd_sendPushNotification(rtData, devices, [message, save])
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

private Long vcmd_setVariable(Map rtData, device, List params){
	String name=(String)params[0]
	def value=params[1]
	myDetail rtData, "setVariable $name  $value"
	def t0=setVariable(rtData, name, value)
	return 0L
}

private Long vcmd_executePiston(Map rtData, device, List params){
	String selfId=(String)rtData.id
	String pistonId=(String)params[0]
	List arguments=(params[1] instanceof List ? (List)params[1]:params[1].toString().tokenize(',')).unique()
	Boolean wait=((Integer)params.size()>2)? (Boolean)cast(rtData, params[2], 'boolean'):false
	String description="webCoRE: Piston ${(String)app.label} requested execution of piston $pistonId".toString()
	Map data=[:]
	for (String argument in arguments){
		if(argument)data[argument]=getVariable(rtData, argument).v
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

private Long vcmd_pausePiston(Map rtData, device, List params){
	String selfId=(String)rtData.id
	String pistonId=(String)params[0]
	if(!(Boolean)parent.pausePiston(pistonId)){
		message="Piston not found "+pistonId
		error message, rtData
	}
	return 0L
}

private Long vcmd_resumePiston(Map rtData, device, List params){
	String selfId=(String)rtData.id
	String pistonId=(String)params[0]
	if(!(Boolean)parent.resumePiston(pistonId)){
		message="Piston not found "+pistonId
		error message, rtData
	}
	return 0L
}

private Long vcmd_executeRule(Map rtData, device, List params){
	String ruleId=(String)params[0]
	String action=(String)params[1]
	Boolean wait=((Integer)params.size()>2)? (Boolean)cast(rtData, params[2], 'boolean'):false
	def rules=RMUtils.getRuleList()
	def myRule=[]
	rules.each{rule->
		def t0=rule.find{ hashId((String)it.key)==ruleId }.collect{(String)it.key}
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
		error message, rtData
	}
	return 0L
}

private Long vcmd_setHSLColor(Map rtData, device, List params){
	Integer hue=(Integer)cast(rtData, Math.round(params[0]/3.6D), 'integer')
	Integer saturation=params[1]
	Integer level=params[2]
	def color=[
		hue: hue,
		saturation: saturation,
		level: level
	]
	String mstate=(Integer)params.size()>3 ? (String)params[3]:(String)null
	Integer delay=(Integer)params.size()>4 ? params[4]:0
	if(mstate!=(String)null && (getDeviceAttributeValue(rtData, device, 'switch')!=mstate)){
		return 0L
	}
	executePhysicalCommand(rtData, device, 'setColor', color, delay)
	return 0L
}

private Long vcmd_wolRequest(Map rtData, device, List params){
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

private Long vcmd_iftttMaker(Map rtData, device, List params){
	if(rtData.settings==null){
		error "no settings", rtData
	}
	String key=((String)rtData.settings.ifttt_url ?: '').trim().replace('https://', '').replace('http://', '').replace('maker.ifttt.com/use/', '')
	if(!key){
		error "Failed to send IFTTT event, because the IFTTT integration is not properly set up. Please visit Settings in your dashboard and configure the IFTTT integration.", rtData
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
	def requestParams=[
		uri: "https://maker.ifttt.com/trigger/${java.net.URLEncoder.encode(event, "UTF-8")}/with/key/"+key,
		requestContentType: "application/json",
		body: body
	]
	httpPost(requestParams){ response ->
		setSystemVariableValue(rtData, '$iftttStatusCode', response.status)
		setSystemVariableValue(rtData, '$iftttStatusOk', response.status==200)
		return 0L
	}
	return 0L
}

private Long vcmd_httpRequest(Map rtData, device, List params){
	String uri=((String)params[0]).replace(" ", "%20")
	if(!uri){
		error "Error executing external web request: no URI", rtData
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
		warn "Invalid URI for web request: $uri", rtData
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
			data[variable]=getVariable(rtData, variable).v
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
		if((Integer)rtData.logging>2)debug "Sending ${func} web request to: $uri", rtData
		if(func){
			"$func"('ahttpRequestHandler', requestParams, [command:'httpRequest'])
			return 24000L
		}
	} catch (all){
		error "Error executing external web request: ", rtData, -2, all
	}
	return 0L
}

public void ahttpRequestHandler(resp, Map callbackData){
	Boolean binary=false
	def t0=resp.getHeaders()
	String t1=t0 && t0."Content-Type" ? t0."Content-Type":(String)null
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
	if(callBackC=='sendEmail'){
		Boolean success=false
		String msg='Unknown error'
		def em=callbackData?.em
		if(resp.status==200){
			data=resp.getJson()
			if(data){
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
						if(data && data instanceof Map){
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
	}

	handleEvents([date: new Date(), device: location, name:'wc_async_reply', value: callBackC, contentType: mediaType, responseData: data, jsonData:[:], responseCode: resp.status, setRtData: setRtData])
}

private Long vcmd_writeToFuelStream(Map rtData, device, List params){
	String canister=(String)params[0]
	String name=(String)params[1]
	def data=params[2]
	def source=params[3]

	if(rtData.useLocalFuelStreams==null){
		rtData.useLocalFuelStreams=(Boolean)parent.useLocalFuelStreams()
	}

	def req=[
		c: canister,
		n: name,
		s: source,
		d: data,
		i: (String)rtData.instanceId
	]

	if((Boolean)rtData.useLocalFuelStreams){
		parent.writeToFuelStream(req)
	}else{
		log.error "Fuel stream app is not installed. Install it to write to local fuel streams", rtData
	}
	return 0L
}

private Long vcmd_storeMedia(Map rtData, device, List params){
	if(!rtData.mediaData || !rtData.mediaType || !(rtData.mediaData)|| ((Integer)rtData.mediaData.size()<=0)){
		error 'No media is available to store; operation aborted.', rtData
		return 0L
	}
	String data=new String(rtData.mediaData, 'ISO_8859_1')
	Map requestParams=[
		uri: "https://api-${rtData.region}-${rtData.instanceId[32]}.webcore.co:9247".toString(),
		path: "/media/store",
		headers: [
			'ST':(String)rtData.instanceId,
			'media-type':rtData.mediaType
		],
		body: data,
		requestContentType: rtData.mediaType
	]
	asynchttpPut(asyncHttpRequestHandler, requestParams, [command:'storeMedia'])
	return 24000L
}

public void asyncHttpRequestHandler(response, Map callbackData){
	def mediaId
	def mediaUrl
	if(response.status==200){
		def data=response.getJson()
		if(data.result=='OK' && data.url){
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

private Long vcmd_saveStateLocally(Map rtData, device, List params, Boolean global=false){
	List attributes=((String)cast(rtData, params[0], 'string')).tokenize(',')
	String canister=((Integer)params.size()>1 ? (String)cast(rtData, params[1], 'string')+':':'')+hashId(device.id)+':'
	Boolean overwrite=!((Integer)params.size()>2 ? (Boolean)cast(rtData, params[2], 'boolean'):false)
	for (String attr in attributes){
		String n=canister+attr
		if(global && !(Boolean)rtData.initGStore){
			rtData.globalStore=(Map)parent.getGStore()
			rtData.initGStore=true
		}
		if(overwrite || (global ? (rtData.globalStore[n]==null):(rtData.store[n]==null))){
			def value=getDeviceAttributeValue(rtData, device, attr)
			if(attr=='hue')value=value*3.6D
			if(global){
				rtData.globalStore[n]=value
				Map cache=rtData.gvStoreCache ?: [:]
				cache[n]=value
				rtData.gvStoreCache=cache
			}else{
				rtData.store[n]=value
			}
		}
	}
	return 0L
}

private Long vcmd_saveStateGlobally(Map rtData, device, List params){
	return vcmd_saveStateLocally(rtData, device, params, true)
}

private Long vcmd_loadStateLocally(Map rtData, device, List params, Boolean global=false){
	List attributes=((String)cast(rtData, params[0], 'string')).tokenize(',')
	String canister=((Integer)params.size()>1 ? (String)cast(rtData, params[1], 'string')+':':'')+hashId(device.id)+':'
	Boolean empty=(Integer)params.size()>2 ? (Boolean)cast(rtData, params[2], 'boolean'):false
	for (String attr in attributes){
		String n=canister+attr
		if(global && !(Boolean)rtData.initGStore){
			rtData.globalStore=(Map)parent.getGStore()
			rtData.initGStore=true
		}
		def value=global ? rtData.globalStore[n]:rtData.store[n]
		if(attr=='hue')value=(Double)cast(rtData, value, 'decimal')/3.6D
		if(empty){
			if(global){
				rtData.globalStore.remove(n)
				Map cache=rtData.gvStoreCache ?: [:]
				cache[n]=null
				rtData.gvStoreCache=cache
			}else{
				rtData.store.remove(n)
			}
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
		if(exactCommand){
			if((Integer)rtData.logging>2)debug "${t0} $exactCommand()", rtData
			executePhysicalCommand(rtData, device, exactCommand)
			continue
		}
		if(fuzzyCommand){
			if((Integer)rtData.logging>2)debug "${t0} $fuzzyCommand($value)", rtData
			executePhysicalCommand(rtData, device, fuzzyCommand, value)
			continue
		}
		warn "Could not find a command to set attribute '$attr' to value '$value'", rtData
	}
	return 0L
}

private Long vcmd_loadStateGlobally(Map rtData, device, List params){
	return vcmd_loadStateLocally(rtData, device, params, true)
}

private Long vcmd_parseJson(Map rtData, device, List params){
	String data=params[0]
	try{
		if(data.startsWith('{') && data.endsWith('}')){
			rtData.json=(LinkedHashMap)new groovy.json.JsonSlurper().parseText(data)
		}else if(data.startsWith('[') && data.endsWith(']')){
			rtData.json=(List)new groovy.json.JsonSlurper().parseText(data)
		}else{
			rtData.json=[:]
		}
	} catch (all){
		error "Error parsing JSON data $data", rtData
	}
	return 0L
}

private static Long vcmd_cancelTasks(Map rtData, device, List params){
	rtData.cancelations.all=true
	return 0L
}

private Boolean evaluateFollowedByCondition(Map rtData, Map condition, String collection, Boolean async, ladderUpdated){
	Boolean result=evaluateCondition(rtData, condition, collection, async)
}

private Boolean evaluateConditions(Map rtData, Map conditions, String collection, Boolean async){
	myDetail rtData, "evaluateConditions", 1
	Long t=now()
	Map msg=timer '', rtData
	//override condition id
	Integer c=(Integer)rtData.stack.c
	Integer myC=conditions.$ ? (Integer)conditions.$:0
	rtData.stack.c=myC
	Boolean not=(collection=='c')? !!conditions.n:!!conditions.rn
	String grouping=(collection=='c')? (String)conditions.o:(String)conditions.rop
	Boolean value=(grouping=='or' ? false:true)


	if(grouping=='followed by' && collection=='c'){
		if((Integer)rtData.fastForwardTo==0 || (Integer)rtData.fastForwardTo==myC){
			//we're dealing with a followed by condition
			Integer ladderIndex=(Integer)cast(rtData, rtData.cache["c:fbi:${myC}".toString()], 'integer')
			Long ladderUpdated=(Long)cast(rtData, rtData.cache["c:fbt:${myC}".toString()], 'datetime')
			Integer steps=conditions[collection] ? (Integer)conditions[collection].size():0
			if(ladderIndex>=steps){
				value=false
			}else{
				def condition=conditions[collection][ladderIndex]
				Long duration=0L
				if(ladderIndex){
					def tv=evaluateOperand(rtData, null, (Map)condition.wd)
					duration=(Long)evaluateExpression(rtData, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v
				}
				if(ladderUpdated && duration!=0L && (ladderUpdated+duration<now())){
					//time has expired
					value=((String)condition.wt=='n')
					if(!value){
						if((Integer)rtData.logging>2)debug "Conditional ladder step failed due to a timeout", rtData
					}
				}else{
					value=evaluateCondition(rtData, condition, collection, async)
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
					cancelStatementSchedules(rtData, myC)
					if((Integer)rtData.logging>2)debug "Condition group #${myC} made progress up the ladder; currently at step $ladderIndex of $steps", rtData
					if(ladderIndex<steps){
						//delay decision, there are more steps to go through
						value=null
						condition=conditions[collection][ladderIndex]
						def tv=evaluateOperand(rtData, null, (Map)condition.wd)
						duration=(Long)evaluateExpression(rtData, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v
						requestWakeUp(rtData, conditions, conditions, duration)
					}
				}
			}

			switch (value){
			case null:
				//we need to exit time events set to work out the timeouts...
				if((Integer)rtData.fastForwardTo==myC)rtData.terminated=true
				break
			case true:
			case false:
				//ladder either collapsed or finished, reset data
				ladderIndex=0
				ladderUpdated=0L
				cancelStatementSchedules(rtData, myC)
				break
			}
			if((Integer)rtData.fastForwardTo==myC)rtData.fastForwardTo=0
			rtData.cache["c:fbi:${myC}".toString()]=ladderIndex
			rtData.cache["c:fbt:${myC}".toString()]=ladderUpdated
		}
	}else{
		for(condition in conditions[collection]){
			Boolean res=evaluateCondition(rtData, condition, collection, async)
			value=(grouping=='or')? value || res:value && res
			//condition traversal optimizations go here
			if((Integer)rtData.fastForwardTo==0 && !rtData.piston.o?.cto && ((value && grouping=='or') || (!value && grouping=='and')))break
		}
	}
	Boolean result=false
	if(value!=null){
		result=not ? !value:value
	}
	if(value!=null && myC!=0){
		if((Integer)rtData.fastForwardTo==0)tracePoint(rtData, "c:${myC}", Math.round(1.0D*now()-t), result)
		Boolean oldResult=!!rtData.cache["c:${myC}".toString()]
		rtData.conditionStateChanged=(oldResult!=result)
		if((Boolean)rtData.conditionStateChanged){
			//condition change, perform Task Cancellation Policy TCP
			cancelConditionSchedules(rtData, myC)
		}
		rtData.cache["c:${myC}".toString()]=result
		//true/false actions
		if(collection=='c'){
			if((result || (Integer)rtData.fastForwardTo!=0) && conditions.ts!=null && (List)(conditions.ts).length)Boolean a=executeStatements(rtData, (List)conditions.ts, async)
			if((!result || (Integer)rtData.fastForwardTo!=0) && conditions.fs!=null && (List)(conditions.fs).length)Boolean a=executeStatements(rtData, (List)conditions.fs, async)
		}
		if((Integer)rtData.fastForwardTo==0){
			msg.m="Condition group #${myC} evaluated $result (state ${(Boolean)rtData.conditionStateChanged ? 'changed':'did not change'})"
			if((Integer)rtData.logging>2)debug msg, rtData
		}
	}
	//restore condition id
	rtData.stack.c=c
	myDetail rtData, "evaluateConditions result: $result", -1
	return result
}

private evaluateOperand(Map rtData, Map node, Map operand, index=null, Boolean trigger=false, Boolean nextMidnight=false){
	myDetail rtData, "evaluateOperand $operand", 1
	def values=[]
	//older pistons don't have the 'to' operand (time offset), we're simulating an empty one
	if(!operand)operand=[t:'c']
	String ovt=(String)operand.vt
	switch ((String)operand.t){
	case '': //optional, nothing selected
		values=[[i: "${node?.$}:$index:0", v: [t: ovt, v: null]]]
		break
	case 'p': //physical device
		Map attribute=(String)operand.a ? Attributes()[(String)operand.a]:[:]
		for(String deviceId in expandDeviceList(rtData, (List)operand.d)){
			Map value=[i: "${deviceId}:${(String)operand.a}", v:getDeviceAttribute(rtData, deviceId, (String)operand.a, operand.i, trigger)+(ovt ? [vt: ovt]:[:])+(attribute && attribute.p ? [p: operand.p]:[:])]
			updateCache(rtData, value)
			Boolean a=values.push(value)
		}
		if((Integer)values.size()>1 && !((String)operand.g in ['any', 'all'])){
			//if we have multiple values and a grouping other than any or all we need to apply that function
			try{
				values=[[i: "${node?.$}:$index:0", v:(Map)"func_${(String)operand.g}"(rtData, values*.v)+(ovt ? [vt: ovt]:[:])]]
			} catch(all){
				error "Error applying grouping method ${(String)operand.g}", rtData
			}
		}
		break
	case 'd': //devices
		List deviceIds=[]
		for (String d in expandDeviceList(rtData, (List)operand.d)){
			if(getDevice(rtData, d))Boolean a=deviceIds.push(d)
		}
		values=[[i: "${node?.$}:d", v:[t:'device', v:deviceIds.unique()]]]
		break
	case 'v': //virtual devices
		String rEN=(String)rtData.event.name
		String evntVal="${rtData.event.value}".toString()
		switch ((String)operand.v){
		case 'mode':
		case 'alarmSystemStatus':
			values=[[i: "${node?.$}:v", v:getDeviceAttribute(rtData, (String)rtData.locationId, (String)operand.v)]]
			break
		case 'alarmSystemAlert':
			String valStr=evntVal+(rEN=='hsmAlert' && evntVal=="rule" ? ", ${(String)rtData.event.descriptionText}".toString():'')
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN=='hsmAlert' ? valStr:(String)null)]]]
			break
		case 'alarmSystemEvent':
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN=='hsmSetArm' ? evntVal:(String)null)]]]
			break
		case 'alarmSystemRule':
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN=='hsmRules' ? evntVal:(String)null)]]]
			break
		case 'powerSource':
			values=[[i: "${node?.$}:v", v:[t:'enum', v:rtData.powerSource]]]
			break
		case 'time':
		case 'date':
		case 'datetime':
			values=[[i: "${node?.$}:v", v:[t: (String)operand.v, v: (Long)cast(rtData, now(), (String)operand.v, 'long')]]]
			break
		case 'routine':
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN=='routineExecuted' ? hashId(evntVal):(String)null)]]]
			break
		case 'systemStart':
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN=='systemStart' ? evntVal:(String)null)]]]
			break
		case 'tile':
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN==(String)operand.v ? evntVal:(String)null)]]]
			break
		case 'ifttt':
			values=[[i: "${node?.$}:v", v:[t:'string', v:(rEN==('ifttt.'+evntVal)? evntVal:(String)null)]]]
			break
		case 'email':
			values=[[i: "${node?.$}:v", v:[t:'email', v:(rEN==('email.'+evntVal)? evntVal:(String)null)]]]
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
			case 'sunrise': v=adjustPreset(rtData, "Sunrise", nextMidnight); break
			case 'noon': v=adjustPreset(rtData, "Noon", nextMidnight); break
			case 'sunset': v=adjustPreset(rtData, "Sunset", nextMidnight); break
			}
			if(time)v=(Long)cast(rtData, v, ovt, 'datetime')
			values=[[i: "${node?.$}:$index:0", v:[t:ovt, v:v]]]
			break
		default:
			values=[[i: "${node?.$}:$index:0", v:[t:ovt, v:operand.s]]]
			break
		}
		break
	case 'x': //variable
		if(ovt=='device' && operand.x instanceof List){
			//we could have multiple devices selected
			def sum=[]
			for (String x in operand.x){
				def var=getVariable(rtData, x)
				if(var.v instanceof List){
					sum += var.v
				}else{
					Boolean a=sum.push(var.v)
				}
				values=[[i: "${node?.$}:$index:0", v:[t:'device', v:sum]+(ovt ? [vt:ovt]:[:])]]
			}
		}else{
			values=[[i: "${node?.$}:$index:0", v:getVariable(rtData, (String)operand.x+((String)operand.xi!=(String)null ? '['+(String)operand.xi+']':''))+(ovt ? [vt: ovt]:[:])]]
		}
		break
	case 'c': //constant
		switch (ovt){
		case 'time':
			Long offset=(operand.c instanceof Integer)? operand.c:(Integer)cast(rtData, operand.c, 'integer')
			values=[[i: "${node?.$}:$index:0", v: [t:'time', v:(offset%1440)*60000]]]
			break
		case 'date':
		case 'datetime':
			values=[[i: "${node?.$}:$index:0", v: [t: ovt, v:operand.c]]]
			break
		}
		if((Integer)values.size()!=0)break
	case 'e': //expression
		values=[[i: "${node?.$}:$index:0", v: [:]+evaluateExpression(rtData, (Map)operand.exp)+(ovt ? [vt: ovt]:[:])]]
		break
	case 'u': //expression
		values=[[i: "${node?.$}:$index:0", v: getArgument(rtData, (String)operand.u)]]
		break
	}
	def ret=values
	if(!node){
		if(values.length)ret=values[0].v
		else ret=[t:'dynamic', v:null]
	}
	myDetail rtData, "evaluateOperand $operand result: $ret", -1
	return ret
}

private Long adjustPreset(Map rtData, String ttyp, nextMidnight){
	Long t2=(Long)"get${ttyp}Time"(rtData)
	Long tnow=now()
	if(tnow<t2)return t2
// this deals with both DST skew and sunrise/sunset skews
	Long t0=(Long)"getNext${ttyp}Time"(rtData)
	Long t1=Math.round(t0-86400000.0D)
	Long delta=Math.round((Integer)location.timeZone.getOffset(t1)- (Integer)location.timeZone.getOffset(t0)*1.0D)
	Long t4=Math.round(t1+delta*1.0D)
	if(tnow>t4)return t4
	return t2
}

private Map evaluateScalarOperand(Map rtData, Map node, Map operand, index=null, String dataType='string'){
	def value=evaluateOperand(rtData, null, operand, index)
	return [t: dataType, v: cast(rtData, (value ? value.v:''), dataType)]
}

private Boolean evaluateCondition(Map rtData, Map condition, String collection, Boolean async){
	myDetail rtData, "evaluateCondition $condition", 1
	Long t=now()
	Map msg=timer '', rtData
	//override condition id
	Integer c=(Integer)rtData.stack.c
	Integer conditionNum=condition.$ ? (Integer)condition.$:0
	rtData.stack.c=conditionNum
	Boolean not=false
	Boolean oldResult=!!rtData.cache["c:${conditionNum}".toString()]
	Boolean result=false
	if((String)condition.t=='group'){
		Boolean tt1=evaluateConditions(rtData, condition, collection, async)
		myDetail rtData, "evaluateCondition $condition result: $tt1", -1
		return tt1
	}else{
		not=!!condition.n
		Map comparison=Comparisons().triggers[(String)condition.co]
		Boolean trigger=comparison!=null
		if(!trigger)comparison=Comparisons().conditions[(String)condition.co]
		rtData.wakingUp=(String)rtData.event.name=='time' && rtData.event.schedule!=null && (Integer)rtData.event.schedule.s==conditionNum
		if((Integer)rtData.fastForwardTo!=0 || comparison!=null){
			if((Integer)rtData.fastForwardTo==0 || ((Integer)rtData.fastForwardTo==-9 /*initial run*/)){
				Integer paramCount=comparison.p!=null ? (Integer)comparison.p:0
				Map lo=null
				Map ro=null
				Map ro2=null
				for(Integer i=0; i<=paramCount; i++){
					Map operand=(i==0 ? condition.lo:(i==1 ? condition.ro:condition.ro2))
					//parse the operand
					def values=evaluateOperand(rtData, condition, operand, i, trigger)
					switch (i){
					case 0:
						lo=[operand: operand, values: values]
						break
					case 1:
						ro=[operand: operand, values: values]
						break
					case 2:
						ro2=[operand: operand, values: values]
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
				Map to=(comparison.t!=null || (ro!=null && (String)lo.operand.t=='v' && (String)lo.operand.v=='time' && (String)ro.operand.t!='c')) && condition.to!=null ? [operand: (Map)condition.to, values: evaluateOperand(rtData, null, (Map)condition.to)]:null
				Map to2=ro2!=null && (String)lo.operand.t=='v' && (String)lo.operand.v=='time' && (String)ro2.operand.t!='c' && condition.to2!=null ? [operand: (Map)condition.to2, values: evaluateOperand(rtData, null, (Map)condition.to2)]:null
				result=evaluateComparison(rtData, (String)condition.co, lo, ro, ro2, to, to2, options)
				//save new values to cache
				if(lo)for (Map value in lo.values)updateCache(rtData, value)
				if(ro)for (Map value in ro.values)updateCache(rtData, value)
				if(ro2)for (Map value in ro2.values)updateCache(rtData, value)
				if((Integer)rtData.fastForwardTo==0)tracePoint(rtData, "c:${conditionNum}".toString(), Math.round(1.0D*now()-t), result)
				if(lo.operand.dm!=null && options.devices!=null)def m=setVariable(rtData, (String)lo.operand.dm, options.devices.matched!=null ? (List)options.devices.matched:[])
				if(lo.operand.dn!=null && options.devices!=null)def n=setVariable(rtData, (String)lo.operand.dn, options.devices.unmatched!=null ? (List)options.devices.unmatched:[])
				//do the stay logic here
				if(t_and_compt && (Integer)rtData.fastForwardTo==0){
					//timed trigger
					if(to!=null){
						def tvalue=to.operand && to.values ? (Map)to.values+[f: to.operand.f]:null
						if(tvalue!=null){
							Long delay=(Long)evaluateExpression(rtData, [t:'duration', v:tvalue.v, vt:(String)tvalue.vt], 'long').v
							if((String)lo.operand.t=='p' && (String)lo.operand.g=='any' && (Integer)lo.values.size()>1){

								List schedules
								Map t0=getCachedMaps()
								if(t0!=[:])schedules=[]+(List)t0.schedules
								else{ schedules=(Boolean)rtData.pep ? (List)atomicState.schedules:(List)state.schedules }
								for (value in lo.values){
									String dev=(String)value.v?.d
									if(dev in (List)options.devices.matched){
										//schedule one device schedule
										if(!schedules.find{ (Integer)it.s==conditionNum && (String)it.d==dev }){
											//schedule a wake up if there's none, otherwise just move on
											if((Integer)rtData.logging>2)debug "Adding a timed trigger schedule for device $dev for condition ${conditionNum}", rtData
											requestWakeUp(rtData, condition, condition, delay, dev)
										}
									}else{
										//cancel that one device schedule
										if((Integer)rtData.logging>2)debug "Cancelling any timed trigger schedules for device $dev for condition ${conditionNum}", rtData
										cancelStatementSchedules(rtData, conditionNum, dev)
									}
								}
							}else{
								if(result){
								//if we find the comparison true, set a timer if we haven't already

									List schedules
									Map t0=getCachedMaps()
									if(t0!=[:])schedules=[]+(List)t0.schedules
									else{ schedules=(Boolean)rtData.pep ? (List)atomicState.schedules:(List)state.schedules }
									if(!schedules.find{ ((Integer)it.s==conditionNum)}){
										if((Integer)rtData.logging>2)debug "Adding a timed trigger schedule for condition ${conditionNum}", rtData
										requestWakeUp(rtData, condition, condition, delay)
									}
								}else{
									if((Integer)rtData.logging>2)debug "Cancelling any timed trigger schedules for condition ${conditionNum}", rtData
									cancelStatementSchedules(rtData, conditionNum)
								}
							}
						}
					}
					result=false
				}
				result=not ? !result:result
			}else if((String)rtData.event.name=='time' && (Integer)rtData.fastForwardTo==conditionNum){
				rtData.fastForwardTo=0
				rtData.resumed=true
				result=!not
			}else{
				result=oldResult
			}
		}
	}
	rtData.wakingUp=false
	rtData.conditionStateChanged=oldResult!=result
	if((Boolean)rtData.conditionStateChanged){
		//condition change, perform Task Cancellation Policy TCP
		cancelConditionSchedules(rtData, conditionNum)
	}
	rtData.cache["c:${conditionNum}".toString()]=result
	//true/false actions
	if((result || (Integer)rtData.fastForwardTo!=0) && condition.ts!=null && ((List)condition.ts).length!=0)Boolean a=executeStatements(rtData, (List)condition.ts, async)
	if((!result || (Integer)rtData.fastForwardTo!=0) && condition.fs!=null && ((List)condition.fs).length!=0)Boolean a=executeStatements(rtData, (List)condition.fs, async)
	//restore condition id
	rtData.stack.c=c
	if((Integer)rtData.fastForwardTo==0){
		msg.m="Condition #${conditionNum} evaluated $result"
		if((Integer)rtData.logging>2)debug msg, rtData
	}
	if((Integer)rtData.fastForwardTo<=0 && (Boolean)condition.s && (String)condition.t=='condition' && condition.lo!=null && (String)condition.lo.t=='v'){
		switch ((String)condition.lo.v){
		case 'time':
		case 'date':
		case 'datetime':
			scheduleTimeCondition(rtData, condition)
			break
		}
	}
	myDetail rtData, "evaluateCondition $condition result: $result", -1
	return result
}

private void updateCache(Map rtData, Map value){
	def oldValue=rtData.cache[(String)value.i]
	if(!oldValue || ((String)oldValue.t!=(String)value.v.t)|| (oldValue.v!=value.v.v)){
		rtData.newCache[(String)value.i]=(Map)value.v+[s: now()]
	}
}

private Boolean evaluateComparison(Map rtData, String comparison, Map lo, Map ro=null, Map ro2=null, Map to=null, Map to2=null, options=[:]){
	myDetail rtData, "evaluateComparison $comparison", 1
	String fn="comp_"+comparison
	Boolean result=((String)lo.operand.g=='any' ? false:true)
	if(options?.matches){
		options.devices=[matched: [], unmatched: []]
	}
	//if multiple left values, go through each
	Map tvalue=to && to.operand && to.values ? (Map)to.values+[f: to.operand.f]:null
	Map tvalue2=to2 && to2.operand && to2.values ? (Map)to2.values:null
	for(Map value in lo.values){
		Boolean res=false
		if(value && value.v && (!value.v.x || options.forceAll)){
			try{
				//physical support
				//value.p=lo.operand.p
				if(value && ((String)value.v.t=='device'))value.v=evaluateExpression(rtData, (Map)value.v, 'dynamic')
				if(!ro){
					Map msg=timer '', rtData
					if(comparison=='event_occurs' && (String)lo.operand.t=='v' && (String)rtData.event.name==(String)lo.operand.v)res=true
					else res=(Boolean)"$fn"(rtData, value, null, null, tvalue, tvalue2)
					msg.m="Comparison (${value?.v?.t}) ${value?.v?.v} $comparison = $res"
					if((Integer)rtData.logging>2)debug msg, rtData
				}else{
					Boolean rres
					res=((String)ro.operand.g=='any' ? false:true)
					//if multiple right values, go through each
					for (Map rvalue in ro.values){
						if(rvalue && ((String)rvalue.v.t=='device'))rvalue.v=evaluateExpression(rtData, (Map)rvalue.v, 'dynamic')
						if(!ro2){
							Map msg=timer '', rtData
							rres=(Boolean)"$fn"(rtData, value, rvalue, null, tvalue, tvalue2)
							msg.m="Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} = $rres"
							if((Integer)rtData.logging>2)debug msg, rtData
						}else{
							rres=((String)ro2.operand.g=='any' ? false:true)
							//if multiple right2 values, go through each
							for (r2value in ro2.values){
								if(r2value && ((String)r2value.v.t=='device'))r2value.v=evaluateExpression(rtData, (Map)r2value.v, 'dynamic')
								Map msg=timer '', rtData
//myDetail rtData, "$fn $value   $rvalue    $r2value    $tvalue   $tvalue2", 1
								Boolean r2res=(Boolean)"$fn"(rtData, value, rvalue, r2value, tvalue, tvalue2)
//myDetail rtData, "$r2res  ${myObj(value?.v?.v)}    ${myObj(rvalue?.v?.v)}  $fn $value   $rvalue    $r2value    $tvalue   $tvalue2", -1
								msg.m="Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} .. (${r2value?.v?.t}) ${r2value?.v?.v} = $r2res"
								if((Integer)rtData.logging>2)debug msg, rtData
								rres=((String)ro2.operand.g=='any' ? rres || r2res:rres && r2res)
								if(((String)ro2.operand.g=='any' && rres) || ((String)ro2.operand.g!='any' && !rres))break
							}
						}
						res=((String)ro.operand.g=='any' ? res || rres:res && rres)
						if(((String)ro.operand.g=='any'&& res) || ((String)ro.operand.g!='any' && !res))break
					}
				}
			}catch(all){
				error "Error calling comparison $fn:", rtData, -2, all
				res=false
			}

			if(res && ((String)lo.operand.t=='v')){
				switch ((String)lo.operand.v){
				case 'time':
				case 'date':
				case 'datetime':
					Boolean pass=(checkTimeRestrictions(rtData, lo.operand, now(), 5, 1)==0L)
					if((Integer)rtData.logging>2)debug "Time restriction check ${pass ? 'passed':'failed'}", rtData
					if(!pass)res=false
				}
			}
		}
		result=((String)lo.operand.g=='any' ? result||res : result&&res)
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
	myDetail rtData, "evaluateComparison $comparison result: $result", -1
	return result
}

private void cancelStatementSchedules(Map rtData, Integer statementId, String data=(String)null){
	//cancel all schedules that are pending for statement statementId
	Boolean found=false
	for(Map item in (List)rtData.cancelations.statements){
		found=(statementId==(Integer)item.id && (!data || data==(String)item.data))
		if(found)break
	}
	if((Integer)rtData.logging>2)debug "Cancelling statement #${statementId}'s schedules...", rtData
	if(!found)Boolean a=((List)rtData.cancelations.statements).push([id: statementId, data: data])
}

private void cancelConditionSchedules(Map rtData, Integer conditionId){
	//cancel all schedules that are pending for condition conditionId
	if((Integer)rtData.logging>2)debug "Cancelling condition #${conditionId}'s schedules...", rtData
	if(!(conditionId in (List)rtData.cancelations.conditions)){
		Boolean a=((List)rtData.cancelations.conditions).push(conditionId)
	}
}

private static Boolean matchDeviceSubIndex(list, deviceSubIndex){
	if(list==null || !(list instanceof List) || (Integer)list.size()==0)return true
	return list.collect{ "$it".toString()}.indexOf("$deviceSubIndex".toString())>=0
}

private static Boolean matchDeviceInteraction(String option, Boolean isPhysical){
	return !((option=='p' && !isPhysical) || (option=='s' && isPhysical))
}

private List listPreviousStates(device, String attribute, Long threshold, Boolean excludeLast){
	List result=[]
	//if(!(device instanceof DeviceWrapper))return result
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

private static Map valueCacheChanged(Map rtData, Map comparisonValue){
	def oldValue=rtData.cache[(String)comparisonValue.i]
	def newValue=comparisonValue.v
	if(!(oldValue instanceof Map))oldValue=false
	return (oldValue!=null && ((String)oldValue.t!=(String)newValue.t || "${oldValue.v}"!="${newValue.v}")) ? [i: (String)comparisonValue.i, v: oldValue] : null
}

private Boolean valueWas(Map rtData, Map comparisonValue, Map rightValue, Map rightValue2, Map timeValue, String func){
	if(!comparisonValue || !comparisonValue.v || !(String)comparisonValue.v.d || !comparisonValue.v.a || !timeValue || !timeValue.v || !timeValue.vt){
		return false
	}
	def device=getDevice(rtData, (String)comparisonValue.v.d)
	if(device==null)return false
	String attribute=(String)comparisonValue.v.a
	Long threshold=(Long)evaluateExpression(rtData, [t:'duration', v:timeValue.v, vt:(String)timeValue.vt], 'long').v

	List states=listPreviousStates(device, attribute, threshold, rtData.event.device?.id==device.id && (String)rtData.event.name==attribute)
	Boolean result=true
	Long duration=0
	for (stte in states){
		if(!("comp_$func"(rtData, [i: (String)comparisonValue.i, v: [t: (String)comparisonValue.v.t, v: cast(rtData, stte.value, (String)comparisonValue.v.t)]], rightValue, rightValue2, timeValue)))break
		duration += stte.duration
	}
	if(duration==0L)return false
	result=((String)timeValue.f=='l')? duration<threshold:duration>=threshold
	if((Integer)rtData.logging>2)debug "Duration ${duration}ms for ${func.replace('is_', 'was_')} ${timeValue.f=='l' ? '<':'>='} ${threshold}ms threshold = ${result}", rtData
	return result
}

private Boolean valueChanged(Map rtData, Map comparisonValue, Map timeValue){
	if(!comparisonValue || !comparisonValue.v || !(String)comparisonValue.v.d || !comparisonValue.v.a || !timeValue || !timeValue.v || !timeValue.vt){
		return false
	}
	def device=getDevice(rtData, (String)comparisonValue.v.d)
	if(device==null)return false
	String attribute=(String)comparisonValue.v.a
	Long threshold=(Long)evaluateExpression(rtData, [t:'duration', v:timeValue.v, vt:(String)timeValue.vt], 'long').v

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
private Boolean comp_is					(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ((String)evaluateExpression(rtData, (Map)lv.v, 'string').v==(String)evaluateExpression(rtData, (Map)rv.v, 'string').v)|| (lv.v.n && ((String)cast(rtData, lv.v.n, 'string')==(String)cast(rtData, rv.v.v, 'string')))}
private Boolean comp_is_not				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_equal_to			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ String dt=(((String)lv?.v?.t=='decimal')|| ((String)rv?.v?.t=='decimal')? 'decimal':(((String)lv?.v?.t=='integer')|| ((String)rv?.v?.t=='integer')? 'integer':'dynamic')); return evaluateExpression(rtData, (Map)lv.v, dt).v==evaluateExpression(rtData, (Map)rv.v, dt).v }
private Boolean comp_is_not_equal_to			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ String dt=(((String)lv?.v?.t=='decimal')|| ((String)rv?.v?.t=='decimal')? 'decimal':(((String)lv?.v?.t=='integer')|| ((String)rv?.v?.t=='integer')? 'integer':'dynamic')); return evaluateExpression(rtData, (Map)lv.v, dt).v!=evaluateExpression(rtData, (Map)rv.v, dt).v }
private Boolean comp_is_different_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not_equal_to(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_less_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtData, (Map)lv.v, 'decimal').v<(Double)evaluateExpression(rtData, (Map)rv.v, 'decimal').v }
private Boolean comp_is_less_than_or_equal_to		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtData, (Map)lv.v, 'decimal').v<=(Double)evaluateExpression(rtData, (Map)rv.v, 'decimal').v }
private Boolean comp_is_greater_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtData, (Map)lv.v, 'decimal').v>(Double)evaluateExpression(rtData, (Map)rv.v, 'decimal').v }
private Boolean comp_is_greater_than_or_equal_to	(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Double)evaluateExpression(rtData, (Map)lv.v, 'decimal').v>=(Double)evaluateExpression(rtData, (Map)rv.v, 'decimal').v }
private Boolean comp_is_even				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ((Integer)evaluateExpression(rtData, (Map)lv.v, 'integer').v).mod(2)==0 }
private Boolean comp_is_odd				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ((Integer)evaluateExpression(rtData, (Map)lv.v, 'integer').v).mod(2)!=0 }
private Boolean comp_is_true				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (Boolean)evaluateExpression(rtData, (Map)lv.v, 'boolean').v }
private Boolean comp_is_false				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !(Boolean)evaluateExpression(rtData, (Map)lv.v, 'boolean').v }
private Boolean comp_is_inside_of_range			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Double v=(Double)evaluateExpression(rtData, (Map)lv.v, 'decimal').v; Double v1=(Double)evaluateExpression(rtData, (Map)rv.v, 'decimal').v; Double v2=(Double)evaluateExpression(rtData, (Map)rv2.v, 'decimal').v; return (v1<v2) ? (v>=v1 && v<=v2):(v>=v2 && v<=v1)}
private Boolean comp_is_outside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_inside_of_range(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_is_any_of				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ String v=(String)evaluateExpression(rtData, (Map)lv.v, 'string').v; for (String vi in ((String)rv.v.v).tokenize(',')){ if(v==(String)evaluateExpression(rtData, [t: (String)rv.v.t, v: "$vi".toString().trim(), i: rv.v.i, a: rv.v.a, vt: (String)rv.v.vt], 'string').v)return true }; return false}
private Boolean comp_is_not_any_of			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_any_of(rtData, lv, rv, rv2, tv, tv2)}

private Boolean comp_was				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is')}
private Boolean comp_was_not				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_not')}
private Boolean comp_was_equal_to			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_equal_to')}
private Boolean comp_was_not_equal_to			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_not_equal_to')}
private Boolean comp_was_different_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_different_than')}
private Boolean comp_was_less_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_less_than')}
private Boolean comp_was_less_than_or_equal_to		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_less_than_or_equal_to')}
private Boolean comp_was_greater_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_greater_than')}
private Boolean comp_was_greater_than_or_equal_to	(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_greater_than_or_equal_to')}
private Boolean comp_was_even				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_even')}
private Boolean comp_was_odd				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_odd')}
private Boolean comp_was_true				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_true')}
private Boolean comp_was_false				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_false')}
private Boolean comp_was_inside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_inside_of_range')}
private Boolean comp_was_outside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_outside_of_range')}
private Boolean comp_was_any_of				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_any_of')}
private Boolean comp_was_not_any_of			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueWas(rtData, lv, rv, rv2, tv, 'is_not_any_of')}

private Boolean comp_changed				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, tv2=null){ return valueChanged(rtData, lv, tv)}
private Boolean comp_did_not_change			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !valueChanged(rtData, lv, tv)}

private static Boolean comp_is_any			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return true }
private Boolean comp_is_before				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Long offset1=tv ? (Long)evaluateExpression(rtData, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v:0L; return cast(rtData, (Long)evaluateExpression(rtData, (Map)lv.v, 'datetime').v+2000L, (String)lv.v.t)<cast(rtData, (Long)evaluateExpression(rtData, (Map)rv.v, 'datetime').v+offset1, (String)lv.v.t)}
private Boolean comp_is_after				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Long offset1=tv ? (Long)evaluateExpression(rtData, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v:0L; return cast(rtData, (Long)evaluateExpression(rtData, (Map)lv.v, 'datetime').v+2000L, (String)lv.v.t)>=cast(rtData, (Long)evaluateExpression(rtData, (Map)rv.v, 'datetime').v+offset1, (String)lv.v.t)}
private Boolean comp_is_between				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ Long offset1=tv ? (Long)evaluateExpression(rtData, [t:'duration', v:tv.v, vt:(String)tv.vt], 'long').v:0L; Long offset2=tv2 ? (Long)evaluateExpression(rtData, [t:'duration', v:tv2.v, vt:(String)tv2.vt], 'long').v:0; Long v=(Long)cast(rtData, (Long)evaluateExpression(rtData, (Map)lv.v, 'datetime').v+2000L, (String)lv.v.t); Long v1=(Long)cast(rtData, (Long)evaluateExpression(rtData, (Map)rv.v, 'datetime').v+offset1, (String)lv.v.t); Long v2=(Long)cast(rtData, (Long)evaluateExpression(rtData, (Map)rv2.v, 'datetime').v+offset2, (String)lv.v.t); return v1<v2 ? v>=v1 && v<v2 : v<v2 || v>=v1}
private Boolean comp_is_not_between			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_is_between(rtData, lv, rv, rv2, tv, tv2)}

/*triggers*/
private Boolean comp_gets				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return (String)cast(rtData, lv.v.v, 'string')==(String)cast(rtData, rv.v.v, 'string') && matchDeviceSubIndex(lv.v.i, rtData.currentEvent.index)}
private Boolean comp_executes				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_arrives				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return rtData.event.name=='email' && match(rtData.event?.jsonData?.from ?: '', (String)evaluateExpression(rtData, (Map)rv.v, 'string').v) && match(rtData.event?.jsonData?.message ?: '', (String)evaluateExpression(rtData, (Map)rv2.v, 'string').v)}
private static Boolean comp_event_occurs		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return false }
private static Boolean comp_happens_daily_at		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return rtData.wakingUp }
private static Boolean comp_changes			(Map rtData, Map lv, rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueCacheChanged(rtData, lv) && matchDeviceInteraction((String)lv.v.p, (Boolean)rtData.currentEvent.physical)}
private static Boolean comp_changes_to			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueCacheChanged(rtData, lv) && ("${lv.v.v}"=="${rv.v.v}") && matchDeviceInteraction((String)lv.v.p, (Boolean)rtData.currentEvent.physical)}
private static Boolean comp_receives			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return ("${lv.v.v}"=="${rv.v.v}") && matchDeviceInteraction((String)lv.v.p, (Boolean)rtData.currentEvent.physical)}
private static Boolean comp_changes_away_from		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ("${oldValue.v.v}"=="${rv.v.v}") && matchDeviceInteraction((String)lv.v.p, (Boolean)rtData.currentEvent.physical)}
private Boolean comp_drops			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')>(Double)cast(rtData, lv.v.v, 'decimal'))}
private Boolean comp_does_not_drop			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_drops(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_drops_below			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')>=(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')<(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_drops_to_or_below			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')>(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')<=(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_rises				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')<(Double)cast(rtData, lv.v.v, 'decimal'))}
private Boolean comp_does_not_rise			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return !comp_rises(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_rises_above			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')<=(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')>(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_rises_to_or_above			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')<(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')>=(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_remains_below			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')<(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')<(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_remains_below_or_equal_to		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')<=(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')<=(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_remains_above			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')>(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')>(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_remains_above_or_equal_to		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && ((Double)cast(rtData, oldValue.v.v, 'decimal')>=(Double)cast(rtData, rv.v.v, 'decimal')) && ((Double)cast(rtData, lv.v.v, 'decimal')>=(Double)cast(rtData, rv.v.v, 'decimal'))}
private Boolean comp_enters_range			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); if(!oldValue)return false; Double ov=(Double)cast(rtData, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtData, lv.v.v, 'decimal'); Double v1=(Double)cast(rtData, rv.v.v, 'decimal'); Double v2=(Double)cast(rtData, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return ((ov<v1)|| (ov>v2)) && ((v>=v1) && (v<=v2))}
private Boolean comp_exits_range			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); if(!oldValue)return false; Double ov=(Double)cast(rtData, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtData, lv.v.v, 'decimal'); Double v1=(Double)cast(rtData, rv.v.v, 'decimal'); Double v2=(Double)cast(rtData, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return ((ov>=v1) && (ov<=v2)) && ((v<v1)|| (v>v2))}
private Boolean comp_remains_inside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); if(!oldValue)return false; Double ov=(Double)cast(rtData, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtData, lv.v.v, 'decimal'); Double v1=(Double)cast(rtData, rv.v.v, 'decimal'); Double v2=(Double)cast(rtData, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return (ov>=v1) && (ov<=v2) && (v>=v1) && (v<=v2)}
private Boolean comp_remains_outside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); if(!oldValue)return false; Double ov=(Double)cast(rtData, oldValue.v.v, 'decimal'); Double v=(Double)cast(rtData, lv.v.v, 'decimal'); Double v1=(Double)cast(rtData, rv.v.v, 'decimal'); Double v2=(Double)cast(rtData, rv2.v.v, 'decimal'); if(v1>v2){ Double vv=v1; v1=v2; v2=vv }; return ((ov<v1)|| (ov>v2)) && ((v<v1) || (v>v2))}
private Boolean comp_becomes_even			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && (((Integer)cast(rtData, oldValue.v.v, 'integer')).mod(2)!=0) && (((Integer)cast(rtData, lv.v.v, 'integer')).mod(2)==0)}
private Boolean comp_becomes_odd			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && (((Integer)cast(rtData, oldValue.v.v, 'integer')).mod(2)==0) && (((Integer)cast(rtData, lv.v.v, 'integer')).mod(2)!=0)}
private Boolean comp_remains_even			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && (((Integer)cast(rtData, oldValue.v.v, 'integer')).mod(2)==0) && (((Integer)cast(rtData, lv.v.v, 'integer')).mod(2)==0)}
private Boolean comp_remains_odd			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && (((Integer)cast(rtData, oldValue.v.v, 'integer')).mod(2)!=0) && (((Integer)cast(rtData, lv.v.v, 'integer')).mod(2)!=0)}

private Boolean comp_changes_to_any_of			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return valueCacheChanged(rtData, lv) && comp_is_any_of(rtData, lv, rv, rv2, tv, tv2) && matchDeviceInteraction((String)lv.v.p, (Boolean)rtData.currentEvent.physical)}
private Boolean comp_changes_away_from_any_of		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ def oldValue=valueCacheChanged(rtData, lv); return oldValue && comp_is_any_of(rtData, oldValue, rv, rv2) && matchDeviceInteraction((String)lv.v.p, (Boolean)rtData.currentEvent.physical)}

private Boolean comp_stays				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is(rtData, lv, rv, rv2, tv, tv2)}
private static Boolean comp_stays_unchanged		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return true }
private Boolean comp_stays_not				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_equal_to			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_equal_to(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_different_than		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_different_than(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_less_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_less_than(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_less_than_or_equal_to	(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_less_than_or_equal_to(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_greater_than			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_greater_than(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_greater_than_or_equal_to	(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_greater_than_or_equal_to(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_even				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_even(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_odd				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_odd(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_true				(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_true(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_false			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_false(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_inside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_inside_of_range(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_outside_of_range		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_outside_of_range(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_any_of			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_any_of(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_away_from			(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not_equal_to(rtData, lv, rv, rv2, tv, tv2)}
private Boolean comp_stays_away_from_any_of		(Map rtData, Map lv, Map rv=null, Map rv2=null, Map tv=null, Map tv2=null){ return comp_is_not_any_of(rtData, lv, rv, rv2, tv, tv2)}

private void traverseStatements(node, closure, parentNode=null, Map data=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(def item in node){
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
		traverseStatements(node.s, closure, node, data)
	}
	if(node.e instanceof List){
		traverseStatements(node.e, closure, node, data)
	}
}

private void traverseEvents(node, closure, parentNode=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(item in node){
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
		for(item in node){
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
		traverseConditions(node.c, closure, node)
	}
}

private void traverseRestrictions(node, closure, parentNode=null){
	if(!node)return
	//if a statements element, go through each item
	if(node instanceof List){
		for(item in node){
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
		for(item in node){
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
		traverseExpressions(node.i, closure, param, node)
	}
}

private void updateDeviceList(Map rtData, List deviceIdList, Boolean clearCache=true){
	app.updateSetting('dev', [type: /*isHubitat()?*/ 'capability'/*:'capability.device'*/, value: deviceIdList.unique()])// settings update do not happen till next execution
	if(clearCache)clearMyCache("updateDeviceList")
}

private void subscribeAll(Map rtData, Boolean doit=true){
	if(eric())log.debug "subscribeAll $doit"
	try{
	if(!rtData){ log.error "no rtData subscribeAll"; return }
	Map ss=[
		events: 0,
		controls: 0,
		devices: 0,
	]
	Map statementData=[timer:false]
	Map msg=timer "Finished subscribing", rtData, -1
	if(doit){
		unsubscribe()
		rtData.devices=[:]
		app.clearSetting('dev')
		if((Integer)rtData.logging>1)trace "Subscribing to devices...", rtData, 1
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
		String subscriptionId=(String)null
		String deviceId=(String)null
		String attribute=(String)null
		String exprID=(String)expression.id
		if((String)expression.t=='device' && exprID){
			if(exprId==(String)rtData.oldLocationId)exprId=(String)rtData.locationId
			devices[exprID]=[c: (comparisonType ? 1:0)+(devices[exprID]?.c ? (Integer)devices[exprID].c:0)]
			deviceId=exprID
			attribute=(String)expression.a
			subscriptionId="${deviceId}${attribute}".toString()
		}
		String exprX=(String)expression.x
		if((String)expression.t=='variable' && exprX && exprX.startsWith('@')){
			subscriptionId="${exprX}".toString()
			deviceId=(String)rtData.locationId
			attribute="${(String)rtData.instanceId}.${exprX}".toString()
		}
		if(subscriptionId && deviceId){
			String ct=(String)subscriptions[subscriptionId]?.t ?: (String)null
			if((ct=='trigger')|| (comparisonType=='trigger')){
				ct='trigger'
			}else{
				ct=ct ?: comparisonType
			}
			//myDetail rtData, "subscribeAll condition is $condition"
			subscriptions[subscriptionId]=[d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? (List)subscriptions[subscriptionId].c:[])+[condition]]
			if(deviceId!=(String)rtData.locationId && deviceId.startsWith(':')){
				rawDevices[deviceId]=getDevice(rtData, deviceId)
				devices[deviceId]=[c: (comparisonType ? 1:0)+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
			}
		}
	}
	operandTraverser={ Map node, Map operand, value, String comparisonType ->
		if(!operand)return
		switch ((String)operand.t){
		case 'p': //physical device
			for(String deviceId in expandDeviceList(rtData, (List)operand.d, true)){
				if(deviceId==(String)rtData.oldLocationId)deviceId=(String)rtData.locationId
				devices[deviceId]=[c: (comparisonType ? 1:0)+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
				String attribute="${operand.a}".toString()
				String subscriptionId="$deviceId${attribute}".toString()
				//if we have any trigger, it takes precedence over anything else
				String ct=(String)subscriptions[subscriptionId]?.t ?: (String)null
				String oct=ct
				String msgVal
				if((ct=='trigger')|| (comparisonType=='trigger')){
					ct='trigger'

					String attrVal
					if((node.co=='receives' || node.co=='gets') && value && (String)value?.t=='c' && value.c){
						attrVal=attribute+".${(String)value.c}".toString()
						msgVal='Attempting Attribute value'
					}
					String aval=(String)subscriptions[subscriptionId]?.a
					if(aval && subscriptions[subscriptionId]?.d && (oct=='trigger' && aval!=attrVal)){
						msgVal='Using Attribute'
						attrVal=''
					}
					if(attrVal){
						attribute=attrVal
					}
					if(doit && msgVal!=(String)null && (Integer)rtData.logging>2)debug msgVal+' subscription', rtData

				}else{
					ct=ct ?: comparisonType
				}
				subscriptions[subscriptionId]=[d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? (List)subscriptions[subscriptionId].c:[])+(comparisonType?[node]:[])]
				if(deviceId!=(String)rtData.locationId && deviceId.startsWith(':')){
					rawDevices[deviceId]=getDevice(rtData, deviceId)
				}
			}
			break
		case 'v': //virtual device
			String deviceId=(String)rtData.locationId
			//if we have any trigger, it takes precedence over anything else
			devices[deviceId]=[c: (comparisonType ? 1:0)+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
			String subscriptionId
			String attribute
			String operV=(String)operand.v
			switch (operV){
			case 'alarmSystemStatus':
				subscriptionId="$deviceId${operV}".toString()
				attribute="hsmStatus"
				break
			case 'alarmSystemAlert':
				subscriptionId="$deviceId${operV}".toString()
				attribute="hsmAlerts"
				break
			case 'alarmSystemEvent':
				subscriptionId="$deviceId${operV}".toString()
				attribute="hsmSetArm"
				break
			case 'alarmSystemRule':
				subscriptionId="$deviceId${operV}".toString()
				attribute="hsmRules"
				break
			case 'time':
			case 'date':
			case 'datetime':
			case 'mode':
			case 'powerSource':
			case 'systemStart':
				subscriptionId="$deviceId${operV}".toString()
				attribute=operV
				break
			case 'email':
				subscriptionId="$deviceId${operV}${(String)rtData.id}".toString()
				attribute="email.${(String)rtData.id}".toString()// receive email does not work
				break
			case 'ifttt':
				if(value && (String)value.t=='c' && value.c){
					def options=VirtualDevices()[operV]?.o
					def item=options ? options[value.c]:value.c
					if(item){
						subscriptionId="$deviceId${operV}${item}".toString()
						String attrVal=".${item}".toString()
						attribute="${operV}${attrVal}".toString()
					}
				}
				break
			}
			if(subscriptionId){
				String ct=(String)subscriptions[subscriptionId]?.t ?: (String)null
				if((ct=='trigger')|| (comparisonType=='trigger')){
					ct='trigger'
				}else{
					ct=ct ?: comparisonType
				}
				subscriptions[subscriptionId]=[d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? (List)subscriptions[subscriptionId].c:[])+(comparisonType?[node]:[])]
				break
			}
			break
		case 'x':
			String operX=(String)operand.x
			if(operX && operX.startsWith('@')){
				String subscriptionId=operX
				String attribute="${(String)rtData.instanceId}.${operX}".toString()
				String ct=(String)subscriptions[subscriptionId]?.t ?: (String)null
				if((ct=='trigger')|| (comparisonType=='trigger')){
					ct='trigger'
				}else{
					ct=ct ?: comparisonType
				}
				subscriptions[subscriptionId]=[d: (String)rtData.locationId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? (List)subscriptions[subscriptionId].c:[])+(comparisonType?[node]:[])]
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
			operandTraverser(event, event.lo, null, comparisonType)
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
					def operand=(i==0 ? condition.lo:(i==1 ? condition.ro:condition.ro2))
					operandTraverser(condition, operand, condition.ro, comparisonType)
				}
			}
		}
		if(condition.ts instanceof List)traverseStatements(condition.ts, statementTraverser, condition, statementData)
		if(condition.fs instanceof List)traverseStatements(condition.fs, statementTraverser, condition, statementData)
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
					def operand=(i==0 ? restriction.lo:(i==1 ? restriction.ro:restriction.ro2))
					operandTraverser(restriction, operand, null, (String)null)
				}
			}
		}
	}
	statementTraverser={ Map node, parentNode, Map data ->
		downgradeTriggers=data!=null && (Boolean)data.timer
		if(node.r)traverseRestrictions(node.r, restrictionTraverser)
		for(String deviceId in node.d){
			if(deviceId==(String)rtData.oldLocationId)deviceId=(String)rtData.locationId
			devices[deviceId]=devices[deviceId] ?: [c: 0]
			if(deviceId!=(String)rtData.locationId && deviceId.startsWith(':')){
				rawDevices[deviceId]=getDevice(rtData, deviceId)
			}
		}
		switch((String)node.t){
		case 'action':
			if(node.k){
				for (k in (List)node.k){
					traverseStatements(k.p?:[], statementTraverser, k, data)
				}
			}
			break
		case 'if':
			if(node.ei){
				for (ei in node.ei){
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
			operandTraverser(node, node.lo, null, 'condition')
			for (c in node.cs){
				operandTraverser(c, c.ro, null, (String)null)
				//if case is a range, traverse the second operand too
				if((String)c.t=='r')operandTraverser(c, c.ro2, null, (String)null)
				if(c.s instanceof List){
					traverseStatements(c.s, statementTraverser, node, data)
				}
			}
			break
		case 'every':
			hasTriggers=true
			break
		}
	}
	if(rtData.piston.r)traverseRestrictions(rtData.piston.r, restrictionTraverser)
	if(rtData.piston.s)traverseStatements(rtData.piston.s, statementTraverser, null, statementData)
	//device variables
	for(variable in rtData.piston.v.findAll{ (String)it.t=='device' && it.v && it.v.d && it.v.d instanceof List}){
		for (String deviceId in variable.v.d){
			if(deviceId==(String)rtData.oldLocationId)deviceId=(String)rtData.locationId
			devices[deviceId]=[c: 0+(devices[deviceId]?.c ? (Integer)devices[deviceId].c:0)]
			if(deviceId!=(String)rtData.locationId){
				rawDevices[deviceId]=getDevice(rtData, deviceId)
			}
		}
	}
	Map dds=[:]
//log.debug "subscribeAll subscriptions ${subscriptions}"
	for (subscription in subscriptions){
		String altSub='never'
		for (condition in (List)subscription.value.c)if(condition){
			condition.s=false
			String tt0=(String)condition.sm
			altSub= tt0=='always' ? tt0 : (altSub!='always' && tt0!='never' ? tt0 : altSub)
		}
		// check for disabled event subscriptions
		if(!rtData.piston.o?.des && (String)subscription.value.t && !!subscription.value.c && altSub!="never" && ((String)subscription.value.t=="trigger" || altSub=="always" || !hasTriggers)){
			def device=((String)subscription.value.d).startsWith(':')? getDevice(rtData, (String)subscription.value.d):null
			String t0=(String)subscription.value.a
			String a=(t0=='orientation')|| (t0=='axisX')|| (t0=='axisY')|| (t0=='axisZ')? 'threeAxis':(String)subscription.value.a
			if(device!=null){
				for (condition in (List)subscription.value.c)if(condition){
					String t1=(String)condition.sm
					condition.s= t1!='never' && ((String)condition.ct=='t' || t1=='always' || !hasTriggers)
				}
				switch (t0){
				case 'time':
				case 'date':
				case 'datetime':
					break
				default:
					if(doit){
						if((Integer)rtData.logging>0)info "Subscribing to $device.${a}...", rtData
						subscribe(device, a, deviceHandler)
					}
					ss.events=(Integer)ss.events+1
					if(!dds[device.id]){
						ss.devices=(Integer)ss.devices+1
						dds[device.id]=1
					}
				}
			}else{
				error "Failed subscribing to $device.${a}, device not found", rtData
			}
		}else{
			for (condition in (List)subscription.value.c)if(condition){ condition.s=false }
			String tt0=(String)subscription.value.d
			if(devices[tt0]){
				devices[tt0].c=(Integer)devices[tt0].c-1
			}
		}
	}
	//not using fake subscriptions for controlled devices - piston has device in settings
	for (d in devices.findAll{ ((Integer)it.value.c<=0 || rtData.piston.o?.des) && (String)it.key!=(String)rtData.locationId }){
		def device=((String)d.key).startsWith(':')? getDevice(rtData, (String)d.key):null
		if(device!=null && (device!=location)){
			if((Integer)rtData.logging>1 && doit)trace "Piston controls $device...", rtData
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
		deviceIdList.removeAll{ it==null }
		updateDeviceList(rtData, deviceIdList, false)

		state.subscriptions=ss
		if((Integer)rtData.logging>1)trace msg, rtData

		//subscribe(app, appHandler)
		subscribe(location, (String)rtData.id, executeHandler)
		Map event=[date:new Date(), device:location, name:'time', value:now(), schedule:[t:0, s:0, i:-9]]
		Boolean a=executeEvent(rtData, event)
		processSchedules rtData, true
	//save cache collected through dummy run
		for(item in rtData.newCache)rtData.cache[(String)item.key]=item.value

		Map t0=getCachedMaps()
		String myId=(String)rtData.id
		if(t0!=[:])theCacheFLD[myId].cache=[:]+(Map)rtData.cache
		state.cache=rtData.cache
	}

	} catch (all){
		error "An error has occurred while subscribing: ", rtData, -2, all
	}
}

private List expandDeviceList(Map rtData, List devices, Boolean localVarsOnly=false){
	localVarsOnly=false	//temporary allowing global vars
	List result=[]
	for(String deviceId in devices){
		if(deviceId && (Integer)deviceId.size()==34 && deviceId.startsWith(':') && deviceId.endsWith(':')){
			Boolean a=result.push(deviceId)
		}else{
			if(localVarsOnly){
				//during subscriptions we use local vars only to make sure we don't subscribe to "variable" lists of devices
				Map var=rtData.localVars[deviceId]
				if(var && (String)var.t=='device' && var.v instanceof Map && (String)var.v.t=='d' && var.v.d instanceof List && (Integer)((List)var.v.d).size()!=0)result += (List)var.v.d
			}else{
				Map var=getVariable(rtData, deviceId)
				if((String)var.t=='device' && var.v instanceof List && (Integer)((List)var.v).size()!=0)result += (List)var.v
				if((String)var.t!='device'){
					def device=getDevice(rtData, (String)cast(rtData, var.v, 'string'))
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

private getDevice(Map rtData, String idOrName){
	if((String)rtData.locationId==idOrName || (String)rtData.oldLocationId==idOrName)return location
	def t0=rtData.devices[idOrName]
	def device=t0!=null ? t0:rtData.devices.find{ (String)it.value.getDisplayName()==idOrName }?.value
	if(device==null){
		if(rtData.allDevices==null){
			Map msg=timer "Device missing from piston. Loading all from parent...", rtData
			rtData.allDevices=(Map)parent.listAvailableDevices(true)
			if((Integer)rtData.logging>2)debug msg, rtData
		}
		if(rtData.allDevices!=null){
			def deviceMap=rtData.allDevices.find{ (idOrName==(String)it.key)|| (idOrName==(String)it.value.getDisplayName())}
			if(deviceMap!=null){
				device=deviceMap.value
				rtData.updateDevices=true
				rtData.devices[(String)deviceMap.key]=device
			}
		}else{
			error "Device ${idOrName} was not found. Please review your piston.", rtData
		}
	}
	return device
}

private getDeviceAttributeValue(Map rtData, device, String attributeName){
	String rtDEvN=rtData.event!=null ? (String)rtData.event.name:''
	Boolean rtDEdID=rtData.event!=null ? rtData.event.device?.id==device.id:false
	if(rtDEvN==attributeName && rtDEdID){
		return rtData.event.value
	}else{
		switch (attributeName){
		case '$status':
			return device.getStatus()
		case 'orientation':
			return getThreeAxisOrientation(rtData.event && rtDEvN=='threeAxis' && rtDEdID ? rtData.event.xyzValue:device.currentValue('threeAxis', true))
		case 'axisX':
			return rtData.event!=null && rtDEvN=='threeAxis' && rtDEdID ? rtData.event.xyzValue.x:device.currentValue('threeAxis', true).x
		case 'axisY':
			return rtData.event!=null && rtDEvN=='threeAxis' && rtDEdID ? rtData.event.xyzValue.y:device.currentValue('threeAxis', true).y
		case 'axisZ':
			return rtData.event!=null && rtDEvN=='threeAxis' && rtDEdID ? rtData.event.xyzValue.z:device.currentValue('threeAxis', true).z
		}
		def result
		try{
			result=device.currentValue(attributeName, true)
		} catch (all){
			error "Error reading current value for $device.$attributeName:", rtData, -2, all
		}
		return result!=null ? result:''
	}
}

private Map getDeviceAttribute(Map rtData, String deviceId, String attributeName, subDeviceIndex=null, Boolean trigger=false){
	if(deviceId==(String)rtData.locationId || deviceId==(String)rtData.oldLocationId){ //backwards compatibility
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
	def device=getDevice(rtData, deviceId)
	if(device!=null){
		Map attribute=attributeName!=null ? Attributes()[attributeName]:null
		if(attribute==null){
			attribute=[t:'string', /* m:false */ ]
		}
		//x=eXclude - if a momentary attribute is looked for and the device does not match the current device, then we must ignore this during comparisons
		def t0=(attributeName!=null ? getDeviceAttributeValue(rtData, device, attributeName):null)
		String tt1=(String)attribute.t
		Boolean match=t0!=null && ((t0 instanceof String && (tt1 in ['string', 'enum']))||
				(t0 instanceof Integer && (tt1=='integer')))
//	String tt2=myObj(t0)
//if(attributeName)log.warn "attributeName $attributeName t0   $t0 of $tt2   tt1 $tt1    match $match }"
		def value=(attributeName!=null ? (match ? t0:cast(rtData, t0, tt1)):"$device")
		if(attributeName=='hue'){
			value=cast(rtData, (Double)cast(rtData, value, 'decimal')*3.6D, (String)attribute.t)
		}
		//have to compare ids and type for hubitat since the locationid can be the same as the deviceid
		def tt0=rtData.event?.device!=null ? rtData.event.device:location
		Boolean deviceMatch=device?.id==tt0.id && isDeviceLocation(device)==isDeviceLocation(tt0)
		return [t: (String)attribute.t, v: value, d: deviceId, a: attributeName, i: subDeviceIndex, x: (attribute.m!=null || trigger) && (!deviceMatch || (( attributeName=='orientation' || attributeName=='axisX' || attributeName=='axisY' || attributeName=='axisZ' ? 'threeAxis':attributeName)!=(String)rtData.event.name))]
	}
	return [t:'error', v:"Device '${deviceId}' not found"]
}

private Map getJsonData(Map rtData, data, String name, String feature=(String)null){
	if(data!=null){
	try{
		List parts=name.replace('][', '].[').tokenize('.')
		def args=(data instanceof Map ? [:]+data:(data instanceof List ? []+data:new groovy.json.JsonSlurper().parseText(data)))
		Integer partIndex=-1
		for(String part in parts){
			partIndex=partIndex+1
			if((args instanceof String)|| (args instanceof GString)){
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
						Map var=getVariable(rtData, "$idx")
						idx=(String)var.t!='error' ? var.v:idx
					}
				}
				if(!overrideArgs && !!part)args=args[part]
				if(args instanceof List)idx=cast(rtData, idx, 'integer')
				args=args[idx]
				continue
			}
			if(!overrideArgs)args=args[part]
		}
		return [t:'dynamic', v:"$args".toString()]
	} catch (all){
		error "Error retrieving JSON data part $part", rtData, -2, all
		return [t:'dynamic', v:'']
	}
	}
	return [t:'dynamic', v:'']
}

private Map getArgument(Map rtData, String name){
	return getJsonData(rtData, rtData.args, name)
}

private Map getJson(Map rtData, String name){
	return getJsonData(rtData, rtData.json, name)
}

private Map getPlaces(Map rtData, String name){
	return getJsonData(rtData, rtData.settings?.places, name)
}

private Map getResponse(Map rtData, String name){
	return getJsonData(rtData, rtData.response, name)
}

private Map getWeather(Map rtData, String name){
	if(rtData.weather==null){
		Map t0=parent.getWData()
		rtData.weather=t0!=null ? t0:[:]
	}
	return getJsonData(rtData, rtData.weather, name)
}

private Map getNFLDataFeature(String dataFeature){
	Map requestParams=[
		uri: "https://api.webcore.co/nfl/$dataFeature",
		query: method=="GET" ? data:null
	]
	httpGet(requestParams){ response ->
		if(response.status==200 && response.data && !binary){
			try{
				return response.data instanceof Map ? response.data:(LinkedHashMap)new groovy.json.JsonSlurper().parseText(response.data)
			} catch (all){
				return null
			}
		}
		return null
	}
}

private Map getNFL(Map rtData, String name){
	List parts=name.tokenize('.')
	rtData.nfl=rtData.nfl ?: [:]
	if((Integer)parts.size()>0){
		String dataFeature=(String)(((String)parts[0]).tokenize('[')[0])
		if(rtData.nfl[dataFeature]==null){
			rtData.nfl[dataFeature]=getNFLDataFeature(dataFeature)
		}
	}
	return getJsonData(rtData, rtData.nfl, name, 'NFL')
}

private Map getIncidents(rtData, String name){
	return getJsonData(rtData, rtData.incidents, name)
}

/*
private void initIncidents(rtData){
	if(rtData.incidents instanceof List)return
	def t0=parent.getIncidents()
	rtData.incidents=t0!=null ? t0:[]
}
*/

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

private Map getVariable(Map rtData, String name){
	Map var=parseVariableName(name)
	name=sanitizeVariableName((String)var.name)
	if(name==null)return [t:'error', v:'Invalid empty variable name']
	Map result
	String tname=name
	if(tname.startsWith('@')){
		loadGlobalCache()
		def tresult=globalVarsFLD[tname]
		if(!(tresult instanceof Map))result=[t:'error', v:"Variable '$tname' not found"]
		else result=(Map)tresult
		result.v=cast(rtData, result.v, (String)result.t)
	}else{
		if(tname.startsWith('$')){
			Integer t0=(Integer)tname.size()
			if(tname.startsWith('$args.') && (t0>6)){
				result=getArgument(rtData, tname.substring(6))
			}else if(tname.startsWith('$args[') && (t0>6)){
				result=getArgument(rtData, tname.substring(5))
			}else if(tname.startsWith('$json.') && (t0>6)){
				result=getJson(rtData, tname.substring(6))
			}else if(tname.startsWith('$json[') && (t0>6)){
				result=getJson(rtData, tname.substring(5))
			}else if(tname.startsWith('$places.') && (t0>8)){
				result=getPlaces(rtData, tname.substring(8))
			}else if(tname.startsWith('$places[') && (t0>8)){
				result=getPlaces(rtData, tname.substring(7))
			}else if(tname.startsWith('$response.') && (t0>10)){
				result=getResponse(rtData, tname.substring(10))
			}else if(tname.startsWith('$response[') && (t0>10)){
				result=getResponse(rtData, tname.substring(9))
			}else if(tname.startsWith('$nfl.') && (t0>5)){
				result=getNFL(rtData, tname.substring(5))
			}else if(tname.startsWith('$weather.') && (t0>9)){
				result=getWeather(rtData, tname.substring(9))
			}else if(tname.startsWith('$incidents.') && (t0>11)){
				result=getIncidents(rtData, tname.substring(11))
			}else if(tname.startsWith('$incidents[') && (t0>11)){
				result=getIncidents(rtData, tname.substring(10))
			}else{
				def tresult=rtData.systemVars[tname]
				if(!(tresult instanceof Map))result=[t:'error', v:"Variable '$tname' not found"]
				else result=(Map)tresult
				if(result!=null && result.d){
					result=[t: (String)result.t, v: getSystemVariableValue(rtData, tname)]
				}
			}
		}else{
			def tlocalVar=rtData.localVars[tname]
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
			Map indirectVar=getVariable(rtData, (String)var.index)
			//indirect variable addressing
			if((String)indirectVar.t!='error'){
				def value=(String)indirectVar.t=='decimal' ? (Integer)cast(rtData, indirectVar.v, 'integer', (String)indirectVar.t):indirectVar.v
				String dataType=(String)indirectVar.t=='decimal' ? 'integer':(String)indirectVar.t
				var.index=(String)cast(rtData, value, 'string', dataType)
			}
			result.v=result.v[(String)var.index]
		}
	}else{
		if(result.v instanceof Map){
			String tt0=(String)result.t
			result=evaluateOperand(rtData, null, (Map)result.v)
			result=(tt0!=null && tt0==(String)result.t)? result:evaluateExpression(rtData, result, tt0)
		}
	}
	return [t:(String)result.t, v:result.v]
}

private Map setVariable(Map rtData, String name, value){
	Map var=parseVariableName(name)
	name=sanitizeVariableName((String)var.name)
	if(name==null)return [t:'error', v:'Invalid empty variable name']
	String tname=name
	if(tname.startsWith('@')){
		loadGlobalCache()
		def tvariable=globalVarsFLD[tname]
		if(tvariable instanceof Map){
			Map variable=(Map)globalVarsFLD[tname]
			variable.v=cast(rtData, value, (String)variable.t)
			Map cache=rtData.gvCache!=null ? (Map)rtData.gvCache:[:]
			cache[tname]=variable
			rtData.gvCache=cache
			return variable
		}
	}else{
// global vars are removed by setting them to null via webcore dashboard
// local vars are removed by 'clear all data' via HE console
		def tvariable=rtData.localVars[tname]
		if(tvariable instanceof Map){
			Map variable=(Map)rtData.localVars[tname]
			if(((String)variable.t).endsWith(']')){
				//we're dealing with a list
				variable.v=(variable.v instanceof Map)? variable.v:[:]
				if((String)var.index=='*CLEAR'){
					variable.v.clear()
				}else{
					Map indirectVar=getVariable(rtData, (String)var.index)
					//indirect variable addressing
					if((String)indirectVar.t!='error'){
						var.index=(String)cast(rtData, indirectVar.v, 'string', (String)indirectVar.t)
					}
					variable.v[(String)var.index]=cast(rtData, value, ((String)variable.t).replace('[]', ''))
				}
			}else{
				def v=(value instanceof GString)? "$value".toString():value
				Boolean match=v!=null && ((v instanceof String && t=='string')||
							(v instanceof Long && t=='long')||
							(v instanceof Integer && t=='integer')||
							(v instanceof Double && t=='decimal'))
				variable.v=match ? v:cast(rtData, v, (String)variable.t)
			}
			if(!variable.f){
				Map vars
				Map t0=getCachedMaps()
				if(t0!=[:])vars=(Map)t0.vars
				else{ vars=(Boolean)rtData.pep ? (Map)atomicState.vars:(Map)state.vars }

				vars[tname]=variable.v
				String myId=(String)rtData.id
				if(t0!=[:])theCacheFLD[myId].vars=vars
				if((Boolean)rtData.pep)atomicState.vars=vars
				else state.vars=vars
			}
			return variable
		}
	}
	result=[t:'error', v:'Invalid variable']
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

/*** EXPRESSION FUNCTIONS							***/

public Map proxyEvaluateExpression(Map rtData, Map expression, String dataType=(String)null){
	rtData=getRunTimeData(rtData)
	resetRandomValues(rtData)
	try{
		Map result=evaluateExpression(rtData, expression, dataType)
		if((String)result.t=='device' && result.a!=null){
			Map attr=Attributes()[(String)result.a]
			result=evaluateExpression(rtData, result, attr!=null && attr.t!=null ? (String)attr.t:'string')
		}
		return result
	} catch (all){
		error 'An error occurred while executing the expression', rtData, -2, all
	}
	return [t:'error', v:'expression error']
}

private static Map simplifyExpression(Map expression){
	while ((String)expression.t=='expression' && expression.i && (Integer)((List)expression.i).size()==1) expression=((List)expression.i)[0]
	return expression
}

private Map evaluateExpression(Map rtData, Map expression, String dataType=(String)null){
	//if dealing with an expression that has multiple items, let's evaluate each item one by one
	//let's evaluate this expression
	if(!expression)return [t:'error', v:'Null expression']
	//not sure what it was needed for - need to comment more
	//if(expression && expression.v instanceof Map)return evaluateExpression(rtData, expression.v, expression.t)
	Long time=now()
	expression=simplifyExpression(expression)
	myDetail rtData, "evaluateExpression $expression   dataType: $dataType", 1
	Map result=expression
	String exprType=(String)expression.t
	switch (exprType){
	case 'integer':
	case 'long':
	case 'decimal':
		result=[t: exprType, v: expression.v]
		break
	case 'time':
		def t0=expression.v
		Boolean found=false
		if("$t0".isNumber() && (t0<86400000))found=true
		result=[t: exprType, v: found ? t0.toLong():(Long)cast(rtData, t0, exprType, dataType)]
		break
	case 'datetime':
		def t0=expression.v
		if("$t0".isNumber() && (t0>=86400000)){
			result=[t: exprType, v: t0.toLong() ]
			break
		}
	case 'int32':
	case 'int64':
	case 'date':
		result=[t: exprType, v: cast(rtData, expression.v, exprType, dataType)]
		break
	case 'bool':
	case 'boolean':
		def t0=expression.v
		if(t0 instanceof Boolean){
			result=[t:'boolean', v:(Boolean)t0]
			break
		}
		result=[t:'boolean', v:(Boolean)cast(rtData, t0, 'boolean', dataType)]
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
		result=[t:'string', v:(String)cast(rtData, t0, 'string', dataType)]
		break
	case 'number':
	case 'float':
	case 'double':
		def t0=expression.v
		if(t0 instanceof Double){
			result=[t:'decimal', v:(Double)t0]
			break
		}
		result=[t:'decimal', v:(Double)cast(rtData, expression.v, 'decimal', dataType)]
		break
	case 'duration':
		String t0=(String)expression.vt
		if(t0==null && expression.v instanceof Long){ result=[t:'long', v:(Long)expression.v ] }
		else result=[t:'long', v:(Long)cast(rtData, expression.v, t0!=(String)null ? t0:'long')]
		break
	case 'variable':
		//get variable as{n: name, t: type, v: value}
		//result=[t:'error', v:'Invalid variable']
		result=getVariable(rtData, (String)expression.x+((String)expression.xi!=(String)null ? '['+(String)expression.xi+']':''))
		break
	case 'device':
		//get variable as{n: name, t: type, v: value}
		if(expression.v instanceof List){
			//already parsed
			result=expression
		}else{
			List deviceIds=(expression.id instanceof List)? (List)expression.id:(expression.id ? [expression.id]:[])
			if((Integer)deviceIds.size()==0){
				Map var=getVariable(rtData, (String)expression.x)
				if((String)var.t=='device'){
					deviceIds=(List)var.v
				}else{
					def device=getDevice(rtData, (String)var.v)
					if(device!=null)deviceIds=[hashId(device.id)]
				}
			}
			result=[t:'device', v:deviceIds, a:(String)expression.a]
		}
		break
	case 'operand':
		result=[t:'string', v:(String)cast(rtData, expression.v, 'string')]
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
						param=evaluateExpression(rtData, param)
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
			myDetail rtData, myStr, 1
			result=(Map)"$fn"(rtData, params)
		} catch (all){
			error "Error executing $fn: ", rtData, -2, all
			//log error
			result=[t:'error', v:all]
		}
		myDetail rtData, myStr+' '+"${result}".toString(), -1
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
				Boolean a=items.push(evaluateExpression(rtData, item)+[:])
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
				if((Boolean)evaluateExpression(rtData, (Map)items[0], 'boolean').v){
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
				v=(o=='+')? v1+v2:v1-v2
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
				//warn "Precalc ($t1) $v1 $o ($t2) $v2 >>> t1d=$t1d, t2d=$t2d, t1n=$t1n, t2n=$t2n", rtData
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
				v1=evaluateExpression(rtData, (Map)items[idx], t1).v
				v2=evaluateExpression(rtData, (Map)items[idx+1], t2).v
				v1=v1=='null' ? null:v1
				v2=v2=='null' ? null:v2
				switch (o){
					case '?':
					case ':':
						error "Invalid ternary operator. Ternary operator's syntax is (condition ? trueValue:falseValue ). Please check your syntax and try again.", rtData
						v=''
						break
					case '-':
						v=v1 - v2
						break
					case '*':
						v=v1 * v2
						break
					case '/':
						v=(v2!=0 ? v1 / v2:0)
						break
					case '\\':
						v=(Integer)Math.floor(v2!=0 ? v1 / v2:0)
						break
					case '%':
						v=(Integer)(v2!=0 ? v1%v2:0)
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

				if((Integer)rtData.logging>2)debug "Calculating ($t1)$v1 $o ($t2)$v2 >> ($t)$v", rtData

				//set the results
				items[idx+1].t=t
				v=(v instanceof GString)? "$v".toString():v
				Boolean match=v!=null && ((v instanceof String && t=='string')||
							(v instanceof Long && t=='long')||
							(v instanceof Integer && t=='integer')||
							(v instanceof Double && t=='decimal'))
				if(match)items[idx+1].v=v
				else items[idx+1].v=cast(rtData, v, t)
			}
			Integer sz=(Integer)items.size()
			items.remove(idx)
		}
		result=items[0] ? (((String)items[0].t=='device')? items[0]:evaluateExpression(rtData, (Map)items[0])):[t:'dynamic', v:null]
		break
	}
	//return the value, either directly or via cast, if certain data type is requested
	//when dealing with devices, they need to be "converted" unless the request is to return devices
	if(dataType && (dataType!='device')&& ((String)result.t=='device')){
		//if not a list, make it a list
		if(!(result.v instanceof List))result.v=[result.v]
		switch ((Integer)((List)result.v).size()){
			case 0: result=[t:'error', v:'Empty device list']; break
			case 1: result=getDeviceAttribute(rtData, (String)((List)result.v)[0], (String)result.a, result.i); break
			default: result=[t:'string', v:buildDeviceAttributeList(rtData, (List)result.v, (String)result.a)]; break
		}
	}
	if(dataType){
		String t0=(String)result.t
		def t1=result.v
		if(dataType!=t0){
			Boolean match= (dataType in ['string', 'enum'])&& (t0 in ['string', 'enum'])
			if(!match)t1=cast(rtData, result.v, dataType, t0)
		}
		result=[t: dataType, v: t1]+(result.a ? [a: (String)result.a]:[:])+(result.i ? [a: result.i]:[:])
	}
	result.d=now()-time
	myDetail rtData, "evaluateExpression $expression  result: $result", -1
	return result
}

private static String buildList(list, String suffix='and'){
	if(!list)return ''
	List nlist
	if(!(list instanceof List))nlist=[list]
	else nlist=list
	Integer cnt=1
	String result=''
	for (item in nlist){
		result += "$item"+(cnt<(Integer)nlist.size()? (cnt==(Integer)nlist.size()-1 ? " $suffix ":', '):'')
		cnt++
	}
	return result
}

private String buildDeviceList(Map rtData, devices, String suffix='and'){
	if(!devices)return ''
	if(!(devices instanceof List))devices=[devices]
	List list=[]
	for (String device in devices){
		def dev=getDevice(rtData, device)
		if(dev!=null)Boolean a=list.push(dev)
	}
	return buildList(list, suffix)
}

private String buildDeviceAttributeList(Map rtData, devices, String attribute, String suffix='and'){
	if(!devices)return ''
	if(!(devices instanceof List))devices=[devices]
	List list=[]
	for (String device in devices){
		def value=getDeviceAttribute(rtData, device, attribute).v
		Boolean a=list.push(value)
	}
	return buildList(list, suffix)
}

/*** dewPoint returns the calculated dew point temperature					***/
/*** Usage: dewPoint(temperature, relativeHumidity[, scale])				***/
private Map func_dewpoint(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting dewPoint(temperature, relativeHumidity[, scale])']
	}
	Double t=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	Double rh=(Double)evaluateExpression(rtData, (Map)params[1], 'decimal').v
	//if no temperature scale is provided, we assume the location's temperature scale
	Boolean fahrenheit=((String)cast(rtData, (Integer)params.size()>2 ? (String)evaluateExpression(rtData, (Map)params[2]).v:(String)location.temperatureScale, 'string')).toUpperCase()=='F'
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

/*** celsius converts temperature from Fahrenheit to Celsius				***/
/*** Usage: celsius(temperature)											***/
private Map func_celsius(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting celsius(temperature)']
	}
	Double t=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	//convert temperature to Celsius
	return [t:'decimal', v:(Double)((t-32.0D)*5.0D/9.0D)]
}


/*** fahrenheit converts temperature from Celsius to Fahrenheit			***/
/*** Usage: fahrenheit(temperature)						***/
private Map func_fahrenheit(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting fahrenheit(temperature)']
	}
	Double t=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	//convert temperature to Fahrenheit
	return [t:'decimal', v:(Double)(t*9.0D/5.0D+32.0D)]
}

/*** fahrenheit converts temperature between Celsius and Fahrenheit if the ***/
/*** units differ from location.temperatureScale			***/
/*** Usage: convertTemperatureIfNeeded(celsiusTemperature, 'C')		***/
private Map func_converttemperatureifneeded(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting convertTemperatureIfNeeded(temperature, unit)']
	}
	Double t=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	String u=((String)evaluateExpression(rtData, (Map)params[1], 'string').v).toUpperCase()
	//convert temperature to Fahrenheit
	switch ((String)location.temperatureScale){
		case u: return [t:'decimal', v:t]
		case 'F': return func_celsius(rtData, [params[0]])
		case 'C': return func_fahrenheit(rtData, [params[0]])
	}
}

/*** integer converts a decimal to integer value			***/
/*** Usage: integer(decimal or string)						***/
private Map func_integer(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting integer(decimal or string)']
	}
	return [t:'integer', v:(Integer)evaluateExpression(rtData, (Map)params[0], 'integer').v]
}
private Map func_int(Map rtData, List params){ return func_integer(rtData, params)}

/*** decimal/float converts an integer value to it's decimal value		***/
/*** Usage: decimal(integer or string)						***/
private Map func_decimal(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting decimal(integer or string)']
	}
	return [t:'decimal', v:(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v]
}
private Map func_float(Map rtData, List params){ return func_decimal(rtData, params)}
private Map func_number(Map rtData, List params){ return func_decimal(rtData, params)}

/*** string converts an value to it's string value				***/
/*** Usage: string(anything)							***/
private Map func_string(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting string(anything)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t:'string', v:result]
}
private Map func_concat(Map rtData, List params){ return func_string(rtData, params)}
private Map func_text(Map rtData, List params){ return func_string(rtData, params)}

/*** Boolean converts a value to it's Boolean value				***/
/*** Usage: boolean(anything)							***/
private Map func_boolean(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting boolean(anything)']
	}
	return [t:'boolean', v:(Boolean)evaluateExpression(rtData, (Map)params[0], 'boolean').v]
}
private Map func_bool(Map rtData, List params){ return func_boolean(rtData, params)}

/*** sqr converts a decimal to square decimal value			***/
/*** Usage: sqr(integer or decimal or string)					***/
private Map func_sqr(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting sqr(integer or decimal or string)']
	}
	return [t:'decimal', v:(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v**2]
}

/*** sqrt converts a decimal to square root decimal value		***/
/*** Usage: sqrt(integer or decimal or string)					***/
private Map func_sqrt(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting sqrt(integer or decimal or string)']
	}
	return [t:'decimal', v:Math.sqrt((Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v)]
}

/*** power converts a decimal to power decimal value			***/
/*** Usage: power(integer or decimal or string, power)				***/
private Map func_power(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting sqrt(integer or decimal or string, power)']
	}
	return [t:'decimal', v:(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v ** (Double)evaluateExpression(rtData, (Map)params[1], 'decimal').v]
}

/*** round converts a decimal to rounded value			***/
/*** Usage: round(decimal or string[, precision])				***/
private Map func_round(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting round(decimal or string[, precision])']
	}
	Integer precision=((Integer)params.size()>1)? (Integer)evaluateExpression(rtData, (Map)params[1], 'integer').v:0
	return [t:'decimal', v:Math.round((Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v * (10 ** precision))/(10 ** precision)]
}

/*** floor converts a decimal to closest lower integer value		***/
/*** Usage: floor(decimal or string)						***/
private Map func_floor(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting floor(decimal or string)']
	}
	return [t:'integer', v:(Integer)cast(rtData, Math.floor((Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v), 'integer')]
}

/*** ceiling converts a decimal to closest higher integer value	***/
/*** Usage: ceiling(decimal or string)						***/
private Map func_ceiling(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting ceiling(decimal or string)']
	}
	return [t:'integer', v:(Integer)cast(rtData, Math.ceil((Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v), 'integer')]
}
private Map func_ceil(Map rtData, List params){ return func_ceiling(rtData, params)}


/*** sprintf converts formats a series of values into a string			***/
/*** Usage: sprintf(format, arguments)						***/
private Map func_sprintf(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting sprintf(format, arguments)']
	}
	String format=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	List args=[]
	for (Integer x=1; x<(Integer)params.size(); x++){
		Boolean a=args.push(evaluateExpression(rtData, (Map)params[x]).v)
	}
	try{
		return [t:'string', v:sprintf(format, args)]
	} catch(all){
		return [t:'error', v:"$all"]
	}
}
private Map func_format(Map rtData, List params){ return func_sprintf(rtData, params)}

/*** left returns a substring of a value					***/
/*** Usage: left(string, count)							***/
private Map func_left(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting left(string, count)']
	}
	String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	Integer count=(Integer)evaluateExpression(rtData, (Map)params[1], 'integer').v
	if(count>(Integer)value.size())count=(Integer)value.size()
	return [t:'string', v:value.substring(0, count)]
}

/*** right returns a substring of a value					***/
/*** Usage: right(string, count)						***/
private Map func_right(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting right(string, count)']
	}
	String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	Integer count=(Integer)evaluateExpression(rtData, (Map)params[1], 'integer').v
	if(count>(Integer)value.size())count=(Integer)value.size()
	return [t:'string', v:value.substring((Integer)value.size()-count, (Integer)value.size())]
}

/*** strlen returns the length of a string value				***/
/*** Usage: strlen(string)							***/
private Map func_strlen(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t:'error', v:'Expecting strlen(string)']
	}
	String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	return [t:'integer', v:(Integer)value.size()]
}
private Map func_length(Map rtData, List params){ return func_strlen(rtData, params)}

/*** coalesce returns the first non-empty parameter				***/
/*** Usage: coalesce(value1[, value2[, ..., valueN]])				***/
private Map func_coalesce(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting coalesce(value1[, value2[, ..., valueN]])']
	}
	for (i=0; i<(Integer)params.size(); i++){
		Map value=evaluateExpression(rtData, (Map)params[0])
		if(!((value.v instanceof List ? (value.v==[null])|| (value.v==[])|| (value.v==['null']):false)|| (value.v==null)|| ((String)value.t=='error')|| (value.v=='null')|| ((String)cast(rtData, value.v, 'string')==''))){
			return value
		}
	}
	return [t:'dynamic', v:null]
}

/*** trim removes leading and trailing spaces from a string			***/
/*** Usage: trim(value)								***/
private Map func_trim(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t:'error', v:'Expecting trim(value)']
	}
	String t0=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String value=(String)t0.trim()
	return [t:'string', v:value]
}

/*** trimleft removes leading spaces from a string				***/
/*** Usage: trimLeft(value)							***/
private Map func_trimleft(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t:'error', v:'Expecting trimLeft(value)']
	}
	String t0=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String value=(String)t0.replaceAll('^\\s+', '')
	return [t:'string', v:value]
}
private Map func_ltrim(Map rtData, List params){ return func_trimleft(rtData, params)}

/*** trimright removes trailing spaces from a string				***/
/*** Usage: trimRight(value)							***/
private Map func_trimright(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t:'error', v:'Expecting trimRight(value)']
	}
	String t0=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String value=(String)t0.replaceAll('\\s+$', '')
	return [t:'string', v:value]
}
private Map func_rtrim(Map rtData, List params){ return func_trimright(rtData, params)}

/*** substring returns a substring of a value					***/
/*** Usage: substring(string, start, count)					***/
private Map func_substring(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting substring(string, start, count)']
	}
	String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	Integer start=(Integer)evaluateExpression(rtData, (Map)params[1], 'integer').v
	def count=(Integer)params.size()>2 ? (Integer)evaluateExpression(rtData, (Map)params[2], 'integer').v:null
	//def end=null
	String result=''
	if((start<(Integer)value.size())&& (start>-(Integer)value.size())){
		if(count!=null){
			if(count<0){
				//reverse
				start=start<0 ? -start:(Integer)value.size()-start
				count=- count
				value=value.reverse()
			}
			if(start>=0){
				if(count>(Integer)value.size()-start)count= (Integer)value.size()-start
			}else{
				if(count>-start)count=-start
			}
		}
		start=start>=0 ? start:(Integer)value.size()+start
		if(count>(Integer)value.size()-start)count=(Integer)value.size()-start
		result=(count==null)? value.substring(start):value.substring(start, start+count)
	}
	return [t:'string', v:result]
}
private Map func_substr(Map rtData, List params){ return func_substring(rtData, params)}
private Map func_mid(Map rtData, List params){ return func_substring(rtData, params)}

/*** replace replaces a search text inside of a value				***/
/*** Usage: replace(string, search, replace[, [..], search, replace])		***/
private Map func_replace(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<3)|| ((Integer)params.size()%2 !=1)){
		return [t:'error', v:'Expecting replace(string, search, replace[, [..], search, replace])']
	}
	String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	Integer cnt=Math.floor(((Integer)params.size()-1)/2)
	for (Integer i=0; i<cnt; i++){
		String search=(String)evaluateExpression(rtData, (Map)params[i*2+1], 'string').v
		String replace=(String)evaluateExpression(rtData, (Map)params[i*2+2], 'string').v
		if(((Integer)search.size()>2)&& search.startsWith('/')&& search.endsWith('/')){
			search=~search.substring(1, (Integer)search.size()-1)
			value=value.replaceAll(search, replace)
		}else{
			value=value.replace(search, replace)
		}
	}
	return [t:'string', v:value]
}

/*** rangeValue returns the matching value in a range				***/
/*** Usage: rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])***/
private Map func_rangevalue(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)|| ((Integer)params.size()%2!=0)){
		return [t:'error', v:'Expecting rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])']
	}
	Double input=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	def value=params[1]
	Integer cnt=Math.floor(((Integer)params.size()-2)/2)
	for (Integer i=0; i<cnt; i++){
		Double point=(Double)evaluateExpression(rtData, (Map)params[i*2 +2], 'decimal').v
		if(input>=point)value=params[i*2 +3]
	}
	return value
}

/*** rainbowValue returns the matching value in a range				***/
/*** Usage: rainbowValue(input, minInput, minColor, maxInput, maxColor)		***/
private Map func_rainbowvalue(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=5)){
		return [t:'error', v:'Expecting rainbowValue(input, minColor, minValue, maxInput, maxColor)']
	}
	Integer input=(Integer)evaluateExpression(rtData, (Map)params[0], 'integer').v
	Integer minInput=(Integer)evaluateExpression(rtData, (Map)params[1], 'integer').v
	Map minColor=getColor(rtData, (String)evaluateExpression(rtData, (Map)params[2], 'string').v)
	Integer maxInput=(Integer)evaluateExpression(rtData, (Map)params[3], 'integer').v
	Map maxColor=getColor(rtData, (String)evaluateExpression(rtData, (Map)params[4], 'string').v)
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

/*** indexOf finds the first occurrence of a substring in a string		***/
/*** Usage: indexOf(stringOrDeviceOrList, substringOrItem)			***/
private Map func_indexof(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)|| (((String)params[0].t!='device')&& ((Integer)params.size()!=2))){
		return [t:'error', v:'Expecting indexOf(stringOrDeviceOrList, substringOrItem)']
	}
	if(((String)params[0].t=='device')&& ((Integer)params.size()>2)){
		String item=(String)evaluateExpression(rtData, (Map)params[(Integer)params.size()-1], 'string').v
		for (Integer idx=0; idx<(Integer)params.size()-1; idx++){
			Map it=evaluateExpression(rtData, (Map)params[idx], 'string')
			if(it.v==item){
				return [t:'integer', v:idx]
			}
		}
		return [t:'integer', v:-1]
	}else if(params[0].v instanceof Map){
		def item=evaluateExpression(rtData, (Map)params[1], (String)params[0].t).v
		def key=params[0].v.find{ it.value==item }?.key
		return [t:'string', v:key]
	}else{
		String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
		String substring=(String)evaluateExpression(rtData, (Map)params[1], 'string').v
		return [t:'integer', v:value.indexOf(substring)]
	}
}

/*** lastIndexOf finds the first occurrence of a substring in a string		***/
/*** Usage: lastIndexOf(string, substring)									***/
private Map func_lastindexof(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)|| (((String)params[0].t!='device')&& ((Integer)params.size()!=2))){
		return [t:'error', v:'Expecting lastIndexOf(string, substring)']
	}
	if(((String)params[0].t=='device')&& ((Integer)params.size()>2)){
		String item=(String)evaluateExpression(rtData, (Map)params[(Integer)params.size()-1], 'string').v
		for (Integer idx=(Integer)params.size()-2; idx>=0; idx--){
			Map it=evaluateExpression(rtData, (Map)params[idx], 'string')
			if(it.v==item){
				return [t:'integer', v:idx]
			}
		}
		return [t:'integer', v:-1]
	}else if(params[0].v instanceof Map){
		String item=evaluateExpression(rtData, (Map)params[1], (String)params[0].t).v
		def key=params[0].v.find{ it.value==item }?.key
		return [t:'string', v:key]
	}else{
		String value=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
		String substring=(String)evaluateExpression(rtData, (Map)params[1], 'string').v
		return [t:'integer', v:value.lastIndexOf(substring)]
	}
}


/*** lower returns a lower case value of a string				***/
/*** Usage: lower(string)							***/
private Map func_lower(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting lower(string)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t:'string', v:result.toLowerCase()]
}

/*** upper returns a upper case value of a string				***/
/*** Usage: upper(string)							***/
private Map func_upper(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting upper(string)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t:'string', v:result.toUpperCase()]
}

/*** title returns a title case value of a string				***/
/*** Usage: title(string)							***/
private Map func_title(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting title(string)']
	}
	String result=''
	for(Map param in params){
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t:'string', v:result.tokenize(' ')*.toLowerCase()*.capitalize().join(' ')]
}

/*** avg calculates the average of a series of numeric values			***/
/*** Usage: avg(values)								***/
private Map func_avg(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting avg(value1, value2, ..., valueN)']
	}
	Double sum=0
	for (Map param in params){
		sum += (Double)evaluateExpression(rtData, param, 'decimal').v
	}
	return [t:'decimal', v:sum/(Integer)params.size()]
}

/*** median returns the value in the middle of a sorted array			***/
/*** Usage: median(values)							***/
private Map func_median(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting median(value1, value2, ..., valueN)']
	}
	List data=params.collect{ evaluateExpression(rtData, (Map)it, 'dynamic')}.sort{ it.v }
	if(data){
		return data[(Integer)Math.floor((Integer)data.size()/2)]
	}
	return [t:'dynamic', v:'']
}

/*** least returns the value that is least found a series of numeric values	***/
/*** Usage: least(values)							***/
private Map func_least(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting least(value1, value2, ..., valueN)']
	}
	Map data=[:]
	for (Map param in params){
		Map value=evaluateExpression(rtData, param, 'dynamic')
		data[value.v]=[t: (String)value.t, v: value.v, c: (data[value.v]?.c ?: 0)+1]
	}
	def value=data.sort{ it.value.c }.collect{ it.value }[0]
	return [t: (String)value.t, v: value.v]
}

/*** most returns the value that is most found a series of numeric values	***/
/*** Usage: most(values)							***/
private Map func_most(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()<1)){
		return [t:'error', v:'Expecting most(value1, value2, ..., valueN)']
	}
	Map data=[:]
	for (Map param in params){
		Map value=evaluateExpression(rtData, param, 'dynamic')
		data[value.v]=[t: (String)value.t, v: value.v, c: (data[value.v]?.c ?: 0)+1]
	}
	def value=data.sort{ -it.value.c }.collect{ it.value }[0]
	return [t: (String)value.t, v: value.v]
}

/*** sum calculates the sum of a series of numeric values			***/
/*** Usage: sum(values)								***/
private Map func_sum(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()<1)){
		return [t:'error', v:'Expecting sum(value1, value2, ..., valueN)']
	}
	Double sum=0
	for (Map param in params){
		sum += (Double)evaluateExpression(rtData, param, 'decimal').v
	}
	return [t:'decimal', v:sum]
}

/*** variance calculates the standard deviation of a series of numeric values */
/*** Usage: stdev(values)							***/
private Map func_variance(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting variance(value1, value2, ..., valueN)']
	}
	Double sum=0
	List values=[]
	for (Map param in params){
		Double value=(Double)evaluateExpression(rtData, param, 'decimal').v
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

/*** stdev calculates the standard deviation of a series of numeric values	***/
/*** Usage: stdev(values)							***/
private Map func_stdev(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t:'error', v:'Expecting stdev(value1, value2, ..., valueN)']
	}
	Map result=func_variance(rtData, params)
	return [t:'decimal', v:Math.sqrt(result.v)]
}

/*** min calculates the minimum of a series of numeric values				***/
/*** Usage: min(values)								***/
private Map func_min(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)){
		return [t:'error', v:'Expecting min(value1, value2, ..., valueN)']
	}
	List data=params.collect{ evaluateExpression(rtData, (Map)it, 'dynamic')}.sort{ it.v }
	if(data){
		return data[0]
	}
	return [t:'dynamic', v:'']
}

/*** max calculates the maximum of a series of numeric values				***/
/*** Usage: max(values)								***/
private Map func_max(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()<1)){
		return [t:'error', v:'Expecting max(value1, value2, ..., valueN)']
	}
	List data=params.collect{ evaluateExpression(rtData, (Map)it, 'dynamic')}.sort{ it.v }
	if(data){
		return data[(Integer)data.size()-1]
	}
	return [t:'dynamic', v:'']
}

/*** abs calculates the absolute value of a number							***/
/*** Usage: abs(number)								***/
private Map func_abs(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()!=1)){
		return [t:'error', v:'Expecting abs(value)']
	}
	Double value=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	String dataType=(value==Math.round(value)? 'integer':'decimal')
	return [t: dataType, v: (Double)cast(rtData, Math.abs(value), dataType, 'decimal')]
}

/*** hslToHex converts a hue/saturation/level trio to it hex #rrggbb representation ***/
/*** Usage: hslToHex(hue, saturation, level)								***/
private Map func_hsltohex(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()!=3)){
		return [t:'error', v:'Expecting hsl(hue, saturation, level)']
	}
	Double hue=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
	Double saturation=(Double)evaluateExpression(rtData, (Map)params[1], 'decimal').v
	Double level=(Double)evaluateExpression(rtData, (Map)params[2], 'decimal').v
	return [t:'string', v:hslToHex(hue, saturation, level)]
}

/*** count calculates the number of true/non-zero/non-empty items in a series of numeric values		***/
/*** Usage: count(values)								***/
private Map func_count(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()<1)){
		return [t:'integer', v:0]
	}
	Integer count=0
	if((params.size()==1)&& (((String)params[0].t=='string')|| ((String)params[0].t=='dynamic'))){
		List list=((String)evaluateExpression(rtData, (Map)params[0], 'string').v).split(',').toList()
		for (Integer i=0; i<(Integer)list.size(); i++){
			count += (Boolean)cast(rtData, list[i], 'boolean')? 1:0
		}
	}else{
		for (Map param in params){
			count += ((Boolean)evaluateExpression(rtData, param, 'boolean').v)? 1:0
		}
	}
	return [t:'integer', v:count]
}

/*** size returns the number of values provided								***/
/*** Usage: size(values)							***/
private Map func_size(Map rtData, List params){
	if(params==null || !(params instanceof List)|| (params.size()<1)){
		return [t:'integer', v:0]
	}
	Integer count=0
	if(((Integer)params.size()==1)&& (((String)params[0].t=='string')|| ((String)params[0].t=='dynamic'))){
		List list=((String)evaluateExpression(rtData, (Map)params[0], 'string').v).split(',').toList()
		count=(Integer)list.size()
	}else{
		count=(Integer)params.size()
	}
	return [t:'integer', v:count]
}

/*** age returns the number of milliseconds an attribute had the current value*/
/*** Usage: age([device:attribute])											***/
private Map func_age(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t:'error', v:'Expecting age([device:attribute])']
	}
	Map param=evaluateExpression(rtData, (Map)params[0], 'device')
	if(((String)param.t=='device')&& ((String)param.a)&& (Integer)((List)param.v).size()){
		def device=getDevice(rtData, (String)((List)param.v)[0])
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

/*** previousAge returns the number of milliseconds an attribute had the	***/
/*** previous value								***/
/*** Usage: previousAge([device:attribute])									***/
private Map func_previousage(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t:'error', v:'Expecting previousAge([device:attribute])']
	}
	Map param=evaluateExpression(rtData, (Map)params[0], 'device')
	if(((String)param.t=='device')&& ((String)param.a)&& (Integer)((List)param.v).size()){
		def device=getDevice(rtData, (String)((List)param.v)[0])
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
			return [t:'long', v:604800000]
		}
	}
	return [t: 'error', v: 'Invalid device']
}

/*** previousValue returns the previous value of the attribute				***/
/*** Usage: previousValue([device:attribute])								***/
private Map func_previousvalue(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t: 'error', v: 'Expecting previousValue([device:attribute])']
	}
	Map param=evaluateExpression(rtData, (Map)params[0], 'device')
	if(((String)param.t=='device')&& ((String)param.a)&& (Integer)((List)param.v).size()){
		Map attribute=Attributes()[(String)param.a]
		if(attribute){
			def device=getDevice(rtData, (String)((List)param.v)[0])
			if(device!=null && !isDeviceLocation(device)){
				List states=device.statesSince((String)param.a, new Date(now()-604500000), [max: 5])
				if((Integer)states.size()>1){
					def newValue=states[0].getValue()
					//some events get duplicated, so we really want to look for the last "different valued" state
					for(Integer i=1; i<(Integer)states.size(); i++){
						def result=states[i].getValue()
						if(result!=newValue){
							return [t: (String)attribute.t, v: cast(rtData, result, (String)attribute.t)]
						}
					}
				}
				//we're saying 7 days, though it may be wrong - but we have no data
				return [t: 'string', v: '']
			}
		}
	}
	return [t: 'error', v: 'Invalid device']
}

/*** newer returns the number of devices whose attribute had the current ***/
/*** value for less than the specified number of milliseconds		***/
/*** Usage: newer([device:attribute] [,.., [device:attribute]], threshold)***/
private Map func_newer(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t: 'error', v: 'Expecting newer([device:attribute] [,.., [device:attribute]], threshold)']
	}
	Integer threshold=(Integer)evaluateExpression(rtData, (Map)params[(Integer)params.size()-1], 'integer').v
	Integer result=0
	for (Integer i=0; i<(Integer)params.size()-1; i++){
		Map age=func_age(rtData, [params[i]])
		if(((String)age.t!='error')&& (age.v<threshold))result++
	}
	return [t: 'integer', v: result]
}

/*** older returns the number of devices whose attribute had the current ***/
/*** value for more than the specified number of milliseconds		***/
/*** Usage: older([device:attribute] [,.., [device:attribute]], threshold)	***/
private Map func_older(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t: 'error', v: 'Expecting older([device:attribute] [,.., [device:attribute]], threshold)']
	}
	Integer threshold=(Integer)evaluateExpression(rtData, (Map)params[(Integer)params.size()-1], 'integer').v
	Integer result=0
	for (Integer i=0; i<(Integer)params.size()-1; i++){
		Map age=func_age(rtData, [params[i]])
		if(((String)age.t!='error')&& (age.v>=threshold))result++
	}
	return [t: 'integer', v: result]
}

/*** startsWith returns true if a string starts with a substring			***/
/*** Usage: startsWith(string, substring)									***/
private Map func_startswith(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting startsWith(string, substring)']
	}
	String string=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String substring=(String)evaluateExpression(rtData, (Map)params[1], 'string').v
	return [t: 'boolean', v: string.startsWith(substring)]
}

/*** endsWith returns true if a string ends with a substring				***/
/*** Usage: endsWith(string, substring)										***/
private Map func_endswith(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting endsWith(string, substring)']
	}
	String string=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String substring=(String)evaluateExpression(rtData, (Map)params[1], 'string').v
	return [t: 'boolean', v: string.endsWith(substring)]
}

/*** contains returns true if a string contains a substring					***/
/*** Usage: contains(string, substring)										***/
private Map func_contains(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)|| (((String)params[0].t!='device')&& ((Integer)params.size()!=2))){
		return [t: 'error', v: 'Expecting contains(string, substring)']
	}
	if(((String)params[0].t=='device')&& ((Integer)params.size()>2)){
		String item=evaluateExpression(rtData, (Map)params[(Integer)params.size()-1], 'string').v
		for (Integer idx=0; idx<(Integer)params.size()-1; idx++){
			Map it=evaluateExpression(rtData, (Map)params[idx], 'string')
			if(it.v==item){
				return [t:'boolean', v:true]
			}
		}
		return [t:'boolean', v:false]
	}else{
		String string=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
		String substring=(String)evaluateExpression(rtData, (Map)params[1], 'string').v
		return [t: 'boolean', v: string.contains(substring)]
	}
}

/*** matches returns true if a string matches a pattern						***/
/*** Usage: matches(string, pattern)										***/
private Map func_matches(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting matches(string, pattern)']
	}
	String string=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String pattern=(String)evaluateExpression(rtData, (Map)params[1], 'string').v
	if(((Integer)pattern.size()>2)&& pattern.startsWith('/')&& pattern.endsWith('/')){
		pattern=~pattern.substring(1, (Integer)pattern.size()-1)
		return [t: 'boolean', v: !!(string =~ pattern)]
	}
	return [t: 'boolean', v: string.contains(pattern)]
}

/*** eq returns true if two values are equal								***/
/*** Usage: eq(value1, value2)							***/
private Map func_eq(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting eq(value1, value2)']
	}
	String t=(String)params[0].t=='device' ? (String)params[1].t:(String)params[0].t
	Map value1=evaluateExpression(rtData, (Map)params[0], t)
	Map value2=evaluateExpression(rtData, (Map)params[1], t)
	return [t: 'boolean', v: value1.v==value2.v]
}

/*** lt returns true if value1<value2										***/
/*** Usage: lt(value1, value2)							***/
private Map func_lt(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting lt(value1, value2)']
	}
	Map value1=evaluateExpression(rtData, (Map)params[0])
	Map value2=evaluateExpression(rtData, (Map)params[1], (String)value1.t)
	return [t: 'boolean', v: value1.v<value2.v]
}

/*** le returns true if value1<=value2									***/
/*** Usage: le(value1, value2)							***/
private Map func_le(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting le(value1, value2)']
	}
	Map value1=evaluateExpression(rtData, (Map)params[0])
	Map value2=evaluateExpression(rtData, (Map)params[1], (String)value1.t)
	return [t: 'boolean', v: value1.v<=value2.v]
}

/*** gt returns true if value1>value2									***/
/*** Usage: gt(value1, value2)							***/
private Map func_gt(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting gt(value1, value2)']
	}
	Map value1=evaluateExpression(rtData, (Map)params[0])
	Map value2=evaluateExpression(rtData, (Map)params[1], (String)value1.t)
	return [t: 'boolean', v: value1.v>value2.v]
}

/*** ge returns true if value1>=value2									***/
/*** Usage: ge(value1, value2)							***/
private Map func_ge(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=2)){
		return [t: 'error', v: 'Expecting ge(value1, value2)']
	}
	Map value1=evaluateExpression(rtData, (Map)params[0])
	Map value2=evaluateExpression(rtData, (Map)params[1], (String)value1.t)
	return [t: 'boolean', v: value1.v>=value2.v]
}

/*** not returns the negative Boolean value								***/
/*** Usage: not(value)								***/
private Map func_not(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t: 'error', v: 'Expecting not(value)']
	}
	Boolean value=(Boolean)evaluateExpression(rtData, (Map)params[0], 'boolean').v
	return [t: 'boolean', v: !value]
}

/*** if evaluates a Boolean and returns value1 if true, or value2 otherwise ***/
/*** Usage: if(condition, valueIfTrue, valueIfFalse)						***/
private Map func_if(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=3)){
		return [t: 'error', v: 'Expecting if(condition, valueIfTrue, valueIfFalse)']
	}
	Boolean value=(Boolean)evaluateExpression(rtData, (Map)params[0], 'boolean').v
	return value ? evaluateExpression(rtData, (Map)params[1]):evaluateExpression(rtData, (Map)params[2])
}

/*** isEmpty returns true if the value is empty								***/
/*** Usage: isEmpty(value)							***/
private Map func_isempty(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t: 'error', v: 'Expecting isEmpty(value)']
	}
	Map value=evaluateExpression(rtData, (Map)params[0])
	Boolean result=(value.v instanceof List ? (value.v==[null])|| (value.v==[])|| (value.v==['null']):false)|| (value.v==null)|| ((String)value.t=='error')|| (value.v=='null')|| ((String)cast(rtData, value.v, 'string')=='')|| ("$value.v".toString()=='')
	return [t: 'boolean', v: result]
}

/*** datetime returns the value as a datetime type							***/
/*** Usage: datetime([value])							***/
private Map func_datetime(Map rtData, List params){
	if(!(params instanceof List)|| ((Integer)params.size()>1)){
		return [t: 'error', v: 'Expecting datetime([value])']
	}
	Long value=(Integer)params.size()>0 ? (Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v:now()
	return [t: 'datetime', v: value]
}

/*** date returns the value as a date type									***/
/*** Usage: date([value])							***/
private Map func_date(Map rtData, List params){
	if(!(params instanceof List)|| ((Integer)params.size()>1)){
		return [t: 'error', v: 'Expecting date([value])']
	}
	Long value=(Integer)params.size()>0 ? (Long)evaluateExpression(rtData, (Map)params[0], 'date').v:(Long)cast(rtData, now(), 'date', 'datetime')
	return [t: 'date', v: value]
}

/*** time returns the value as a time type									***/
/*** Usage: time([value])							***/
private Map func_time(Map rtData, List params){
	if(!(params instanceof List)|| ((Integer)params.size()>1)){
		return [t: 'error', v: 'Expecting time([value])']
	}
	Long value=(Integer)params.size()>0 ? (Long)evaluateExpression(rtData, (Map)params[0], 'time').v:(Long)cast(rtData, now(), 'time', 'datetime')
	return [t: 'time', v: value]
}

/*** addSeconds returns the value as a time type							***/
/*** Usage: addSeconds([dateTime, ]seconds)									***/
private Map func_addseconds(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting addSeconds([dateTime, ]seconds)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtData, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*1000
	return [t: 'datetime', v: value+delta]
}

/*** addMinutes returns the value as a time type							***/
/*** Usage: addMinutes([dateTime, ]minutes)									***/
private Map func_addminutes(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting addMinutes([dateTime, ]minutes)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtData, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*60000
	return [t: 'datetime', v: value+delta]
}

/*** addHours returns the value as a time type								***/
/*** Usage: addHours([dateTime, ]hours)										***/
private Map func_addhours(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting addHours([dateTime, ]hours)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtData, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*3600000
	return [t: 'datetime', v: value+delta]
}

/*** addDays returns the value as a time type								***/
/*** Usage: addDays([dateTime, ]days)										***/
private Map func_adddays(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting addDays([dateTime, ]days)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtData, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*86400000
	return [t: 'datetime', v: value+delta]
}

/*** addWeeks returns the value as a time type								***/
/*** Usage: addWeeks([dateTime, ]weeks)										***/
private Map func_addweeks(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting addWeeks([dateTime, ]weeks)']
	}
	Long value=(Integer)params.size()==2 ? (Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v:now()
	Long delta=(Long)evaluateExpression(rtData, ((Integer)params.size()==2 ? (Map)params[1]:(Map)params[0]), 'long').v*604800000
	return [t: 'datetime', v: value+delta]
}

/*** weekDayName returns the name of the week day							***/
/*** Usage: weekDayName(dateTimeOrWeekDayIndex)								***/
private Map func_weekdayname(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t: 'error', v: 'Expecting weekDayName(dateTimeOrWeekDayIndex)']
	}
	Long value=(Long)evaluateExpression(rtData, (Map)params[0], 'long').v
	Integer index=((value>=86400000)? (Integer)utcToLocalDate(value).day:value)%7
	return [t: 'string', v: weekDays()[index]]
}

/*** monthName returns the name of the month								***/
/*** Usage: monthName(dateTimeOrMonthNumber)								***/
private Map func_monthname(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t: 'error', v: 'Expecting monthName(dateTimeOrMonthNumber)']
	}
	Long value=(Long)evaluateExpression(rtData, (Map)params[0], 'long').v
	Integer index=((value>=86400000)? (Integer)utcToLocalDate(value).month:value-1)%12+1
	return [t: 'string', v: yearMonths()[index]]
}

/*** arrayItem returns the nth item in the parameter list					***/
/*** Usage: arrayItem(index, item0[, item1[, .., itemN]])					***/
private Map func_arrayitem(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)){
		return [t: 'error', v: 'Expecting arrayItem(index, item0[, item1[, .., itemN]])']
	}
	Integer index=(Integer)evaluateExpression(rtData, (Map)params[0], 'integer').v
	if(((Integer)params.size()==2)&& ((params[1].t=='string')|| (params[1].t=='dynamic'))){
		List list=((String)evaluateExpression(rtData, (Map)params[1], 'string').v).split(',').toList()
		if((index<0)|| (index>=(Integer)list.size())){
			return [t: 'error', v: 'Array item index is outside of bounds.']
		}
		return [t: 'string', v: list[index]]
	}
	Integer sz=(Integer)params.size()-1
	if((index<0)|| (index>=sz)){
		return [t: 'error', v: 'Array item index is outside of bounds.']
	}
	return params[index+1]
}

/*** isBetween returns true if value>=startValue and value<=endValue	***/
/*** Usage: isBetween(value, startValue, endValue)							***/
private Map func_isbetween(Map rtData, List params){
	if(params==null || !(params instanceof List) || (Integer)params.size()!=3){
		return [t: 'error', v: 'Expecting isBetween(value, startValue, endValue)']
	}
	Map value=evaluateExpression(rtData, (Map)params[0])
	Map startValue=evaluateExpression(rtData, (Map)params[1], (String)value.t)
	Map endValue=evaluateExpression(rtData, (Map)params[2], (String)value.t)
	return [t: 'boolean', v: value.v>=startValue.v && value.v<=endValue.v]
}

/*** formatDuration returns a duration in a readable format					***/
/*** Usage: formatDuration(value[, friendly=false[, granularity='s'[, showAdverbs=false]]])	***/
private Map func_formatduration(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>4)){
		return [t: 'error', v: "Expecting formatDuration(value[, friendly=false[, granularity='s'[, showAdverbs=false]]])"]
	}
	Long value=(Long)evaluateExpression(rtData, (Map)params[0], 'long').v
	Boolean friendly=(Integer)params.size()>1 ? (Boolean)evaluateExpression(rtData, (Map)params[1], 'boolean').v:false
	String granularity=(Integer)params.size()>2 ? (String)evaluateExpression(rtData, (Map)params[2], 'string').v:'s'
	Boolean showAdverbs=(Integer)params.size()>3 ? (Boolean)evaluateExpression(rtData, (Map)params[3], 'boolean').v:false

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
	return [t: 'string', v: result]
}

/*** formatDateTime returns a datetime in a readable format					***/
/*** Usage: formatDateTime(value[, format])									***/
private Map func_formatdatetime(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting formatDateTime(value[, format])']
	}
	Long value=(Long)evaluateExpression(rtData, (Map)params[0], 'datetime').v
	String format=(Integer)params.size()>1 ? (String)evaluateExpression(rtData, (Map)params[1], 'string').v:(String)null
	return [t: 'string', v: (format ? formatLocalTime(value, format):formatLocalTime(value))]
}

/*** random returns a random value											***/
/*** Usage: random([range | value1, value2[, ..,valueN]])	***/
private Map func_random(Map rtData, List params){
	Integer sz=params && (params instanceof List)? (Integer)params.size():0
	switch (sz){
		case 0:
			return [t: 'decimal', v: Math.random()]
		case 1:
			Double range=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
			return [t: 'integer', v: (Integer)Math.round(range*Math.random())]
		case 2:
			if((((String)params[0].t=='integer')|| ((String)params[0].t=='decimal'))&& (((String)params[1].t=='integer')|| ((String)params[1].t=='decimal'))){
				Double min=(Double)evaluateExpression(rtData, (Map)params[0], 'decimal').v
				Double max=(Double)evaluateExpression(rtData, (Map)params[1], 'decimal').v
				if(min>max){
				Double swap=min
				min=max
				max=swap
			}
			return [t: 'integer', v: (Integer)Math.round(min+(max-min)*Math.random())]
		}
	}
	Integer choice=(Integer)Math.round((sz-1)*Math.random())
	if(choice>=sz)choice=sz-1
	return params[choice]
}


/*** distance returns a distance measurement											***/
/*** Usage: distance((device | latitude, longitude), (device | latitude, longitude)[, unit])	***/
private Map func_distance(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<2)|| ((Integer)params.size()>4)){
		return [t: 'error', v: 'Expecting distance((device | latitude, longitude), (device | latitude, longitude)[, unit])']
	}
	Double lat1, lng1, lat2, lng2
	String unit
	Integer idx=0
	Integer pidx=0
	String errMsg=''
	while (pidx<(Integer)params.size()){
		if(((String)params[pidx].t!='device')|| (((String)params[pidx].t=='device')&& !!params[pidx].a)){
			//a decimal or device attribute is provided
			switch (idx){
			case 0:
				lat1=(Double)evaluateExpression(rtData, (Map)params[pidx], 'decimal').v
				break
			case 1:
				lng1=(Double)evaluateExpression(rtData, (Map)params[pidx], 'decimal').v
				break
			case 2:
				lat2=(Double)evaluateExpression(rtData, (Map)params[pidx], 'decimal').v
				break
			case 3:
				lng2=(Double)evaluateExpression(rtData, (Map)params[pidx], 'decimal').v
				break
			case 4:
				unit=(String)evaluateExpression(rtData, (Map)params[pidx], 'string').v
			}
			idx += 1
			pidx += 1
			continue
		}else{
			switch (idx){
			case 0:
			case 2:
				params[pidx].a='latitude'
				Double lat=(Double)evaluateExpression(rtData, (Map)params[pidx], 'decimal').v
				params[pidx].a='longitude'
				Double lng=(Double)evaluateExpression(rtData, (Map)params[pidx], 'decimal').v
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
	if(errMsg!='')return [t: 'error', v: errMsg]
	if((idx<4)|| (idx>5))return [t: 'error', v: 'Invalid parameter combination. Expecting either two devices, a device and two decimals, or four decimals, followed by an optional unit.']
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

/*** json encodes data as a JSON string					***/
/*** Usage: json(value[, pretty])									***/
private Map func_json(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()<1)|| ((Integer)params.size()>2)){
		return [t: 'error', v: 'Expecting json(value[, format])']
	}
	def builder=new groovy.json.JsonBuilder([params[0].v])
	String op=params[1] ? 'toPrettyString':'toString'
	String json=builder."${op}"()
	return [t: 'string', v: json[1..-2].trim()]
}

/*** percent encodes data for use in a URL					***/
/*** Usage: urlencode(value)									***/
private Map func_urlencode(Map rtData, List params){
	if(params==null || !(params instanceof List)|| ((Integer)params.size()!=1)){
		return [t: 'error', v: 'Expecting urlencode(value])']
	}
	// URLEncoder converts spaces to+which is then indistinguishable from any
	// actual+characters in the value. Match encodeURIComponent in ECMAScript
	// which encodes "a+b c" as "a+b%20c" rather than URLEncoder's "a+b+c"
	String t0=(String)evaluateExpression(rtData, (Map)params[0], 'string').v
	String value=(t0!=null ? t0:'').replaceAll('\\+', '__wc_plus__')
	return [t: 'string', v: URLEncoder.encode(value, 'UTF-8').replaceAll('\\+', '%20').replaceAll('__wc_plus__', '+')]
}
private Map func_encodeuricomponent(Map rtData, List params){ return func_urlencode(rtData, params)}

/*** COMMON PUBLISHED METHODS							***/

private String mem(Boolean showBytes=true){
	String mbytes=new groovy.json.JsonOutput().toJson(state)
	Integer bytes=(Integer)mbytes.length()
	return Math.round(100.0D*(bytes/100000.0D))+"%${showBytes ? " ($bytes bytes)".toString() : ""}"
}

private String runTimeHis(Map rtData){
	String myId=(String)rtData.id
	return 'Total run history: '+(theCacheFLD[myId].runTimeHis).toString()+'<br>' +
		'Last run details: '+(theCacheFLD[myId].runStats).toString()
}

/*** UTILITIES									***/

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
	if(theHashMapFLD!=null){
		result=(String)theHashMapFLD[myId]
	}else theHashMapFLD=[:]
	if(result==(String)null){
		result=':'+md5('core.'+myId)+':'
		if(updateCache){
			theHashMapFLD[myId]=result
		}
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

@Field final List trueStrings=[  '1', 'true', "on", "open", "locked", "active", "wet", "detected", "present", "occupied", "muted", "sleeping"]
@Field final List falseStrings=[ '0', 'false', "off", "closed", "unlocked", "inactive", "dry", "clear", "not detected", "not present", "not occupied", "unmuted", "not sleeping", "null"]

private cast(Map rtData, value, String dataType, String srcDataType=(String)null){
	if(dataType=='dynamic')return value
	if(value==null){
		value=''
		srcDataType='string'
	}
	value=(value instanceof GString)? "$value".toString():value //get rid of GStrings
	if(srcDataType==(String)null || (Integer)srcDataType.length()==0 || srcDataType=='boolean' || srcDataType=='dynamic'){
		if(value instanceof List){ srcDataType='device' }else
		if(value instanceof Boolean){ srcDataType='boolean' }else
		if(value instanceof String){ srcDataType='string' }else
		if(value instanceof Integer){ srcDataType='integer' }else
		if(value instanceof BigInteger){ srcDataType='long' }else
		if(value instanceof Long){ srcDataType='long' }else
		if(value instanceof Double){ srcDataType='decimal' }else
		if(value instanceof Float){ srcDataType='decimal' }else
		if(value instanceof BigDecimal){ srcDataType='decimal' }else
		if(value instanceof Map && value.x!=null && value.y!=null && value.z!=null){ srcDataType='vector3' }else{
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
	myDetail rtData, "cast $srcDataType $value as $dataType"
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
				case 'device': return buildDeviceList(rtData, value)
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
			} catch(all){
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
			} catch(all){
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
			} catch(all){
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
			Long d= srcDataType=='string' ? stringToTime(value):(Long)value // (Long)cast(rtData, value, 'long')
			Date t1=new Date(d)
			Long t2=Math.round(((Integer)t1.hours*3600.0D+(Integer)t1.minutes*60.0D+(Integer)t1.seconds)*1000.0D)
			return t2
		case 'date':
			if(srcDataType=='time' && value<86400000) value=getTimeToday(value)
			Long d=(srcDataType=='string')? stringToTime(value):(Long)value // (Long)cast(rtData, value, 'long')
			Date t1=new Date(d)
			Long t2=Math.round((Math.floor(d/1000.0D)*1000.0D)-(((Integer)t1.hours*3600.0D+(Integer)t1.minutes*60.0D+(Integer)t1.seconds)*1000.0D)) // take ms off and first guess at midnight (could be earlier/later depending if DST change day
			Long t3=Math.round(t2-(1.0D*3600000.0D)) // guess at 11 PM
			Long t4=Math.round(t2+(4.0D*3600000.0D)) // guess at 04 AM
			Long t5=Math.round(t2+(3.0D*3600000.0D)+((Integer)location.timeZone.getOffset(t3)-(Integer)location.timeZone.getOffset(t4))) // normalize to 3:00 AM for DST
			return t5
		case 'datetime':
			if(srcDataType=='time' && value<86400000) return getTimeToday(value)
			return (srcDataType=='string')? stringToTime(value):(Long)value // (Long)cast(rtData, value, 'long')
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
					t1=(Long)cast(rtData, value, 'long')
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
				value.removeAll{ !it }
				return value
			}
			String v=(String)cast(rtData, value, 'string')
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
		} catch (all0){
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
		} catch (all){
		}

		try{
			Date tt1=toDateTime(dateOrTimeOrString)
			result=(Long)tt1.getTime()
			return result
		} catch(all3){
		}

		try{
			def tz=location.timeZone
			if(dateOrTimeOrString =~ /\s[A-Z]{3}$/){ // this is not the timezone... strings like CET are not unique.
				try{
					tz=TimeZone.getTimeZone(dateOrTimeOrString[-3..-1])
					dateOrTimeOrString=dateOrTimeOrString.take((Integer)dateOrTimeOrString.size()-3).trim()
				} catch (all4){
				}
			}

			String t0=dateOrTimeOrString?.trim()?: ''
			Boolean hasMeridian=false
			Boolean hasAM
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
		} catch (all5){
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

private static Double _hue2rgb(p, q, t){
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

/******************************************************************************/
/*** DEBUG FUNCTIONS									***/
/******************************************************************************/
private void myDetail(Map rtData, String msg, Integer shift=-2){
	if((Boolean)rtData.eric){ Map a=log(msg, rtData, shift, null, 'warn', true, false)}
}

private Map log(message, Map rtData, Integer shift=-2, err=null, String cmd=(String)null, Boolean force=false, Boolean svLog=true){
	if(cmd=='timer'){
		return [m:message, t:now(), s:shift, e:err]
	}
	if(message instanceof Map){
		shift=message.s
		err=message.e
		message=(String)message.m+" (${now()-(Long)message.t}ms)"
	}
	String myMsg=(String)message
	cmd=cmd ? cmd:'debug'
	//shift is
	// 0 - initialize level, level set to 1
	// 1 - start of routine, level up
	// -1 - end of routine, level down
	// anything else - nothing happens
	Integer maxLevel=4
	Integer level=rtData?.debugLevel ? (Integer)rtData.debugLevel:0
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

	rtData.debugLevel=level
	Boolean hasErr=(err!=null && !!err)

	if(svLog && rtData!=null && rtData instanceof Map && rtData.logs instanceof List){
		myMsg=myMsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, "\r")
		if((Integer)myMsg.size()>1024){
			myMsg=myMsg[0..1023]+'...[TRUNCATED]'
		}
		List msgs=!hasErr ? myMsg.tokenize("\r"):[myMsg]
		for(msg in msgs){
			Boolean a=((List)rtData.logs).push([o: now()-(Long)rtData.timestamp, p: prefix2, m: msg+(hasErr ? " $err":''), c: cmd])
		}
	}
	if(hasErr){
		log."$cmd" "$prefix $myMsg $err"
	}else{
		if((cmd=='error' || cmd=='warn')|| force || !svLog || (Boolean)rtData.logsToHE || (Boolean)rtData.eric)log."$cmd" "$prefix $myMsg"
	}
	return [:]
}

private void info(message, Map rtData, Integer shift=-2, err=null){ Map a=log(message, rtData, shift, err, 'info')}
private void trace(message, Map rtData, Integer shift=-2, err=null){ Map a=log(message, rtData, shift, err, 'trace')}
private void debug(message, Map rtData, Integer shift=-2, err=null){ Map a=log(message, rtData, shift, err, 'debug')}
private void warn(message, Map rtData, Integer shift=-2, err=null){ Map a=log(message, rtData, shift, err, 'warn')}
private void error(message, Map rtData, Integer shift=-2, err=null){ Map a=log(message, rtData, shift, err, 'error')}
private Map timer(String message, Map rtData, Integer shift=-2, err=null){ log(message, rtData, shift, err, 'timer')}

private void tracePoint(Map rtData, String objectId, Long duration, value){
	if(objectId!=(String)null && rtData!=null && rtData.trace!=null){
		rtData.trace.points[objectId]=[o: Math.round(1.0D*now()-(Long)rtData.trace.t-duration), d: duration, v: value]
	}else{
		error "Invalid object ID $objectID for trace point...", rtData
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

private void initSunriseAndSunset(Map rtData){
	Map t0=svSunTFLD
	Long t=now()
	if(t0!=null){
		if(t<(Long)t0.nextM){
			rtData.sunTimes=[:]+t0
		}else{ t0=null; svSunTFLD=null; rtData.nextsunrise==null; rtData.nextsunset=null }
	}
	if(t0==null){
		Map sunTimes=app.getSunriseAndSunset()
		if(sunTimes.sunrise==null){
			warn 'Actual sunrise and sunset times are unavailable; please reset the location for your hub', rtData
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
		rtData.sunTimes=t0
		if(t)svSunTFLD=t0
		if(eric())log.debug 'updating global sunrise'
	}
	rtData.sunrise=rtData.sunTimes.sunrise
	rtData.sunset=rtData.sunTimes.sunset
}

private Long getSunriseTime(Map rtData){
	initSunriseAndSunset(rtData)
	return (Long)rtData.sunrise
}

private Long getSunsetTime(Map rtData){
	initSunriseAndSunset(rtData)
	return (Long)rtData.sunset
}

private Long getNextSunriseTime(Map rtData){
	if(rtData.nextsunrise==null)rtData.nextsunrise=getNextOccurance(rtData, 'Sunrise')
	return (Long)rtData.nextsunrise
}

private Long getNextSunsetTime(Map rtData){
	if(rtData.nextsunset==null)rtData.nextsunset=getNextOccurance(rtData, 'Sunset')
	return (Long)rtData.nextsunset
}

// This is trying to ensure we don't fire sunsets or sunrises twice in same day by ensuring we fire a bit later than actual sunrise or sunset
private Long getNextOccurance(Map rtData, String ttyp){
	Long t0=(Long)"get${ttyp}Time"(rtData)
	if(now()>t0){
		List t1=getLocationEventsSince("${ttyp.toLowerCase()}Time", new Date()-2)
		def t2
		if((Integer)t1.size()>0){
			t2=t1[0]
		}
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

private Long getNoonTime(Map rtData=null){
	return timeToday('12:00', location.timeZone).getTime()
}

private Long getNextNoonTime(Map rtData=null){
	return timeTodayAfter('23:59', '12:00', location.timeZone).getTime()
}

private void getLocalVariables(Map rtData, List vars, Map atomState){
	rtData.localVars=[:]
	Map values=atomState.vars
	for (var in vars){
		String t0=(String)var.t
		def t1=values[(String)var.n]
		Map variable=[t:t0, v:var.v!=null ? var.v:(t0.endsWith(']') ? (t1 instanceof Map ? t1:[:]):cast(rtData, t1, t0)), f: !!var.v] //f means fixed value - we won't save this to the state
		if(var.v!=null && (String)var.a=='s' && !t0.endsWith(']')){
			variable.v=evaluateExpression(rtData, evaluateOperand(rtData, null, (Map)var.v), t0).v
		}
		rtData.localVars[(String)var.n]=variable
	}
}

private Map getSystemVariablesAndValues(Map rtData){
	Map result=[:]+getSystemVariables
	for(variable in result){
		String keyt1=(String)variable.key
		if(variable.value.d!=null && (Boolean)variable.value.d)variable.value.v=getSystemVariableValue(rtData, keyt1)
		else{
			Map t0=rtData.cachePersist
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

private getSystemVariableValue(Map rtData, String name){
	switch (name){
	//case '$args': return "${rtData.args}".toString()
	case '$json': return "${rtData.json}".toString()
	case '$places': return "${rtData.settings?.places}".toString()
	case '$response': return "${rtData.response}".toString()
	case '$weather': return "${rtData.weather}".toString()
	case '$nfl': return "${rtData.nfl}".toString()
	case '$incidents': return "${rtData.incidents}".toString()
	case '$hsmTripped': return rtData.incidents instanceof List && (Integer)rtData.incidents.size()>0
	case '$hsmStatus': return (String)location.hsmStatus
	case '$mediaId': return rtData.mediaId
	case '$mediaUrl': return (String)rtData.mediaUrl
	case '$mediaType': return (String)rtData.mediaType
	case '$mediaSize': return (rtData.mediaData!=null ? (Integer)rtData.mediaData.size():0)
	case '$name': return (String)app.label
	case '$state': return (String)rtData.state?.new
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
	case '$sunrise': return getSunriseTime(rtData)
	case '$sunset': return getSunsetTime(rtData)
	case '$nextMidnight': return getNextMidnightTime()
	case '$nextNoon': return getNextNoonTime()
	case '$nextSunrise': return getNextSunriseTime(rtData)
	case '$nextSunset': return getNextSunsetTime(rtData)
	case '$time': Date t=localDate(); Integer h=(Integer)t.hours; Integer m=(Integer)t.minutes; return ((h==0 ? 12:(h>12 ? h-12:h))+':'+(m<10 ? "0$m":"$m")+" "+(h <12 ? 'A.M.':'P.M.')).toString()
	case '$time24': Date t=localDate(); Integer h=(Integer)t.hours; Integer m=(Integer)t.minutes; return (h+':'+(m<10 ? "0$m":"$m")).toString()
	case '$random':
		def tresult=getRandomValue(rtData, name)
		Double result
		if(tresult!=null)result=(Double)tresult
		else{
			result=(Double)Math.random()
			setRandomValue(rtData, name, result)
		}
		return result
	case '$randomColor':
		def tresult=getRandomValue(rtData, name)
		String result
		if(tresult!=null)result=(String)tresult
		else{
			result=(String)(getRandomColor(rtData))?.rgb
			setRandomValue(rtData, name, result)
		}
		return result
	case '$randomColorName':
		def tresult=getRandomValue(rtData, name)
		String result
		if(tresult!=null)result=(String)tresult
		else{
			result=(String)(getRandomColor(rtData))?.name
			setRandomValue(rtData, name, result)
		}
		return result
	case '$randomLevel':
		def tresult=getRandomValue(rtData, name)
		Integer result
		if(tresult!=null)result=(Integer)tresult
		else{
			result=(Integer)Math.round(100.0D*Math.random())
			setRandomValue(rtData, name, result)
		}
		return result
	case '$randomSaturation':
		def tresult=getRandomValue(rtData, name)
		Integer result
		if(tresult!=null)result=(Integer)tresult
		else{
			result=(Integer)Math.round(50.0D+50.0D*Math.random())
			setRandomValue(rtData, name, result)
		}
		return result
	case '$randomHue':
		def tresult=getRandomValue(rtData, name)
		Integer result
		if(tresult!=null)result=(Integer)tresult
		else{
			result=(Integer)Math.round(360.0D*Math.random())
			setRandomValue(rtData, name, result)
		}
		return result
	case '$locationMode':return (String)location.getMode()
	case '$temperatureScale':return (String)location.getTemperatureScale()
	}
}

private static void setSystemVariableValue(Map rtData, String name, value, Boolean cachePersist=true){
//	if(name==null || !(name.startsWith('$')))return
	Map var=rtData.systemVars[name]
	if(var==null || var.d!=null)return
	rtData.systemVars[name].v=value

	if(cachePersist){
		if(name in [
			'$args',
			'$httpContentType',
			'$httpStatusCode',
			'$httpStatusOk',
			'$iftttStatusCode',
			'$iftttStatusOk' ]){

			Map t0=rtData.cachePersist
			t0=t0!=null ? t0:[:]
			t0[name]=[:]+var+[v:value]
			if(name=='$args')t0[name]=[:]+var+[v:"${value}".toString()]
			else t0[name]=[:]+var+[v:value]
			rtData.cachePersist=t0
		}
	}
}

private static getRandomValue(Map rtData, String name){
	rtData.temp=rtData.temp!=null ? rtData.temp:[randoms:[:]]
	return rtData.temp.randoms[name]
}

private static void setRandomValue(Map rtData, String name, value){
	rtData.temp=rtData.temp!=null ? rtData.temp:[randoms:[:]]
	rtData.temp.randoms[name]=value
}

private static void resetRandomValues(Map rtData){
	rtData.temp=[randoms:[:]]
}

private Map getColorByName(Map rtData, String name){
	Map t1=getColors().find{ (String)it.name==name }
	Map t2
	if(t1!=null){ t2=[:]+t1; return t2 }
	return t1
}

private Map getRandomColor(Map rtData){
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
	Map result=[:]
	String mStr="getting attributes from parent"
	if(theAttributesFLD!=null){
		result=theAttributesFLD
	}else{
		if(eric())log.debug mStr
		result=(Map)parent.getChildAttributes()
		theAttributesFLD=result
	}
	if(result==[:]){
		if(eric())log.error "NO "+mStr
	}
	return result
}

@Field static Map theComparisonsFLD

//uses p, t
private Map Comparisons(){
	Map result=[:]
	String mStr="getting comparisons from parent"
	if(theComparisonsFLD!=null){
		result=theComparisonsFLD
	}else{
		if(eric())log.debug mStr
		result=(Map)parent.getChildComparisons()
		theComparisonsFLD=result
	}
	if(result==[:]){
		if(eric())log.error "NO "+mStr
	}
	return result
}

@Field static Map theVirtCommandsFLD

//uses o (override phys command), a (aggregate commands)
private Map VirtualCommands(){
	Map result=[:]
	String mStr="getting virt commands from parent"
	if(theVirtCommandsFLD!=null){
		result=theVirtCommandsFLD
	}else{
		if(eric())log.debug mStr
		result=(Map)parent.getChildVirtCommands()
		theVirtCommandsFLD=result
	}
	if(result==[:]){
		if(eric())log.error "NO "+mStr
	}
	return result
}

//uses c and r
// the physical command r: is replaced with command c. If the VirtualCommand c exists and has o: true we will use that virtual command; otherwise it will be replaced with a physical command
@Field final Map CommandsOverrides=[
		push: [c:"push",	s:null, r:"pushMomentary"],
		flash: [c:"flash",	s:null, r:"flashNative"] //flash native command conflicts with flash emulated command. Also needs "o" option on command described later
]

@Field static Map theVirtDevicesFLD

//uses ac, o
private Map VirtualDevices(){
	Map result=[:]
	String mStr="getting virt devices from parent"
	if(theVirtDevicesFLD!=null){
		result=theVirtDevicesFLD
	}else{
		if(eric())log.debug mStr
		result=(Map)parent.getChildVirtDevices()
		theVirtDevicesFLD=result
	}
	if(result==[:]){
		if(eric())log.error 'NO '+mStr
	}
	return result
}

@Field static Map thePhysCommandsFLD

//uses a, v
private Map PhysicalCommands(){
	Map result=[:]
	String mStr="getting commands from parent"
	if(thePhysCommandsFLD!=null){
		result=thePhysCommandsFLD
	}else{
		if(eric())log.debug mStr
		result=(Map)parent.getChildCommands()
		thePhysCommandsFLD=result
	}
	if(result==[:]){
		if(eric())log.error 'NO '+mStr
	}
	return result
}

@Field static List theColorsFLD

private List getColors(){
	List result=[]
	String mStr="getting colors from parent"
	if(theColorsFLD!=null){
		result=theColorsFLD
	}else{
		if(eric())log.debug mStr
		result=(List)parent.getColors()
		theColorsFLD=result
	}
	if(result==[]){
		if(eric())log.error 'NO '+mStr
	}
	return result
}
