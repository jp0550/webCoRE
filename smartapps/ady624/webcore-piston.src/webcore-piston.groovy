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
 * Last update August 23, 2019 for Hubitat
*/
public static String version() { return "v0.3.10f.20190822" }
public static String HEversion() { return "v0.3.10f.20190823" }

/*** webCoRE DEFINITION					***/

private static String handle() { return "webCoRE" }

import groovy.json.*
import hubitat.helper.RMUtils
import groovy.transform.Field

definition(
	name: "${handle()} Piston",
	namespace: "ady624",
	author: "Adrian Caramaliu",
	description: "Do not install this directly, use webCoRE instead",
	category: "Convenience",
	parent: "ady624:${handle()}",
	iconUrl: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png",
	iconX2Url: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE@2x.png",
	iconX3Url: "https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE@3x.png",
	importUrl: "https://raw.githubusercontent.com/imnotbob/webCoRE/hubitat-patches/smartapps/ady624/webcore-piston.src/webcore-piston.groovy"
)

preferences {
	page(name: "pageMain")
	page(name: "pageRun")
	page(name: "pageClear")
	page(name: "pageClearAll")
	page(name: "pageDumpPiston")
}

private static boolean eric() { return false }

/*** CONFIGURATION PAGES				***/

def pageMain() {
	return dynamicPage(name: "pageMain", title: "", install: true, uninstall: !!state.build) {
		if(!parent || !parent.isInstalled()) {
			section() {
				paragraph "Sorry, you cannot install a piston directly from the Dashboard, please use the webCoRE App instead."
			}
			section(sectionTitleStr("Installing webCoRE")) {
				paragraph "If you are trying to install webCoRE, please go back one step and choose webCoRE, not webCoRE Piston. You can also visit wiki.webcore.co for more information on how to install and use webCoRE"
				if(parent) {
					String t0 = (String)parent.getWikiUrl()
					href "", title: imgTitle("https://cdn.rawgit.com/ady624/webCoRE/master/resources/icons/app-CoRE.png", inputTitleStr("More information")), description: t0, style: "external", url: t0, required: false
				}
			}
		} else {
			section (sectionTitleStr("General")) {
				label name: "name", title: "Name", required: true, state: (name ? "complete" : null), defaultValue: parent.generatePistonName(), submitOnChange: true
			}

			section(sectionTitleStr("Dashboard")) {
				String dashboardUrl = (String)parent.getDashboardUrl()
				if(dashboardUrl) {
					dashboardUrl = "${dashboardUrl}piston/${hashId(app.id)}"
					href "", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/dashboard.png", inputTitleStr("View piston in dashboard")), style: "external", url: dashboardUrl, required: false
				} else {
					paragraph "Sorry, your dashboard does not seem to be enabled, please go to the parent app and enable the dashboard."
				}
			}

			section(sectionTitleStr("Application Info")) {
				Map rtData = getTemporaryRunTimeData(now())
				if((boolean)rtData.disabled) {
					paragraph "Piston is disabled by webCoRE"
				}
				if(!(boolean)rtData.active) {
					paragraph "Piston is paused"
				}
				if(!rtData.bin) {
					paragraph "Automatic backup bin code: ${rtData.bin}"
				}
				paragraph "Version: ${version()}"
				paragraph "VersionH: ${HEversion()}"
				paragraph "Memory Usage: ${mem()}"

			}

			section(sectionTitleStr("Recovery")) {
				href "pageRun", title: "Force-run this piston"
				href "pageClear", title: "Clear all data except local variables", description: "You will lose all logs, trace points, statistics, but no variables"
				href "pageClearAll", title: "Clear all data", description: "You will lose all data stored in any local variables"
			}

			section() {
				input "dev", "capability.*", title: "Devices", description: "Piston devices", multiple: true
				input "maxStats", "number", title: "Max number of stats", description: "Max number of stats", defaultValue: 100
				input "maxLogs", "number", title: "Max number of logs", description: "Max number of logs", defaultValue: 100
			}
			if(eric()) {
			section("Debug") {
				href "pageDumpPiston", title: "Dump piston structure", description: ""
			}
			}
		}
	}
}

def pageRun() {
	test()
	return dynamicPage(name: "pageRun", title: "", uninstall: false) {
		section("Run") {
			paragraph "Piston tested"
			Map t0 = parent.getWCendpoints()
			String t1 = "/execute/${hashId(app.id)}?access_token=${t0.at}"
			paragraph "Execute endpoint (cloud) ${t0.ep}${t1}"
			paragraph "Local Execute endpoint ${t0.epl}${t1}"
		}
	}
}

private String sectionTitleStr(String title)	{ return "<h3>$title</h3>" }
private String inputTitleStr(String title)	{ return "<u>$title</u>" }
private String pageTitleStr(String title)	{ return "<h1>$title</h1>" }
private String paraTitleStr(String title)	{ return "<b>$title</b>" }

private String imgTitle(String imgSrc, String titleStr, String color=(String)null, imgWidth=30, imgHeight=null) {
	String imgStyle = ""
	imgStyle += imgWidth ? "width: ${imgWidth}px !important;" : ""
	imgStyle += imgHeight ? "${imgWidth ? " " : ""}height: ${imgHeight}px !important;" : ""
	if(color) { return """<div style="color: ${color}; font-weight: bold;"><img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img></div>""" }
	else { return """<img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img>""" }
}

def pageClear() {
	clear1()
	return dynamicPage(name: "pageClear", title: "", uninstall: false) {
		section("Clear") {
			paragraph "All non-essential data has been cleared."
		}
	}
}

private void clear1() {
	state.logs = []
	state.stats = [:]
	//state.temp = [:]
	state.trace = [:]

	String tsemaphoreName = "sph" + "${app.id}"
	if(theSempahoresFLD) theSemaphoresFLD."${tsemaphoreName}" = null
	String queueName = "aevQ" + "${app.id}"
	if(theQueuesFLD) theQueuesFLD."${queueName}" = []

	cleanState()

	clearMyCache("clear1")
}

def pageClearAll() {
	clear1()
	state.cache = [:]
	state.vars = [:]
	state.store = [:]
	return dynamicPage(name: "pageClearAll", title: "", uninstall: false) {
		section("Clear All") {
			paragraph "All local data has been cleared."
		}
	}
}

private String dumpListDesc(data, int level, List lastLevel, String listLabel, boolean html=false) {
	String str = ""
	int cnt = 1
	List newLevel = lastLevel

	def list1 = data?.collect {it}
	list1?.each { par ->
		int t0 = cnt - 1
		if(par instanceof Map) {
			def newmap = [:]
			newmap["${listLabel}[${t0}]"] = par
			boolean t1 = (cnt == list1.size()) ? true : false
			newLevel[level] = t1
			str += dumpMapDesc(newmap, level, newLevel, !t1)
		} else if(par instanceof List || par instanceof ArrayList) {
			def newmap = [:]
			newmap["${listLabel}[${t0}]"] = par
			boolean t1 = (cnt == list1.size()) ? true : false
			newLevel[level] = t1
			str += dumpMapDesc(newmap, level, newLevel, !t1)
		} else {
			String lineStrt = "\n"
			for(int i=0; i < level; i++) {
				lineStrt += (i+1 < level) ? (!lastLevel[i] ? "   │" : "    " ) : "   "
			}
			lineStrt += (cnt == 1 && list1.size() > 1) ? "┌── " : (cnt < list1?.size() ? "├── " : "└── ")
			str += "${lineStrt}${listLabel}[${t0}]: ${par} (${getObjType(par)})"
		}
		cnt = cnt+1
	}
	return str
}

private String dumpMapDesc(data, int level, List lastLevel, boolean listCall=false, boolean html=false) {
	String str = ""
	int cnt = 1
	data?.each { par ->
		String lineStrt = ""
		List newLevel = lastLevel
		boolean thisIsLast = (cnt == data?.size() && !listCall) ? true : false
		if(level > 0) {
			newLevel[(level-1)] = thisIsLast
		}
		boolean theLast = thisIsLast
		if(level == 0) {
			lineStrt = "\n\n • "
		} else {
			theLast == (last && thisIsLast) ? true : false
			lineStrt = "\n"
			for(int i=0; i < level; i++) {
				lineStrt += (i+1 < level) ? (!newLevel[i] ? "   │" : "    " ) : "   "
			}
			lineStrt += ((cnt < data?.size() || listCall) && !thisIsLast) ? "├── " : "└── "
		}
		if(par?.value instanceof Map) {
			str += "${lineStrt}${par?.key.toString()}: (Map)"
			newLevel[(level+1)] = theLast
			str += dumpMapDesc(par?.value, level+1, newLevel)
		}
		else if(par?.value instanceof List || par?.value instanceof ArrayList) {
			str += "${lineStrt}${par?.key.toString()}: [List]"
			newLevel[(level+1)] = theLast

			str += dumpListDesc(par?.value, level+1, newLevel, "")
		}
		else {
			String objType = getObjType(par?.value)
			if(html) {
				str += "<span>${lineStrt}${par?.key.toString()}: (${par?.value}) (${objType})</span>"
			} else {
				str += "${lineStrt}${par?.key.toString()}: (${par?.value}) (${objType})"
			}
		}
		cnt = cnt + 1
	}
	return str
}

private String myObj(obj) {
	if(obj instanceof String) {return "String"}
	else if(obj instanceof Map) {return "Map"}
	else if(obj instanceof List) {return "List"}
	else if(obj instanceof ArrayList) {return "ArrayList"}
	else if(obj instanceof Integer) {return "Int"}
	else if(obj instanceof BigInteger) {return "BigInt"}
	else if(obj instanceof Long) {return "Long"}
	else if(obj instanceof Boolean) {return "Bool"}
	else if(obj instanceof BigDecimal) {return "BigDec"}
	else if(obj instanceof Float) {return "Float"}
	else if(obj instanceof Byte) {return "Byte"}
	else { return "unknown"}
}

private String getObjType(obj) {
	return "<span style='color:orange'>${myObj(obj)}</span>"
}

private String getMapDescStr(data) {
	String str = ""
	List lastLevel = [true]
	str = dumpMapDesc(data, 0, lastLevel)
	return str != "" ? str : "No Data was returned"
}

def pageDumpPiston() {
	def rtData = getRunTimeData()
	String message = getMapDescStr(rtData.piston)
	//String message = "${rtData.piston}"
	//log message, null, null, null, 'debug', true
	return dynamicPage(name: "pageDumpPiston", title: "", uninstall: false) {
		section("Piston dump") {
			paragraph "$message"
		}
	}
}

/*** PUBLIC METHODS					***/

public Boolean isInstalled() {
	return !!state.created
}

void installed() {
	if(!app.id) return
	state.created = now()
	state.modified = now()
	state.build = 0
	state.vars = state.vars ?: [:]
	state.subscriptions = state.subscriptions ?: [:]
	state.logging = 0
	initialize()
}

void updated() {
	unsubscribe()
	initialize()
}

void initialize() {
	cleanState()

	clearMyCache("initialize")
	//clearParentCache()

	if(!!state.active) {
		resume()
	}
}

private void cleanState() {
//cleanups between releases
	for(sph in state.findAll{ it.key.startsWith('sph') }) {
		state.remove(sph.key as String)
	}
	state.remove("hash")
	state.remove("piston")
	state.remove("cVersion")
	state.remove("hVersion")
	state.remove("disabled")
	state.remove("logPExec")
	state.remove("settings")
	state.remove("svSunT")
	state.remove("temp")
}

public Map get(boolean minimal = false) { // minimal is backup
	def rtData = getRunTimeData()
	return [
		meta: [
			id: (String)rtData.id,
			author: (String)rtData.author,
			name: rtData.svLabel ?: app.label,
			created: rtData.created,
			modified: rtData.modified,
			build: rtData.build,
			bin: rtData.bin,
			active: (boolean)rtData.active,
			category: rtData.category ?: 0
		],
		piston: rtData.piston
	] + (minimal ? [:] : [
		systemVars: getSystemVariablesAndValues(rtData),
		subscriptions: state.subscriptions,
		state: state.state,
		logging: (int)state.logging ?: 0,
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

public Map activity(lastLogTimestamp) {
	List logs = (List) state.logs
	int lsz = logs.size()
	long llt = lastLogTimestamp && lastLogTimestamp instanceof String && lastLogTimestamp.isLong() ? (long)lastLogTimestamp.toLong() : 0L
	int index = (llt && lsz) ? logs.findIndexOf{ it.t == llt } : 0
	index = index > 0 ? index : (llt ? 0 : lsz)
	return [
		name: state.svLabel ?: app.label,
		state: state.state,
		logs: index ? logs[0..index-1] : [],
		trace: state.trace,
		localVars: state.vars,
		memory: mem(),
		lastExecuted: state.lastExecuted,
		nextSchedule: state.nextSchedule,
		schedules: state.schedules
	]
}

public Map clearLogs() {
	clear1()
	//state.logs = []
	//cleanState()
	//clearMyCache()
	return [:]
}

private String decodeEmoji(String value) {
	if(!value) return ''
	return value.replaceAll(/(\:%[0-9A-F]{2}%[0-9A-F]{2}%[0-9A-F]{2}%[0-9A-F]{2}\:)/, { m -> URLDecoder.decode(m[0].substring(1, 13), 'UTF-8') })
}

private Map recreatePiston() {
	String sdata = ""
	int i = 0
	while(true) {
		String s = (String)settings?."chunk:$i"
		if(s) {
			sdata += s
		} else { break }
		i++
	}
	if(sdata) {
		def data = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(decodeEmoji(new String(sdata.decodeBase64(), "UTF-8")))
		def piston = [
			o: data.o ?: {},
			r: data.r ?: [],
			rn: !!data.rn,
			rop: data.rop ?: 'and',
			s: data.s ?: [],
			v: data.v ?: [],
			z: data.z ?: ''
		]
		setIds(piston)
		return piston
	}
	return [:]
}

public Map setup(data, chunks) {
	if(!data) {
		log.error "setup: no data"
		return [:]
	}
	clear1()
	state.modified = now()
	state.build = (int)(state.build ? (int)state.build + 1 : 1)
	def piston = [
		o: data.o ?: {},
		r: data.r ?: [],
		rn: !!data.rn,
		rop: data.rop ?: 'and',
		s: data.s ?: [],
		v: data.v ?: [],
		z: data.z ?: ''
	]
	setIds(piston)

	for(chunk in settings.findAll{ it.key.startsWith('chunk:') && !chunks[it.key] }) {
		app.clearSetting("${chunk.key}")
	}
	for(chunk in chunks) {
		app.updateSetting(chunk.key, [type: 'text', value: chunk.value])
	}
	app.updateSetting('bin', [type: 'text', value: state.bin ?: ''])
	app.updateSetting('author', [type: 'text', value: (String)state.author ?: ''])

	state.pep = piston.o?.pep ? true : false // pep is parallel execution - this serializes execution of events

	//clearMyCache("setup")  (already done with clear1())

	if(data.n) {
		if(!!state.svLabel) {
			String res = state.svLabel
			app.updateLabel(res)
			state.svLabel = null
		}
		app.updateLabel(data.n)
	}
	state.schedules = []
	state.vars = state.vars ?: [:]
	state.modifiedVersion = version()

	Map rtData = [:]
	rtData.piston = (Map) piston
	if((state.build == 1) || (!!state.active)) {
		rtData = resume(piston)
	} else checkLabel()
	return [active: state.active, build: state.build, modified: state.modified, state: state.state, rtData: rtData]
}

private int setIds(node, int maxId = 0, Map existingIds = [:], List requiringIds = [], int level = 0) {
	String nodeT = node?.t
	if(nodeT in ['if', 'while', 'repeat', 'for', 'each', 'switch', 'action', 'every', 'condition', 'restriction', 'group', 'do', 'on', 'event']) {
		int id = node['$'] ? (int)node['$'] : 0
		if(!id || existingIds[id]) {
			requiringIds.push(node)
		} else {
			maxId = maxId < id ? id : maxId
			existingIds[id] = id
		}
		if((nodeT == 'if') && (node.ei)) {
			node.ei.removeAll{ !it.c && !it.s }
			for (elseIf in node.ei) {
				id = elseIf['$'] ? (int)elseIf['$'] : 0
				if(!id || existingIds[id]) {
					requiringIds.push(elseIf)
				} else {
					maxId = (maxId < id) ? id : maxId
					existingIds[id] = id
				}
			}
		}
		if((nodeT == 'switch') && (node.cs)) {
			for (_case in node.cs) {
				id = _case['$'] ? (int)_case['$'] : 0
				if(!id || existingIds[id]) {
					requiringIds.push(_case)
				} else {
					maxId = (maxId < id) ? id : maxId
					existingIds[id] = id
				}
			}
		}
		if((nodeT == 'action') && (node.k)) {
			for (task in node.k) {
				id = task['$'] ? (int)task['$'] : 0
				if(!id || existingIds[id]) {
					requiringIds.push(task)
				} else {
					maxId = (maxId < id) ? id : maxId
					existingIds[id] = id
				}
			}
		}
	}
	for (list in node.findAll{ it.value instanceof List }) {
		for (item in list.value.findAll{ it instanceof Map }) {
			maxId = setIds(item, maxId, existingIds, requiringIds, level + 1)
		}
	}
	if(level == 0) {
		for (item in requiringIds) {
			maxId += 1
			item['$'] = maxId
		}
	}
	return maxId
}

public void settingsToState(String myKey, setval) {
log.warn "something call settingToState with ${myKey}"
	if(setval) {
		atomicState."${myKey}" = setval
		//state."${myKey}" = setval
	} else state.remove("${myKey}" as String)
	clearMyCache("setting to state $myKey")
	clearParentCache("setting to state $myKey")
	runIn(5, "checkLabel")
}

private void checkLabel(Map rtData = null) {
	if(!rtData) rtData = getTemporaryRunTimeData(now())
	boolean act = (boolean)rtData.active
	boolean dis = (boolean)rtData.disabled
	boolean found = match(app.label, "<span")
	if(!act || dis) {
		if(!found && !state.svLabel) {
			state.svLabel = app.label
			if(rtData) rtData.svLabel = app.label
			String tstr = ""
			if(!act) {
				tstr = "(Paused)"
			}
			if(dis) {
				tstr = "(Disabled) Kill switch is active"
			}
			String res = "${app.label} <span style='color:orange'>${tstr}</span>"
			app.updateLabel(res)
			clearMyCache("checklabel")
			return
		}
	} else {
		if(found && !!state.svLabel) {
			String res = state.svLabel
			app.updateLabel(res)
			state.svLabel = null
			if(rtData) rtData.svLabel = null
			clearMyCache("checklabel")
			return
		}
		//state.remove("svLabel")
	}
}

public void config(data) { // creates a new piston
	if(!data) {
		return
	}
	if(data.bin) {
		state.bin = data.bin
		app.updateSetting('bin', [type: 'text', value: state.bin ?: ''])
	}
	if(data.author) {
		state.author = (String)data.author
		app.updateSetting('author', [type: 'text', value: state.author ?: ''])
	}
	if(data.initialVersion) {
		state.initialVersion = (String)data.initialVersion
	}
	clearMyCache("config")
}

public Map setBin(bin) {
	if(!bin || !!state.bin) {
		log.error "setBin: no bin"
		return [:]
	}
	state.bin = bin
	app.updateSetting('bin', [type: 'text', value: bin ?: ''])
	String typ = "setBin"
	clearParentCache(typ)
	clearMyCache(typ)
	return [:]
}

public Map pausePiston() {
	state.active = false
	clearMyCache("pauseP")
	Map rtData = getRunTimeData()
	Map msg = timer "Piston successfully stopped", null, -1
	if((int)rtData.logging) info "Stopping piston...", rtData, 0
	state.schedules = []
	rtData.stats.nextSchedule = 0
	unsubscribe()
	unschedule()
	//app.updateSetting('dev', (Map) null)
	app.clearSetting("dev")
	state.trace = [:]
	state.subscriptions = [:]
	checkLabel(rtData)
	if((int)rtData.logging) info msg, rtData
	updateLogs(rtData)
	state.active = false
	clearMyCache("pauseP1")
	return rtData
}

public Map resume(Map piston=null) {
	state.active = true
	clearMyCache("resumeP")
	Map tempRtData = getTemporaryRunTimeData(now())
	Map msg = timer "Piston successfully started", null, -1
	if(piston) tempRtData.piston = piston
	state.subscriptions = [:]
	state.schedules = []
	Map rtData = getRunTimeData(tempRtData, null, true) //performs subscribeAll(rtData)

	if(rtData.logging) info "Starting piston... (${HEversion()})", rtData, 0
	checkVersion(rtData)
	checkLabel(rtData)
	if((int)rtData.logging) info msg, rtData
	updateLogs(rtData)
	rtData.result = [active: true, subscriptions: state.subscriptions]
	state.active = true
	clearMyCache("resumeP1")
	return rtData
}

public Map setLoggingLevel(level) {
	String tlogging = "$level".toString()
	int logging = (boolean)tlogging.isInteger() ? (int)tlogging.toInteger() : 0
	if(logging < 0) logging = 0
	if(logging > 3) logging = 3
	state.logging = logging
	if(logging == 0) state.logs = []
	cleanState()
	clearMyCache("setLoggingLevel")
	return [logging: logging]
}

public Map setCategory(category) {
	state.category = category
	cleanState()
	clearMyCache("setCategory")
	return [category: category]
}

public Map test() {
	handleEvents([date: new Date(), device: location, name: 'test', value: now()])
	return [:]
}

public Map execute(data, source) {
	handleEvents([date: new Date(), device: location, name: 'execute', value: source ?: now(), jsonData: data], false)
	return [:]
}

public Map clickTile(index) {
	handleEvents([date: new Date(), device: location, name: 'tile', value: index])
	return state.state ?: [:]
}

//atomic state performance is much worse in hubitat. Grab a cached version where possible
private Map getCachedAtomicState(Map rtData) {
	long atomStart = now()
	def atomState
	atomicState.loadState()
	atomState = atomicState.@backingMap
	if((int)rtData.logging > 2) debug "AtomicState generated in ${now() - atomStart}ms", rtData
	return atomState
}

@Field static Map theQueuesFLD
@Field static Map theSemaphoresFLD

// This can a) lock semaphore, b) wait for semaphore, c) queue event, d) just fall through (no locking, waiting)
private Map lockOrQueueSemaphore(String semaphore, event, boolean queue, Map rtData) {
	long r_semaphore = 0
	long semaphoreDelay = 0
	String semaphoreName
	String tsemaphoreName = "sph" + "${app.id}"
	String queueName = "aevQ" + "${app.id}"
	boolean waited = false
	boolean didQ = false
	long tt1 = now()
	long startTime = tt1
	if(theQueuesFLD) {
		
	} else theQueuesFLD = [:]
	if(theSemaphoresFLD) {
		;
	} else theSemaphoresFLD = [:]
	if(semaphore) {
		long lastSemaphore
		while (semaphore) {
			def t0 = theSemaphoresFLD."${tsemaphoreName}"
			long tt0 = t0 ? (long)t0.toLong() : 0L
			lastSemaphore = tt0
			if(!lastSemaphore || (tt1 - lastSemaphore > 100000L)) {
				theSemaphoresFLD."${tsemaphoreName}" = tt1
				semaphoreName = tsemaphoreName
				semaphoreDelay = waited ? tt1 - startTime : 0L
				r_semaphore = tt1
				break
			}
			if(queue) {
				if(event) {
					def myEvent = [
						t: event.date.getTime(),
						name: event.name,
						value: event.value,
						descriptionText: event.descriptionText,
						unit: event?.unit,
						physical: event?.physical,
						jsonData: event?.jsonData,
					] + (event instanceof com.hubitat.hub.domain.Event ? [:] : [
						index: event?.index,
						recovery: event?.recovery,
						schedule: event?.schedule,
						contentType: event?.contentType,
						responseData: event?.responseData,
						responseCode: event?.responseCode,
						setRtData: event?.setRtData
					])
					if(event.device) {
						myEvent.device = [id: event.device?.id, name: event.device?.name, label: event.device?.label ]
						if(event.device?.hubs) {
							myEvent.device.hubs = [t: "tt"]
						}
					}
					def mt0 = theQueuesFLD."${queueName}"
					List eventlst = mt0 ?: []
					eventlst.push(myEvent)
					theQueuesFLD."${queueName}" = eventlst
					didQ = true
				}
				break
			} else {
				waited = true
				pause(500)
				tt1 = now()
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

private Map getTemporaryRunTimeData(long startTime) {
	Map rtData = [:]
	rtData = [
		temporary: true,
		timestamp: startTime,
		logs:[]
	]
	Map t0 = getDSCache()
	Map t1 = getParentCache()
	return rtData + t0 + t1
}

@Field static Map theCacheFLD  // each piston has a map in here

private void clearMyCache(String meth=null) {
	String myId = hashId(app.id)
	if(!myId) return
	if(theCacheFLD) {
		theCacheFLD."${myId}" = null
	}
	if(eric()) log.warn "clearing my cache $meth"
}

private Map getDSCache() {
	Map result = [:]
	String appId = hashId(app.id)
	String myId = appId
	if(!myId) {
		log.error "getDSCache: no id"
		return [:]
	}
	if(theCacheFLD) {
		result = theCacheFLD."${myId}"
	} else theCacheFLD = [:]
	if(!result) {
		def t1 = [
			id: appId,
			logging: state.logging ? (int)state.logging : 0,
			svLabel: state.svLabel,
			name: state.svLabel ?: app.label,
			active: state.active ? true : false,
			category: state.category,
			pep: (boolean)state.pep,
			created: state.created,
			modified: state.modified,
			build: state.build,
			author: (String)state.author,
			bin: state.bin,
		]
		t1.devices = (settings.dev && (settings.dev instanceof List) ? settings.dev.collectEntries{[(hashId(it.id)): it]} : [:])
		result = [:] + t1
		theCacheFLD."${myId}" = result
		if(eric()) log.warn "creating piston cache"
	}
	if(!result) {
		log.error "no result getDSCache"
	}
	return [:] + result
}

@Field static Map theParentCacheFLD

public void clearParentCache(String meth=null) {
	theParentCacheFLD = null
	if(eric()) log.warn "clearing parent cache $meth"
}

private Map getParentCache() {
	Map result = [:]
	if(theParentCacheFLD) {
		result = theParentCacheFLD
	} else theParentCacheFLD = [:]
	if(!result) {
		def t0 = parent.getChildPstate()
		if(eric()) log.warn "gathering parent cache"

		def t1 = [
			coreVersion: (String)t0.sCv,
			hcoreVersion: (String)t0.sHv,
			powerSource: (String)t0.powerSource,
			region: (String)t0.region,
			instanceId: (String)t0.instanceId, //hashId(parent.id),
			settings: t0.stsetting, //state.settings,
			enabled: !!t0.enabled,
			disabled: !t0.enabled,
			logPExec: !!t0.logPExec,
			locationId: hashId(location.id + '-L'),
		]
		result = [:] + t1
		theParentCacheFLD = result
	}
	if(!result) {
		log.error "no result getParentCache"
	}

	return [:] + result
}

private Map getRunTimeData(Map rtData=null, Map retSt=null, boolean fetchWrappers=false) {
	long timestamp = now()
	long started = timestamp
	List logs = []
	Map piston
	long lstarted, lended
	if(rtData) {
		timestamp = (long)rtData.timestamp
		logs = rtData.logs ?: []
		piston = rtData.piston ?: null
		lstarted = rtData.lstarted ?: 0L
		lended = rtData.lended ?: 0L
	} else {
		rtData = getTemporaryRunTimeData(timestamp)
	}
	if(rtData.temporary) {
		rtData.remove("temporary")
	}

	Map mtt1 = [
		semaphore: retSt?.semaphore,
		semaphoreName: retSt?.semaphoreName,
		semaphoreDelay: retSt?.semaphoreDelay,
		wAtSem: retSt?.semaphoreDelay > 0 ? true : false,
	]
	rtData = rtData + mtt1

	rtData.locationModeId = hashId(location.getCurrentMode().id)

	rtData.timestamp = timestamp
	rtData.lstarted = lstarted
	rtData.lended = lended
	rtData.logs = [[t: timestamp]]
	if(logs && logs.size()) {
		rtData.logs = (List)rtData.logs + logs
	}

	rtData.eric = eric() && (rtData.logging > 2)

	rtData.trace = [t: timestamp, points: [:]]
	rtData.stats = [nextScheduled: 0]
	rtData.newCache = [:]
	rtData.schedules = []
	rtData.cancelations = [statements:[], conditions:[], all: false]
	rtData.updateDevices = false
	rtData.systemVars = [:] + getSystemVariables

	def atomState = ((boolean)rtData.pep || (boolean)rtData.wAtSem) ? getCachedAtomicState(rtData) : state
	rtData.cache = atomState.cache ?: [:]

	def st = atomState.state
	rtData.state = (st instanceof Map) ? st : [old: '', new: '']
	rtData.state.old = rtData.state.new
	rtData.store = atomState.store ?: [:]

	rtData.pStart = now()
	boolean doSubScribe = false
	if(piston) {
		;
	} else {
		piston = recreatePiston()
		doSubScribe = true
	}
	rtData.piston = piston
	rtData.localVars = getLocalVariables(rtData, piston.v, atomState)

	if(doSubScribe || fetchWrappers) subscribeAll(rtData, fetchWrappers)

	rtData.pEnd = now()
	if(!rtData.ended) {
		long t0 = now()
		rtData.ended = t0
		rtData.generatedIn = t0 - started
	}
	return rtData
}

private void checkVersion(Map rtData) {
	String ver = HEversion()
	String t0 = (String)rtData.hcoreVersion
	if(ver != t0) {
		//parent and child apps have different versions
		warn "WARNING: Results may be unreliable because the ${ver > rtData.hcoreVersion ? "child app's version ($ver) is newer than the parent app's version (${t0})" : "parent app's version (${t0}) is newer than the child app's version ($ver)" }. Please consider updating both apps to the same version.", rtData
	}
	if(!location.timeZone) {
		error "Your location is not setup correctly - timezone information is missing. Please select your location by placing the pin and radius on the map, then tap Save, and then tap Done. You may encounter error or incorrect timing until this is fixed."
	}
}

/*** EVENT HANDLING								***/

void deviceHandler(event) {
	handleEvents(event)
}

void timeHandler(event, boolean recovery = false) {
	handleEvents([date: new Date((long)event.t), device: location, name: 'time', value: (long)event.t, schedule: event, recovery: recovery], !recovery)
}

void timeRecoveryHandler(event) {
	if(event) {
		timeHandler(event, true)
	} else {
		timeHandler([t:now()], true)
	}

}

void executeHandler(event) {
	pause(150)
	handleEvents([date: event.date, device: location, name: 'execute', value: event.value, jsonData: event.jsonData])
}

@Field final Map getPistonLimits = [
	schedule: 3000,
	scheduleVariance: 970,
	executionTime: 20000,
	slTime: 1000,
	taskShortDelay: 150,
	taskLongDelay: 500,
	taskMaxDelay: 2000,
	maxStats: 100,
	maxLogs: 100,
]

void handleEvents(event, boolean queue=true, boolean callMySelf=false, pist=null) {
	long startTime = now()
	long eventDelay = startTime - (long)event.date.getTime()
	Map msg = timer "Event processed successfully", null, -1
	Map tempRtData = getTemporaryRunTimeData(startTime)
	String evntName = (String)event.name
	if(tempRtData.logging) info "Received event [${event?.device?.label?: event?.device?.name?:location}].${(evntName == 'time') ? evntName + (event.recovery ? '/recovery' : '') : evntName} = ${event.value} with a delay of ${eventDelay}ms, canQueue: ${queue}, calledMyself: ${callMySelf}", tempRtData, 0

	state.lastExecuted = startTime
	boolean act = tempRtData.active
	boolean dis = tempRtData.disabled
	if(!act || dis) {
		String tstr = " active, aborting piston execution."
		if(!act) { // this is pause/resume piston
			msg.m = "Piston is not${tstr} (Paused)"
		}
		if(dis) {
			msg.m = "Kill switch is${tstr}"
		}
		checkLabel(tempRtData)
		if(tempRtData.logging) info msg, tempRtData
		updateLogs(tempRtData)
		return
	}

	boolean myPep = (boolean)tempRtData.pep
	if(myPep == null) {
		Map piston = recreatePiston()
		myPep = piston.o?.pep
		state.pep = myPep
		piston = null
	} else {
		tempRtData.piston = pist
	}

	tempRtData.lstarted = now()
	String appId = tempRtData.id
	boolean serializationOn = true // on / off switch
	boolean strictSync = false // this could be a setting
	boolean doSerialization = !myPep && (serializationOn || strictSync)
	String st0 = doSerialization ? appId : (String)null
	Map retSt = [:]
	if(st0 && !callMySelf) {
		retSt = lockOrQueueSemaphore(st0, event, queue, tempRtData)
		if(retSt.exitOut) {
			msg.m = "Event queued"
			if(tempRtData.logging) info msg, tempRtData
			updateLogs(tempRtData)
			return
		}
	}
	if(retSt.semaphoreDelay) {
		warn "Piston waited at a semaphore for ${retSt.semaphoreDelay}ms", temprtData
	}
	tempRtData.lended = now()

	if(evntName != 'time' && state.nextSchedule) unschedule(timeHandler)

	Map rtData = getRunTimeData(tempRtData, retSt)
	checkVersion(rtData)

	if((int)rtData.logging > 2) {
		long theend = now()
		long t0 = theend - startTime
		long t1 = (long)rtData.lended - (long)rtData.lstarted
		long t2 = (long)rtData.generatedIn
		long t3 = (long)rtData.pEnd - (long)rtData.pStart
		long missing = t0 - t1 - t2
		long t4 = (long)rtData.lended - startTime
		long t5 = theend - (long)rtData.lended
		debug "RunTime initialize > ${t0} LockT > ${t1}ms > rtDataT > ${t2}ms > pistonT > ${t3}ms (first state access ${missing} $t4 $t5)", rtData
	}
	if((int)rtData.logging > 1) trace "Runtime (${"$rtData".size()} bytes) successfully initialized in ${rtData.generatedIn}ms (${HEversion()})", rtData

// start of per event processing
	rtData.temp = [:]

	rtData.stats.timing = [
		t: startTime,
		d: eventDelay > 0 ? eventDelay : 0,
		l: now() - startTime
	]

	Map msg2 = timer "Execution stage complete.", null, -1
	boolean success = true
	boolean syncTime = false
	boolean firstTime = true
	if((evntName != 'time') && (evntName != 'wc_async_reply')) {
		if((int)rtData.logging) info "Execution stage started", rtData, 1
		success = executeEvent(rtData, event)
		syncTime = true
		firstTime = false
	}

	while (success && ((int)getPistonLimits.executionTime + (long)rtData.timestamp - now() > (int)getPistonLimits.schedule)) {
		List schedules = myPep ? atomicState.schedules : state.schedules
		if(!schedules || !schedules.size()) break
		long t = now()
		if(evntName == 'wc_async_reply') {
			event.schedule = schedules.sort{ it.t }.find{ it.d == event.value }
		} else {
			//anything less than .9 seconds in the future is considered due, we'll do some pause to sync with it
			//we're doing this because many times, the scheduler will run a job early, usually 0-1.5 seconds early...
			evntName = 'time'
			event = [date: event.date, device: location, name: evntName, value: t, schedule: schedules.sort{ (long)it.t }.find{ (long)it.t < t + (int)getPistonLimits.scheduleVariance }]
			syncTime = true
		}
		if(!event.schedule) break
		if(firstTime && !strictSync) syncTime = false

		schedules.remove(event.schedule)

		if(myPep) atomicState.schedules = schedules
		else state.schedules = schedules

		if(evntName == 'wc_async_reply') {
			//int responseCode = (int)cast(rtData, event.responseCode, 'integer')
			int responseCode = (int)event.responseCode
			boolean statOk = (responseCode >= 200) && (responseCode <= 299)
			if(event.value == 'httpRequest') {
				if(responseCode == 200 && event.schedule.stack) {
					event.schedule.stack.response = event.responseData
					//event.schedule.stack.json = event.jsonData
				}
				if(responseCode != 200 && event.schedule.stack) {
					event.schedule.stack.response = null
					//event.schedule.stack.json = [:]
				}
				//String contentType = (String)cast(rtData, event.contentType, 'string')
				String contentType = (String)event.contentType
				setSystemVariableValue(rtData, '$httpContentType', contentType)
			}
			if(event.value == 'httpRequest' || event.value == 'storeMedia') {
				if(statOk && event.setRtData) {
					for(item in event.setRtData) {
						rtData[(String)item.key] = item.value
					}
				}
				setSystemVariableValue(rtData, '$httpStatusCode', responseCode)
				setSystemVariableValue(rtData, '$httpStatusOk', statOk)
			}
			evntName = 'time'
			event.name = evntName
			event.value = t
		} else {
			if(event.schedule.d == 'httpRequest') {
				setSystemVariableValue(rtData, '$httpContentType', '')
				setSystemVariableValue(rtData, '$httpStatusCode', 408)
				setSystemVariableValue(rtData, '$httpStatusOk', false)
				if(event.schedule.stack) {
					event.schedule.stack.response = null
				}
				error "Timeout Error httpRequest", rtData
			}
			if(event.schedule.d == 'sendEmail') {
				error "Timeout Error sendEmail", rtData
			}
			if(event.schedule.d == 'storeMedia') {
				setSystemVariableValue(rtData, '$httpStatusCode', 408)
				setSystemVariableValue(rtData, '$httpStatusOk', false)
				error "Timeout Error storeMedia", rtData
			}
		}
		//if we have any other pending -3 events (device schedules), we cancel them all
		//if(event.schedule.i > 0) schedules.removeAll{ (it.s == event.schedule.s) && ( it.i == -3 ) }
		if(syncTime) {
			int delay = (long)event.schedule.t - now() - 30
			if(delay > 0 && delay < (int)getPistonLimits.scheduleVariance) {
				if((int)rtData.logging > 1) trace "Synchronizing scheduled event, waiting for ${delay}ms", rtData
				pause delay
			}
		}
		if(firstTime) {
			msg2 = timer "Execution stage complete.", null, -1
			if((int)rtData.logging) info "Execution stage started", rtData, 1
		}
		success = executeEvent(rtData, event)
		syncTime = true
		firstTime = false
	}

	rtData.stats.timing.e = now() - startTime
	if((int)rtData.logging) info msg2, rtData
	if(!success) msg.m = "Event processing failed"
	finalizeEvent(rtData, msg, success)

	if(rtData.currentEvent && rtData.logPExec) {
		String desc = 'webCore piston \'' + app.label + '\' was executed'
		sendLocationEvent(name: 'webCoRE', value: 'pistonExecuted', isStateChange: true, displayed: false, linkText: desc, descriptionText: desc, data: [
			id: appId,
			name: app.label,
			event: [date: rtData.currentEvent.date, delay: rtData.currentEvent.delay, duration: now() - rtData.currentEvent.date, device: "$rtData.event.device", name: (String)rtData.currentEvent.name, value: rtData.currentEvent.value, physical: rtData.currentEvent.physical, index: rtData.currentEvent.index],
			state: [old: rtData.state.old, new: rtData.state.new]
		])
	}

// process queued events
	while(doSerialization && !callMySelf) {
		String queueName = "aevQ" + "${app.id}"
		List evtQ = theQueuesFLD?."${queueName}"
		if(evtQ == null || evtQ == [] || evtQ.size() == 0) break
		List evtList = evtQ.sort { it.t }
		def theEvent = evtList.remove(0)
		theQueuesFLD."${queueName}" = evtList

		int qsize = evtQ.size()
		if(qsize > 8) { log.error "large queue size ${qsize}" }
		theEvent.date = new Date(theEvent.t)
		handleEvents(theEvent, false, true, rtData.piston)
	}

	if(doSerialization && rtData.semaphoreName && (theSemaphoresFLD."${rtData.semaphoreName}" <= rtData.semaphore)) {
		if((int)rtData.logging > 2) { log.debug "Released Lock and exiting" }
		theSemaphoresFLD."${rtData.semaphoreName}" = null
	} else if((int)rtData.logging > 2) { log.debug "Exiting" }
}

private boolean executeEvent(Map rtData, event) {
	if((boolean)rtData.eric) log "executeEvent", null, 1, null, "warn", true
	try {
		rtData = rtData ?: getRunTimeData()

		rtData.event = event
		rtData.previousEvent = state.lastEvent
		if(rtData.previousEvent == null) rtData.previousEvent = [:]
		String evntName = (String)event.name
		int index = 0
		if(event.jsonData) {
			def attribute = Attributes()[evntName]
			if(attribute && attribute.i && event.jsonData[(String)attribute.i]) { // .i is the attribute to lookup
				index = event.jsonData[(String)attribute.i]
			}
			if(!index) index = 1
		}
		def srcEvent = event && (evntName == 'time') && event.schedule && event.schedule.evt ? event.schedule.evt : null
		rtData.args = event ? ((evntName == 'time') && event.schedule && event.schedule.args && (event.schedule.args instanceof Map) ? event.schedule.args : (event.jsonData ?: [:])) : [:]
		if(event && (evntName == 'time') && event.schedule && event.schedule.stack) {
			setSystemVariableValue(rtData, '$index', event.schedule.stack.index)
			setSystemVariableValue(rtData, '$device', event.schedule.stack.device)
			setSystemVariableValue(rtData, '$devices', event.schedule.stack.devices)
			rtData.json = event.schedule.stack.json ?: [:]
			rtData.response = event.schedule.stack.response ?: [:]
			index = srcEvent.index ?: 0
// more to restore here?
		}
		def theDevice = srcEvent ? srcEvent.device : null
		def theDevice1 = theDevice == null && event.device ? event.device.id : null
		def theDevice2 = theDevice1 == null ? rtData.locationId : null
		def theFinalDevice = theDevice ?: (theDevice1 ? hashId(theDevice1 + (!isDeviceLocation(event.device) ? '' : '-L' )) : (theDevice2 ?: null))
		rtData.currentEvent = [
			date: event.date.getTime(),
			delay: rtData.stats?.timing?.d ?: 0,
			device: theFinalDevice,
			name: srcEvent ? srcEvent.name : evntName,
			value: srcEvent ? srcEvent.value : event.value,
			descriptionText: srcEvent ? srcEvent.descriptionText : event.descriptionText,
			unit: srcEvent ? srcEvent.unit : event.unit,
			physical: srcEvent ? srcEvent.physical : !!event.physical,
			index: index
		]
		state.lastEvent = rtData.currentEvent
		//previous variables
		rtData.conditionStateChanged = false
		rtData.pistonStateChanged = false
		rtData.fastForwardTo = null
		rtData.statementLevel = 0
		rtData.break = false
		rtData.resumed = false
		rtData.terminated = false
		if(evntName == 'time') {
			rtData.fastForwardTo = (int)event.schedule.i
		}
		setSystemVariableValue(rtData, '$state', rtData.state.new)
		setSystemVariableValue(rtData, '$previousEventDate', rtData.previousEvent?.date ?: now())
		setSystemVariableValue(rtData, '$previousEventDelay', rtData.previousEvent?.delay ?: 0)
		setSystemVariableValue(rtData, '$previousEventDevice', [rtData.previousEvent?.device])
		setSystemVariableValue(rtData, '$previousEventDeviceIndex', rtData.previousEvent?.index ?: 0)
		setSystemVariableValue(rtData, '$previousEventAttribute', rtData.previousEvent?.name ?: '')
		setSystemVariableValue(rtData, '$previousEventDescription', rtData.previousEvent?.descriptionText ?: '')
		setSystemVariableValue(rtData, '$previousEventValue', rtData.previousEvent?.value ?: '')
		setSystemVariableValue(rtData, '$previousEventUnit', rtData.previousEvent?.unit ?: '')
		setSystemVariableValue(rtData, '$previousEventDevicePhysical', !!rtData.previousEvent?.physical)

		setSystemVariableValue(rtData, '$currentEventDate', rtData.currentEvent.date ?: now())
		setSystemVariableValue(rtData, '$currentEventDelay', rtData.currentEvent.delay ?: 0)
		setSystemVariableValue(rtData, '$currentEventDevice', [rtData.currentEvent?.device])
		setSystemVariableValue(rtData, '$currentEventDeviceIndex', (rtData.currentEvent.index != '') && (rtData.currentEvent.index != null) ? rtData.currentEvent.index : 0)
		setSystemVariableValue(rtData, '$currentEventAttribute', rtData.currentEvent.name ?: '')
		setSystemVariableValue(rtData, '$currentEventDescription', rtData.currentEvent.descriptionText ?: '')
		setSystemVariableValue(rtData, '$currentEventValue', rtData.currentEvent.value ?: '')
		setSystemVariableValue(rtData, '$currentEventUnit', rtData.currentEvent.unit ?: '')
		setSystemVariableValue(rtData, '$currentEventDevicePhysical', !!rtData.currentEvent.physical)

		rtData.stack = [c: 0, s: 0, cs:[], ss:[]]
		boolean ended = false
		try {
			boolean allowed = !rtData.piston.r || !(rtData.piston.r.length) || evaluateConditions(rtData, rtData.piston, 'r', true)
			rtData.restricted = !rtData.piston.o?.aps && !allowed
			if(allowed || !!rtData.fastForwardTo) {
				if(rtData.fastForwardTo == -3) {
					//device related time schedules
					if(!rtData.restricted) {
						def data = event.schedule.d
						if(data && data.d && data.c) {
							//we have a device schedule, execute it
							def device = getDevice(rtData, data.d)
							if(device) {
								//executing scheduled physical command
								//used by fades, flashes, etc.
								executePhysicalCommand(rtData, device, (String)data.c, data.p, null, null, true)
							}
						}
					}
				} else {
					if(executeStatements(rtData, rtData.piston.s)) {
						ended = true
						tracePoint(rtData, 'end', 0, 0)
					}
					processSchedules rtData
				}
			} else {
				warn "Piston execution aborted due to restrictions in effect", rtData
				//we need to run through all to update stuff
				rtData.fastForwardTo = -9
				executeStatements(rtData, rtData.piston.s)
				ended = true
				tracePoint(rtData, 'end', 0, 0)
				processSchedules rtData
			}
			if(!ended) tracePoint(rtData, 'break', 0, 0)
		} catch (all) {
			error "An error occurred while executing the event: ", rtData, null, all
		}
		if((boolean)rtData.eric) log "executeEvent", null, -1, null, "warn", true
		return true
	} catch(all) {
		error "An error occurred within executeEvent: ", rtData, null, all
	}
	processSchedules rtData
	return false
}

private void finalizeEvent(Map rtData, Map initialMsg, boolean success = true) {
	long startTime = now()
	boolean myPep = (boolean)rtData.pep
	processSchedules(rtData, true)

	if(initialMsg) {
		if(success) {
			if((int)rtData.logging) info initialMsg, rtData
		} else {
			error initialMsg
		}
	}

	Map result = [:]
	String appId = rtData.id
	String myId = appId
	if(theCacheFLD && myId) {
		result = theCacheFLD."${myId}"
		if(result) theCacheFLD."${myId}".temp = rtData.temp
	}

	if(rtData.updateDevices) {
		updateDeviceList(rtData, rtData.devices*.value.id)
	}
	updateLogs(rtData) // we're done saving logs

	state.state = rtData.state

	rtData.trace.d = now() - (long)rtData.trace.t
	state.trace = rtData.trace

	//flush the new cache value
	for(item in rtData.newCache) rtData.cache[(String)item.key] = item.value

	//overwrite state, might have changed meanwhile
	if(myPep) {
		atomicState.cache = rtData.cache
		atomicState.store = rtData.store
	} else {
		state.cache = rtData.cache
		state.store = rtData.store
	}

//remove large stuff
	def data = [ "newCache", "mediaData", "weather", "logs", "trace", "devices", "systemVars", "localVars", "currentAction", "previousEvent", "json", "response", "cache", "store", "settings", "locationModeId", "locationId", "coreVersion", "hcoreVersion", "cancelations", "conditionStateChanged", "pistonStateChanged", "fastForwardTo", "resumed", "terminated", "instanceId", "wakingUp", "statementLevel", "args", "nfl", "temp" ]
	for (foo in data) {
		rtData.remove((String)foo)
	}
	if( !(rtData?.event instanceof com.hubitat.hub.domain.Event)) {
		if(rtData?.event?.responseData) rtData.event.responseData = [:]
		if(rtData?.event?.jsonData) rtData.event.jsonData = [:]
		if(rtData?.event?.setRtData) rtData.event.setRtData = [:]
		if(rtData?.event?.schedule?.stack) rtData.event.schedule.stack = [:]
	}

	if(rtData.gvCache || rtData.gvStoreCache) {
		unschedule(finishUIupdate)
		def tpiston = [:] + rtData.piston
		rtData.piston = [:]
		rtData.piston.z = tpiston.z

		if(rtData.gvCache) {
			Map vars = globalVarsFLD
			for(var in rtData.gvCache) {
				String varName = var.key
				if(varName && varName.startsWith('@') && (vars[varName]) && (var.value.v != vars[varName].v)) {
					globalVarsFLD[varName].v = var.value.v
				}
			}
		}

		parent.updateRunTimeData(rtData)

		rtData.piston = tpiston

		rtData.remove('gvCache')
		rtData.remove('gvStoreCache')
		//log.debug "Runtime (${"$rtData".size()} bytes)"
		//log.debug "${rtData}"
	} else {
// schedule to update UI for state

		def st = [:] + state.state
		st.remove('old')
		Map myRt = [
			id: (String)rtData.id,
			active: (boolean)rtData.active,
			category: rtData.category,
			stats: [
				nextSchedule: rtData.stats.nextSchedule // state.nextSchedule
			],
			piston: [
				z: rtData.piston.z
			],
			state: st
		]
		runIn( 2, finishUIupdate, [data: myRt])
	}

	//update graph data
	rtData.stats.timing.u = now() - startTime
	Map stats
	if(myPep) stats = atomicState.stats
	else stats = state.stats
	stats = stats ?: [:]

	stats.timing = stats.timing ?: []
	stats.timing.push(rtData.stats.timing)
	int t1 = settings.maxStats ?: 0
	if(!t1) t1 = (int)getPistonLimits.maxStats
	int t0 = (int)stats.timing.size()
	if(t0 > t1) {
		stats.timing = stats.timing[t0 - t1..t0 - 1]
	}
	if(myPep) atomicState.stats = stats
	else state.stats = stats
}

public void finishUIupdate(myRt) {
	parent.updateRunTimeData(myRt)
}

private void processSchedules(Map rtData, boolean scheduleJob = false) {
	//reschedule stuff
	//todo, override tasks, if any

	boolean myPep = (boolean)rtData.pep
	List schedules = myPep ? atomicState.schedules : state.schedules
	//if automatic states, we set it based on the autoNew - if any
	if(!rtData.piston.o?.mps) {
		rtData.state.new = rtData.state.autoNew ?: 'true'
	}

	rtData.state.old = rtData.state.new

	if(rtData.cancelations.all) {
		schedules.removeAll{ (int)it.i > 0 }
	}
	//cancel statements
	schedules.removeAll{ schedule -> !!rtData.cancelations.statements.find{ cancelation -> ((int)cancelation.id == (int)schedule.s) && (!cancelation.data || (cancelation.data == schedule.d)) }}
	//cancel on conditions
	for(cid in rtData.cancelations.conditions) {
		schedules.removeAll{ cid in it.cs }
	}
	//cancel on piston state change
	if((boolean)rtData.pistonStateChanged) {
		schedules.removeAll{ !!it.ps }
	}
	//rtData.pistonStateChanged = false
	rtData.cancelations = []
	schedules = (schedules + (rtData.schedules)) //.sort{ it.t }

	if(myPep) atomicState.schedules = schedules
	else state.schedules = schedules

	if(scheduleJob) {
		long nextT = 0L
		if(schedules.size()) {
			Map next = schedules.sort{ (long)it.t }[0]
			long t = ((long)next.t - now()) / 1000
			t = (t < 1 ? 1 : t)
			nextT = (long)next.t
			//rtData.stats.nextSchedule = (long)next.t
			if((int)rtData.logging) info "Setting up scheduled job for ${formatLocalTime(nextT)} (in ${t}s)" + (schedules.size() > 1 ? ', with ' + (schedules.size() - 1).toString() + ' more job' + (schedules.size() > 2 ? 's' : '') + ' pending' : ''), rtData
			int t1 = Math.round(t)
			runIn(t1, timeHandler, [data: next])
		} else {
			; //rtData.stats.nextSchedule = 0
		}
		rtData.stats.nextSchedule = nextT
	}
	state.nextSchedule = rtData.stats.nextSchedule
	rtData.schedules = []
}

private void updateLogs(Map rtData) {
	//we only save the logs if we got some
	if(!rtData || !rtData.logs || (rtData.logs.size() < 2)) return

	boolean myPep = (boolean)state.pep
	int t1 = settings.maxLogs ?: 0
	if(!t1 || t1 < 0) t1 = (int)getPistonLimits.maxLogs

	List t0
	if(myPep) t0 = atomicState.logs
	else t0 = state.logs
	List logs = (List) rtData.logs + t0
	while (t1 >= 0) {
		int t2 = logs.size()
		if(!t1 || !t2) { logs = []; break }
		if(t1 < (t2 - 1)) {
			logs = logs[0..t1]
			state.logs = logs
		}
		if(t1 > 5 && "$state".size() > 75000) {
			t1 -= Math.min(50, (t1/2) )
		} else { break }
	}
	if(myPep) atomicState.logs = logs
	else state.logs = logs
}

private boolean executeStatements(Map rtData, List statements, boolean async = false) {
	rtData.statementLevel = (int)rtData.statementLevel + 1
	for(Map statement in statements) {
		//only execute statements that are enabled
		if(!statement.di && !executeStatement(rtData, statement, async)) {
			//stop processing
			rtData.statementLevel = (int)rtData.statementLevel - 1
			return false
		}
	}
	//continue processing
	rtData.statementLevel = (int)rtData.statementLevel - 1
	return true
}

private boolean executeStatement(Map rtData, Map statement, boolean async = false) {
	//if rtData.fastForwardTo is a positive, non-zero number, we need to fast forward through all
	//branches until we find the task with an id equal to that number, then we play nicely after that
	if(!statement) return false
	if(!rtData.fastForwardTo) {
		switch ((String)statement.tep) {
		case 'c':
			if(!(boolean)rtData.conditionStateChanged) {
				if((int)rtData.logging > 2) debug "Skipping execution for statement #${(int)statement.$} because condition state did not change", rtData
				return
			}
			break
		case 'p':
			if(!(boolean)rtData.pistonStateChanged) {
				if((int)rtData.logging > 2) debug "Skipping execution for statement #${(int)statement.$} because piston state did not change", rtData
				return
			}
			break
		case 'b':
			if((!(boolean)rtData.conditionStateChanged) && (!(boolean)rtData.pistonStateChanged)) {
				if((int)rtData.logging > 2) debug "Skipping execution for statement #${(int)statement.$} because neither condition state nor piston state changed", rtData
				return
			}
			break
		}
	}
	if((boolean)rtData.eric) log "executeStatement", null, 1, null, "warn", true
	rtData.stack.ss.push(rtData.stack.s)
	rtData.stack.s = statement.$
	long t = now()
	boolean value = true
	def c = rtData.stack.c
	boolean stacked = true /* cancelable on condition change */
	if(stacked) {
		rtData.stack.cs.push(c)
	}
	boolean parentConditionStateChanged = (boolean)rtData.conditionStateChanged
	//def parentAsync = async
	def parentIndex = getVariable(rtData, '$index').v
	def parentDevice = getVariable(rtData, '$device').v
	boolean selfAsync = ((String)statement.a == "1") || ((String)statement.t == 'every') || ((String)statement.t == 'on')
	async = async || selfAsync
	boolean myPep = (boolean)rtData.pep
	boolean perform = false
	boolean repeat = true
	def index = null
	boolean allowed = !statement.r || !(statement.r.length) || evaluateConditions(rtData, statement, 'r', async)
	if(allowed || !!rtData.fastForwardTo) {
		while (repeat) {
			switch ((String)statement.t) {
			case 'every':
				//we override current condition so that child statements can cancel on it
				boolean ownEvent = (rtData.event && ((String)rtData.event.name == 'time') && rtData.event.schedule && ((int)rtData.event.schedule.s == (int)statement.$) && ((int)rtData.event.schedule.i < 0))
				List schedules = myPep ? atomicState.schedules : state.schedules
				if(ownEvent || !schedules.find{ (int)it.s == (int)statement.$ }) {
					//if the time has come for our timer, schedule the next timer
					//if no next time is found quick enough, a new schedule with i = -2 will be setup so that a new attempt can be made at a later time
					if(ownEvent) rtData.fastForwardTo = null
					scheduleTimer(rtData, statement, ownEvent ? (long)rtData.event.schedule.t : 0L)
				}
				rtData.stack.c = (int)statement.$
				if(ownEvent) rtData.fastForwardTo = null
				if(!!rtData.fastForwardTo || (ownEvent && allowed && !rtData.restricted)) {
					//we don't want to run this if there are piston restrictions in effect
					//we only execute the every if i = -1 (for rapid timers with large restrictions i.e. every second, but only on Mondays) we need to make sure we don't block execution while trying
					//to find the next execution scheduled time, so we give up after too many attempts and schedule a rerun with i = -2 to give us the chance to try again at that later time
					if(!!rtData.fastForwardTo || ((int)rtData.event.schedule.i == -1)) executeStatements(rtData, statement.s, true)
					//we always exit a timer, this only runs on its own schedule, nothing else is executed
					if(ownEvent) rtData.terminated = true
					value = false
					break
				}
				value = true
				break
			case 'repeat':
				//we override current condition so that child statements can cancel on it
				rtData.stack.c = (int)statement.$
				if(!executeStatements(rtData, statement.s, async)) {
					//stop processing
					value = false
					if(!rtData.fastForwardTo) break
				}
				value = true
				perform = (evaluateConditions(rtData, statement, 'c', async) == false)
				break
			case 'on':
				perform = false
				if(!rtData.fastForwardTo) {
					//look to see if any of the event matches
					String deviceId = (rtData.event.device) ? hashId(rtData.event.device.id) : null
					for (event in statement.c) {
						def operand = event.lo
						if(operand && (String)operand.t) {
							switch ((String)operand.t) {
							case 'p':
								if(!!deviceId && ((String)rtData.event.name == (String)operand.a) && !!operand.d && (deviceId in expandDeviceList(rtData, operand.d, true))) perform = true
								break
							case 'v':
								if((String)rtData.event.name == (String)operand.v) perform = true
								break
							case 'x':
								if((rtData.event.value == operand.x) && ((String)rtData.event.name == ( ((String)operand.x).startsWith('@@') ? '@@' + handle() : (String)rtData.instanceId) + ".${(String)operand.x}" )) perform = true
								break
							}
						}
						if(perform) break
					}
				}
				value = (!!rtData.fastForwardTo || perform) ? executeStatements(rtData, statement.s, async) : true
				break
			case 'if':
			case 'while':
				//check conditions for if and while
				perform = evaluateConditions(rtData, statement, 'c', async)
				//we override current condition so that child statements can cancel on it
				rtData.stack.c = (int)statement.$
				if(!rtData.fastForwardTo && (!rtData.piston.o?.mps) && ((String)statement.t == 'if') && ((int)rtData.statementLevel == 1) && perform) {
					//automatic piston state
					rtData.state.autoNew = 'true'
				}
				if(perform || !!rtData.fastForwardTo) {
					if((String)statement.t in ['if', 'while']) {
						if(!executeStatements(rtData, statement.s, async)) {
							//stop processing
							value = false
							if(!rtData.fastForwardTo) break
						}
						value = true
						if(!rtData.fastForwardTo) break
					}
				}
				if(!perform || !!rtData.fastForwardTo) {
					if((String)statement.t == 'if') {
						//look for else-ifs
						for (elseIf in statement.ei) {
							perform = evaluateConditions(rtData, elseIf, 'c', async)
							if(perform || !!rtData.fastForwardTo) {
								if(!executeStatements(rtData, elseIf.s, async)) {
									//stop processing
									value = false
									if(!rtData.fastForwardTo) break
								}
								value = true
								if(!rtData.fastForwardTo) break
							}
						}
						if(!rtData.fastForwardTo && (!rtData.piston.o?.mps) && ((int)rtData.statementLevel == 1)) {
							//automatic piston state
								rtData.state.autoNew = 'false'
						}
						if((!perform || !!rtData.fastForwardTo) && !executeStatements(rtData, statement.e, async)) {
							//stop processing
							value = false
							if(!rtData.fastForwardTo) break
						}
					}
				}
				break
			case 'for':
			case 'each':
				def devices = []
				double startValue = 0
				double endValue = 0
				double stepValue = 1
				if((String)statement.t == 'each') {
					def t0 = evaluateOperand(rtData, null, statement.lo).v
					devices = t0 ?: []
					endValue = devices.size() - 1
				} else {
					startValue = (double)evaluateScalarOperand(rtData, statement, statement.lo, null, 'decimal').v
					endValue = (double)evaluateScalarOperand(rtData, statement, statement.lo2, null, 'decimal').v
					double t0 = (double)evaluateScalarOperand(rtData, statement, statement.lo3, null, 'decimal').v
					stepValue = t0 ?: 1.0
				}
				String counterVariable = (String)getVariable(rtData, (String)statement.x).t != 'error' ? (String)statement.x : (String)null
				if(((startValue <= endValue) && (stepValue > 0)) || ((startValue >= endValue) && (stepValue < 0)) || !!rtData.fastForwardTo) {
					//initialize the for loop
					if(rtData.fastForwardTo) index = (double)cast(rtData, rtData.cache["f:${(int)statement.$}"], 'decimal')
					if(index == null) {
						index = (double)cast(rtData, startValue, 'decimal')
						rtData.cache["f:${(int)statement.$}"] = index
					}
					setSystemVariableValue(rtData, '$index', index)
					if(((String)statement.t == 'each') && !rtData.fastForward) setSystemVariableValue(rtData, '$device', (index < devices.size() ? [devices[(int)index]] : []))
					if(counterVariable && !rtData.fastForward) setVariable(rtData, counterVariable, ((String)statement.t == 'each') ? (index < devices.size() ? [devices[(int)index]] : []) : index)
					//do the loop
					perform = executeStatements(rtData, statement.s, async)
					if(!perform) {
						//stop processing
						value = false
						if(!!rtData.break) {
							//we reached a break, so we really want to continue execution outside of the switch
							value = true
							rtData.break = null
							//perform = false
						}
						break
					}
					//don't do the rest if we're fast forwarding
					if(!!rtData.fastForwardTo) break
					index = index + stepValue
					setSystemVariableValue(rtData, '$index', index)
					if(((String)statement.t == 'each') && !rtData.fastForward) setSystemVariableValue(rtData, '$device', (index < devices.size() ? [devices[(int)index]] : []))
					if(counterVariable && !rtData.fastForward) setVariable(rtData, counterVariable, ((String)statement.t == 'each') ? (index < devices.size() ? [devices[(int)index]] : []) : index)
					rtData.cache["f:${(int)statement.$}"] = index
					if(((stepValue > 0 ) && (index > endValue)) || ((stepValue < 0 ) && (index < endValue))) {
						perform = false
						break
					}
				}
				break
			case 'switch':
				Map lo = [operand: statement.lo, values: evaluateOperand(rtData, statement, statement.lo)]
				//go through all cases
				boolean found = false
				boolean implicitBreaks = ((String)statement.ctp == 'i')
				boolean fallThrough = !implicitBreaks
				perform = false
				if((int)rtData.logging > 2) debug "Evaluating switch with values $lo.values", rtData
				for (_case in statement.cs) {
					Map ro = [operand: _case.ro, values: evaluateOperand(rtData, _case, _case.ro)]
					Map ro2 = ((String)_case.t == 'r') ? [operand: _case.ro2, values: evaluateOperand(rtData, _case, _case.ro2, null, false, true)] : null
					perform = perform || evaluateComparison(rtData, ((String)_case.t == 'r' ? 'is_inside_of_range' : 'is'), lo, ro, ro2)
					found = found || perform
					if(perform || (found && fallThrough) || !!rtData.fastForwardTo) {
						def fastForwardTo = rtData.fastForwardTo
						if(!executeStatements(rtData, _case.s, async)) {
							//stop processing
							value = false
							if(!!rtData.break) {
								//we reached a break, so we really want to continue execution outside of the switch
								value = true
								found = true
								fallThrough = false
								rtData.break = null
							}
							if(!rtData.fastForwardTo) {
								break
							}
						}
						//if we determine that the fast forwarding ended during this execution, we assume found is true
						found = found || (fastForwardTo != rtData.fastForwardTo)
						value = true
						//if implicit breaks
						if(implicitBreaks && !rtData.fastForwardTo) {
							fallThrough = false
							break
						}
					}
				}
				if(statement.e && statement.e.length && (value || !!rtData.fastForwardTo) && (!found || fallThrough || !!rtData.fastForwardTo)) {
					//no case found, let's do the default
					if(!executeStatements(rtData, statement.e, async)) {
						//stop processing
						value = false
						if(!!rtData.break) {
							//we reached a break, so we really want to continue execution outside of the switch
							value = true
							rtData.break = null
						}
						if(!rtData.fastForwardTo) break
					}
				}
				break
			case 'action':
				value = executeAction(rtData, statement, async)
				break
			case 'do':
				value = executeStatements(rtData, statement.s, async)
				break
			case 'break':
				if(!rtData.fastForwardTo) {
					rtData.break = true
				}
				value = false
				break
			case 'exit':
				if(!rtData.fastForwardTo) {
					vcmd_setState(rtData, null, [(String)cast(rtData, evaluateOperand(rtData, null, statement.lo).v, 'string')])
					rtData.terminated = true
				}
				value = false
				break
			}
			//break the loop
			if(rtData.fastForwardTo || ((String)statement.t == 'if')) perform = false

			//is this statement a loop
			boolean loop = ((String)statement.t in ['while', 'repeat', 'for', 'each'])
			if(loop && !value && !!rtData.break) {
				//someone requested a break from the loop, we're doing it
				rtData.break = false
				//but we're allowing the rest to continue
				value = true
				perform = false
			}
			//do we repeat the loop?
			repeat = perform && value && loop && !rtData.fastForwardTo

			long overBy = checkForSlowdown(rtData)
			if(overBy > 0) {
				long delay = (int)getPistonLimits.taskShortDelay
				if(overBy > 10000) {
					delay = (int)getPistonLimits.taskLongDelay
				}
				String mstr = "executeStatement: Execution time exceeded by ${overBy}ms, "
				if(repeat && overBy > 240000) {
					error "${mstr}Terminating", rtData
					rtData.terminated = true
					repeat = false
				} else {
					doPause("${mstr}Waiting for ${delay}ms", delay, rtData)
				}
			}
		}
	}
	if(!rtData.fastForwardTo) {
		boolean tt0 = ((String)statement.t == 'every') ? true : false
		def t0 = tt0 ? rtData.schedules.find{ (int)it.s == (int)statement.$} : null
		List schedules
		if(tt0 && t0 == null) schedules = myPep ? atomicState.schedules : state.schedules
		def schedule = tt0 ? (t0 ?: schedules.find{ (int)it.s == (int)statement.$ }) : null
		if(schedule) {
			//timers need to show the remaining time
			tracePoint(rtData, "s:${statement.$}", now() - t, now() - (long)schedule.t)
		} else {
			tracePoint(rtData, "s:${statement.$}", now() - t, value)
		}
	}
	//if(statement.a == '1') {
		//when an async action requests the thread termination, we continue to execute the parent
		//when an async action terminates as a result of a time event, we exit completely
//		value = (rtData.event.name != 'time')
	//}
	if(selfAsync) {
		//if running in async mode, we return true (to continue execution)
		value = !rtData.resumed
		rtData.resumed = false
	}
	if(rtData.terminated) {
		value = false
	}
	//restore current condition
	rtData.stack.c = c
	if(stacked) {
		rtData.stack.cs.pop()
	}
	rtData.stack.s = rtData.stack.ss.pop()
	setSystemVariableValue(rtData, '$index', parentIndex)
	setSystemVariableValue(rtData, '$device', parentDevice)
	rtData.conditionStateChanged = parentConditionStateChanged
	if((boolean)rtData.eric) log "executeStatement", null, -1, null, "warn", true
	return value || !!rtData.fastForwardTo
}

private long checkForSlowdown(Map rtData) {
	//return how long over the time limit we are
	long overBy = 0
	long curRunTime = now() - (long)rtData.timestamp - (int)getPistonLimits.slTime
	if( curRunTime > overBy) {
		overBy = curRunTime
	}
	return overBy
}

private long doPause(String mstr, long delay, Map rtData) {
	long actDelay = 0L
	long t0 = now()
	if(!rtData.lastPause || ( (t0 - rtData.lastPause) > 1000)) {
		if((int)rtData.logging > 1) trace "${mstr}; lastPause: ${rtData.lastPause}", rtData
		rtData.lastPause = t0
		pause(delay)
		long t1 = now()
		actDelay = t1 - t0 // delay
		rtData.lastPause = t1
		long t2 = state.pauses ? (long)state.pauses : 0L
		state.pauses = t2 + 1
	}
	return actDelay
}

private boolean executeAction(Map rtData, Map statement, boolean async) {
	if((boolean)rtData.eric) log "executeAction", null, 1, null, "warn", true
	def parentDevicesVar = rtData.systemVars['$devices'].v
	//if override
	if(!rtData.fastForwardTo && ((String)statement.tsp != 'a')) {
		cancelStatementSchedules(rtData, (int)statement.$)
	}
	boolean result = true
	List deviceIds = expandDeviceList(rtData, statement.d)
	List devices = deviceIds.collect{ getDevice(rtData, it) }
	rtData.currentAction = statement
	for (Map task in statement.k) {
		if(task.$ && (int)task.$ == rtData.fastForwardTo) {
			//resuming a waiting task, we need to bring back the devices
			if(rtData.event && rtData.event.schedule && rtData.event.schedule.stack) {
				setSystemVariableValue(rtData, '$index', rtData.event.schedule.stack.index)
				setSystemVariableValue(rtData, '$device', rtData.event.schedule.stack.device)
				if(rtData.event.schedule.stack.devices instanceof List) {
					setSystemVariableValue(rtData, '$devices', rtData.event.schedule.stack.devices)
					deviceIds = rtData.event.schedule.stack.devices
					devices = deviceIds.collect{ getDevice(rtData, it) }
				}
			}
		}
		rtData.systemVars['$devices'].v = deviceIds
		result = executeTask(rtData, devices, statement, task, async)
		if(!result && !rtData.fastForwardTo) {
			break
		}
	}
	rtData.systemVars['$devices'].v = parentDevicesVar
	if((boolean)rtData.eric) log "executeAction", null, -1, null, "warn", true
	return result
}

private boolean executeTask(Map rtData, List devices, Map statement, Map task, boolean async) {
	//parse parameters
	def virtualDevice = devices.size() ? null : location
	long t = now()
	if(rtData.fastForwardTo) {
		if((int)task.$ == (int)rtData.fastForwardTo) {
			//finally found the resuming point, play nicely from hereon
			tracePoint(rtData, "t:${task.$}", now() - t, null)
			rtData.fastForwardTo = null
			//restore $device and $devices
			rtData.resumed = true
		}
		//we're not doing anything, we're fast forwarding...
		return true
	}
	if(task.m && (task.m instanceof List) && (task.m.size())) {
		if(!(rtData.locationModeId in task.m)) {
			if((int)rtData.logging > 2) debug "Skipping task ${task.$} because of mode restrictions", rtData
			return true
		}
	}
	if((boolean)rtData.eric) log "executeTask", null, 1, null, "warn", true
	List params = []
	for (Map param in task.p) {
		def p
		switch ((String)param.vt) {
		case 'variable':
			p = param.x instanceof List ? param.x : (param.x + (param.xi != null ? '[' + param.xi + ']' : ''))
			break
		default:
			def v = evaluateOperand(rtData, null, param)
			//if not selected, we want to return null
			String tt1 = (String)param.vt //?: (String)v.vt
			def t0 = v.v
			boolean match = (tt1 && ((tt1 == (String)v.t) || (t0 instanceof String && tt1 in ['string', 'enum', 'text'] ) ||
					(t0 instanceof Integer && tt1 == 'integer') ||
					(t0 instanceof Long && tt1 == 'long') ||
					(t0 instanceof Double && tt1 == 'decimal')  ||
					(t0 instanceof BigDecimal && tt1 == 'decimal')) )
			p = (t0 != null) ? (!match ? evaluateExpression(rtData, v, tt1).v : t0) : null
		}
		//ensure value type is successfuly passed through
		params.push p
	}

	//handle duplicate command "push" which was replaced with fake command "pushMomentary"
	//def override = rtData.commands.overrides.find { it.value.r == task.c }
	def override = CommandsOverrides.find { it.value.r == task.c }
	String command = override ? override.value.c : task.c

	//def vcmd = rtData.commands.virtual[command]
	def vcmd = VirtualCommands()[command]
	long delay = 0
	for (device in (virtualDevice ? [virtualDevice] : devices)) {
		if(!virtualDevice && device.hasCommand(command) && !(vcmd && vcmd.o /*virtual command overrides physical command*/)) {
			Map msg = timer "Executed [$device].${command}"
			try {
				delay = "cmd_${command}"(rtData, device, params)
			} catch(all) {
				executePhysicalCommand(rtData, device, command, params)
			}
			if((int)rtData.logging > 1) trace msg, rtData
		} else {
			if(vcmd) {
				delay = executeVirtualCommand(rtData, vcmd.a ? devices : device, command, params)
				//aggregate commands only run once, for all devices at the same time
				if(vcmd.a) break
			}
		}
	}
	//if we don't have to wait, we're home free

	//negative delays force us to reschedule, no sleeping on this one
	boolean reschedule = (delay < 0)
	delay = reschedule ? -delay : delay

	if(delay) {
		//get remaining piston time
		if(delay > (int)getPistonLimits.taskMaxDelay) {
			reschedule = true
		}
		if(reschedule || async) {
			//schedule a wake up
			long sec = delay/1000
			if((int)rtData.logging > 1) trace "Requesting a wake up for ${formatLocalTime(now() + delay)} (in ${sec}s)", rtData
			tracePoint(rtData, "t:${task.$}", now() - t, -delay)
			requestWakeUp(rtData, statement, task, delay, task.c)
			if((boolean)rtData.eric) log "executeTask", null, -1, null, "warn", true
			return false
		} else {
			if((int)rtData.logging > 1) trace "executeTask: Waiting for ${delay}ms", rtData
			pause(delay)
		}
	}
	tracePoint(rtData, "t:${task.$}", now() - t, delay)

	//get remaining piston time
	long overBy = checkForSlowdown(rtData)
	if(overBy > 0) {
		int mdelay = (int)getPistonLimits.taskShortDelay
		if(overBy > 10000L) {
			mdelay = (int)getPistonLimits.taskLongDelay
		}
		long actDelay = doPause("executeTask: Execution time exceeded by ${overBy}ms, Waiting for ${mdelay}ms", mdelay, rtData)
	}
	if((boolean)rtData.eric) log "executeTask", null, -1, null, "warn", true
	return true
}

private long executeVirtualCommand(Map rtData, devices, String command, List params) {
	Map msg = timer "Executed virtual command ${devices ? (devices instanceof List ? "$devices." : "[$devices].") : ""}${command}"
	long delay = 0
	try {
		delay = "vcmd_${command}"(rtData, devices, params)
		if((int)rtData.logging > 1) trace msg, rtData
	} catch(all) {
		msg.m = "Error executing virtual command ${devices instanceof List ? "$devices" : "[$devices]"}.${command}:"
		msg.e = all
		error msg, rtData
	}
	return delay
}

private executePhysicalCommand(Map rtData, device, String command, List params = [], delay = null, String scheduleDevice = (String)null, boolean disableCommandOptimization = false) {
	if(!!delay && !!scheduleDevice) {
		//delay without schedules is not supported in hubitat
		scheduleDevice = hashId(device.id)
		//we're using schedules instead
		def statement = rtData.currentAction
		def cs = [] + (((String)statement.tcp == 'b') || ((String)statement.tcp == 'c') ? (rtData.stack?.cs ?: []) : [])
		int ps = ((String)statement.tcp == 'b') || ((String)statement.tcp == 'p') ? 1 : 0
		cs.removeAll{ it == 0 }
		def schedule = [
			t: now() + delay,
			s: (int)statement.$,
			i: -3,
			cs: cs,
			ps: ps,
			d: [
				d: scheduleDevice,
				c: command,
				p: params
			]
		]
		rtData.schedules.push(schedule)
	} else {
	try {
		params = (params instanceof List) ? params : (params != null ? [params] : [])
		//cleanup the params so that SONOS works
		while (params.size() && (params[params.size()-1] == null)) params.pop()
		Map msg = timer ""
		boolean skip = false
		if(!rtData.piston.o?.dco && !disableCommandOptimization && !(command in ['setColorTemperature', 'setColor', 'setHue', 'setSaturation'])) {
			//def cmd = rtData.commands.physical[command]
			def cmd = PhysicalCommands().command
			if(cmd && cmd.a) {
				if(cmd.v && !params.size()) {
					//commands with no parameter that set an attribute to a preset value
					if(getDeviceAttributeValue(rtData, device, (String)cmd.a) == cmd.v) {
						skip = true
					}
				} else if(params.size() == 1) {
					if(getDeviceAttributeValue(rtData, device, (String)cmd.a) == params[0]) {
						skip = (command in ['setLevel', 'setInfraredLevel'] ? getDeviceAttributeValue(rtData, device, 'switch') == 'on' : true)
					}
				}
			}
		}
		//if we're skipping, we already have a message
		if(skip) {
			msg.m = "Skipped execution of physical command [${device.label ?: device.name}].$command($params) because it would make no change to the device."
		} else {
			if(params.size()) {
				if(delay) { //simulated in hubitat
					pause(delay)
					//device."$command"((params as Object[]) + [delay: delay])
					msg.m = "Executed physical command [${device.label ?: device.name}].$command($params, [delay: $delay])"
				} else {
					//device."$command"(params as Object[])
					msg.m = "Executed physical command [${device.label ?: device.name}].$command($params)"
				}
				device."$command"(params as Object[])
			} else {
				if(delay) { //simulated in hubitat
					pause(delay)
					//device."$command"([delay: delay])
					msg.m = "Executed physical command [${device.label ?: device.name}].$command([delay: $delay])"
				} else {
					//device."$command"()
					msg.m = "Executed physical command [${device.label ?: device.name}].$command()"
				}
				device."$command"()
			}
		}
		if((int)rtData.logging > 1) trace msg, rtData
	} catch(all) {
		error "Error while executing physical command $device.$command($params):", rtData, null, all
	}
	if(rtData.piston.o?.ced) {
		pause(rtData.piston.o.ced)
		if((int)rtData.logging > 1) trace "Injected a ${rtData.piston.o.ced}ms delay after [$device].$command(${params ? "$params" : ''})", rtData
	}
	}
}

private scheduleTimer(Map rtData, Map timer, long lastRun = 0) {
	//if already scheduled once during this run, don't do it again
	if(rtData.schedules.find{ (int)it.s == (int)timer.$ }) return
	if((boolean)rtData.eric) log "scheduleTimer", null, 1, null, "warn", true
	//complicated stuff follows...
	long t = now()
	String tinterval = "${evaluateOperand(rtData, null, timer.lo).v}"
	if(!(boolean)tinterval.isInteger()) {
		if((boolean)rtData.eric) log "scheduleTimer", null, -1, null, "warn", true
		return
	}
	int interval = (int)tinterval.toInteger()
	if(interval <= 0) {
		if((boolean)rtData.eric) log "scheduleTimer", null, -1, null, "warn", true
		return
	}
	String intervalUnit = (String)timer.lo.vt

	int level = 0
	switch(intervalUnit) {
		case 'ms': level = 1; break
		case 's':  level = 2; break
		case 'm':  level = 3; break
		case 'h':  level = 4; break
		case 'd':  level = 5; break
		case 'w':  level = 6; break
		case 'n':  level = 7; break
		case 'y':  level = 8; break
	}

	long delta = 0
	long time = 0
	switch (intervalUnit) {
		case 'ms': delta = 1; break
		case 's': delta = 1000; break
		case 'm': delta = 60000; break
		case 'h': delta = 3600000; break
	}

	if(!delta) {
		//let's get the offset
		time = (long)evaluateExpression(rtData, evaluateOperand(rtData, null, timer.lo2), 'datetime').v
		if((String)timer.lo2.t != 'c') {
			def offset = evaluateOperand(rtData, null, timer.lo3)
			time += (long)evaluateExpression(rtData, [t: 'duration', v: offset.v, vt: (String)offset.vt], 'long').v
		}
		//resulting time is in UTC
		if(!lastRun) {
			//first run, just adjust the time so we're in the future
			time = pushTimeAhead(time, now())
		}
	}
	delta = delta * interval
	boolean priorActivity = !!lastRun

	long rightNow = now()
	lastRun = lastRun ?: rightNow
	long nextSchedule = lastRun

	if(lastRun > rightNow) {
		//sometimes ST runs timers early, so we need to make sure we're at least in the near future
		rightNow = lastRun + 1
	}

	if(intervalUnit == 'h') {
		long min = (long)cast(rtData, timer.lo.om, 'long')
		nextSchedule = (long)3600000 * Math.floor(nextSchedule / 3600000) + (min * 60000)
	}

	//next date
	int cycles = 100
	while (cycles) {
		if(delta) {
			if(nextSchedule < (rightNow - delta)) {
				//we're behind, let's fast forward to where the next occurrence happens in the future
				long count = Math.floor((rightNow - nextSchedule) / delta)
				//if((int)rtData.logging > 2) debug "Timer fell behind by $count interval${count > 1 ? 's' : ''}, catching up...", rtData
				nextSchedule = nextSchedule + delta * count
			}
			nextSchedule = nextSchedule + delta
		} else {

			//advance one day if we're in the past
			time = pushTimeAhead(time, rightNow)
			// while (time < rightNow) time = time + 86400000
			long lastDay = Math.floor(nextSchedule / 86400000)
			long thisDay = Math.floor(time / 86400000)

			//the repeating interval is not necessarily constant
			switch (intervalUnit) {
				case 'd':
					if(priorActivity) {
						//add the required number of days
						nextSchedule = time + 86400000 * (interval - (thisDay - lastDay))
					} else {
						nextSchedule = time
					}
					break
				case 'w':
					//figure out the first day of the week matching the requirement
					long currentDay = new Date(time).day
					long requiredDay = (long)cast(rtData, timer.lo.odw, 'long')
					if(currentDay > requiredDay) requiredDay += 7
					//move to first matching day
					nextSchedule = time + 86400000 * (requiredDay - currentDay)
					if(nextSchedule < rightNow) {
						nextSchedule += 604800000 * interval
					}
					break
				case 'n':
				case 'y':
					//figure out the first day of the week matching the requirement
					int odm = timer.lo.odm.toInteger()
					def odw = timer.lo.odw
					int omy = intervalUnit == 'y' ? timer.lo.omy.toInteger() : 0
					int day = 0
					def date = new Date(time)
					int year = date.year
					int month = (intervalUnit == 'n' ? date.month : omy) + (priorActivity ? interval : ((nextSchedule < rightNow) ? 1 : 0)) * (intervalUnit == 'n' ? 1 : 12)
					if(month >= 12) {
						year += Math.floor(month / 12)
						month = month.mod(12)
					}
					date.setDate(1)
					date.setMonth(month)
					date.setYear(year)
					int lastDayOfMonth = (new Date(date.year, date.month + 1, 0)).date
					if(odw == 'd') {
						if(odm > 0) {
							day = (odm <= lastDayOfMonth) ? odm : 0
						} else {
							day = lastDayOfMonth + 1 + odm
							day = (day >= 1) ? day : 0
						}
					} else {
						odw = odw.toInteger()
						//find the nth week day of the month
						if(odm > 0) {
							//going forward
							int firstDayOfMonthDOW = (new Date(date.year, date.month, 1)).day
							//find the first matching day
							def firstMatch = 1 + odw - firstDayOfMonthDOW + (odw < firstDayOfMonthDOW ? 7 : 0)
							day = firstMatch + 7 * (odm - 1)
							day = (result <= lastDayOfMonth) ? day : 0
						} else {
							//going backwards
							int lastDayOfMonthDOW = (new Date(date.year, date.month + 1, 0)).day
							//find the first matching day
							def firstMatch = lastDayOfMonth + odw - lastDayOfMonthDOW - (odw > lastDayOfMonthDOW ? 7 : 0)
							day = firstMatch + 7 * (odm + 1)
							day = (day >= 1) ? day : 0
						}
					}
					if(day) {
						date.setDate(day)
						nextSchedule = date.time
					}
					break
			}
		}
		//check to see if it fits the restrictions
		if(nextSchedule >= rightNow) {
			long offset = checkTimeRestrictions(rtData, timer.lo, nextSchedule, level, interval)
			if(!offset) break
			if(offset > 0) nextSchedule += offset
		}
		time = nextSchedule
		priorActivity = true
		cycles -= 1
	}

	if(nextSchedule > lastRun) {
		rtData.schedules.removeAll{ (int)it.s == (int)timer.$ }
		requestWakeUp(rtData, timer, [$: -1], nextSchedule)
	}
	if((boolean)rtData.eric) log "scheduleTimer", null, -1, null, "warn", true
}

private long pushTimeAhead(long pastTime, long curTime) {
	long retTime = pastTime
	while (retTime < curTime) {
		long t0 = retTime + 86400000
		long t1 = t0 + ((int)location.timeZone.getOffset(retTime) - (int)location.timeZone.getOffset(t0))
		retTime = t1
	}
	return retTime
}

private void scheduleTimeCondition(Map rtData, Map condition) {
	if((boolean)rtData.eric) log "scheduleTimeConditions", null, 1, null, "warn", true
	//if already scheduled once during this run, don't do it again
	int t0 = (int)condition.$
	if(rtData.schedules.find{ ((int)it.s == t0) && ((int)it.i == 0) }) return
	//def comparison = rtData.comparisons.conditions[condition.co]
	def comparison = Comparisons().conditions[(String)condition.co]
	boolean trigger = false
	if(!comparison) {
		//comparison = rtData.comparisons.triggers[condition.co]
		comparison = Comparisons().triggers[(String)condition.co]
		if(!comparison) return
		trigger = true
	}
	cancelStatementSchedules(rtData, (int)condition.$)
	if(!comparison.p) return
	def tv1 = condition.ro && ((String)condition.ro.t != 'c') ? evaluateOperand(rtData, null, condition.to) : null

	def v1a = evaluateOperand(rtData, null, condition.ro)
	long tt0 = (long)v1a.v
	long v1b = (((String)v1a.t == 'time') && (tt0 < 86400000)) ? getTimeToday(tt0) : tt0
	long v1c = (tv1 ? (((String)tv1.t == 'integer' && (int)tv1.v == 0) ? 0L : (long)evaluateExpression(rtData, [t: 'duration', v: tv1.v, vt: (String)tv1.vt], 'long').v ) : 0L)
	long v1 = v1b + v1c

//	long v1 = (long)evaluateExpression(rtData, evaluateOperand(rtData, null, condition.ro), 'datetime').v + (tv1 ? (long)evaluateExpression(rtData, [t: 'duration', v: tv1.v, vt: (String)tv1.vt], 'long').v : 0)

	def tv2 = condition.ro2 && ((String)condition.ro2.t != 'c') && (comparison.p > 1) ? evaluateOperand(rtData, null, condition.to2) : null
	long v2 = trigger ? v1 : (((int)comparison.p > 1) ? ((long)evaluateExpression(rtData, evaluateOperand(rtData, null, condition.ro2, null, false, true), 'datetime').v + (tv2 ? evaluateExpression(rtData, [t: 'duration', v: tv2.v, vt: (String)tv2.vt]).v : 0)) : ((String)condition.lo.v == 'time' ? getMidnightTime() : v1))
	long n = now() + 2000
	if((String)condition.lo.v == 'time') {
		v1 = pushTimeAhead(v1, n)
		v2 = pushTimeAhead(v2, n)
	}
	//figure out the next time
	v1 = (v1 < n) ? v2 : v1
	v2 = (v2 < n) ? v1 : v2
	n = v1 < v2 ? v1 : v2
	if(n > now()) {
		if((int)rtData.logging > 2) debug "Requesting time schedule wake up at ${formatLocalTime(n)}", rtData
		requestWakeUp(rtData, condition, [$:0], n)
	}
	if((boolean)rtData.eric) log "scheduleTimeConditions", null, -1, null, "warn", true
}

private long checkTimeRestrictions(Map rtData, Map operand, long time, int level, int interval) {
	//returns 0 if restrictions are passed
	//returns a positive number as millisecond offset to apply to nextSchedule for fast forwarding
	//returns a negative number as a failed restriction with no fast forwarding offset suggestion

	List om = (level <= 2) && (operand.om instanceof List) && operand.om.size() ? operand.om : null
	List oh = (level <= 3) && (operand.oh instanceof List) && operand.oh.size() ? operand.oh : null
	List odw = (level <= 5) && (operand.odw instanceof List) && operand.odw.size() ? operand.odw : null
	List odm = (level <= 6) && (operand.odm instanceof List) && operand.odm.size() ? operand.odm : null
	List owm = (level <= 6) && !odm && (operand.owm instanceof List) && operand.owm.size() ? operand.owm : null
	List omy = (level <= 7) && (operand.omy instanceof List) && operand.omy.size() ? operand.omy : null


	if(!om && !oh && !odw && !odm && !owm && !omy) return 0
	def date = new Date(time)

	long result = -1
	//month restrictions
	if(omy && (omy.indexOf(date.month + 1) < 0)) {
		int month = (omy.sort{ it }.find{ it > date.month + 1 } ?: 12 + omy.sort{ it }[0]) - 1
		int year = date.year + (month >= 12 ? 1 : 0)
		month = (month >= 12 ? month - 12 : month)
		long ms = (new Date(year, month, 1)).time - time
		switch (level) {
		case 2: //by second
			result = interval * (Math.floor(ms / 1000 / interval) - 2) * 1000
			break
		case 3: //by minute
			result = interval * (Math.floor(ms / 60000 / interval) - 2) * 60000
			break
	}
		return (result > 0) ? result : -1
	}

	//week of month restrictions
	if(owm) {
		if(!((owm.indexOf(getWeekOfMonth(date)) >= 0) || (owm.indexOf(getWeekOfMonth(date, true)) >= 0))) {
		switch (level) {
		case 2: //by second
			result = interval * (Math.floor(((7 - date.day) * 86400 - date.hours * 3600 - date.minutes * 60) / interval) - 2) * 1000
			break
		case 3: //by minute
			result = interval * (Math.floor(((7 - date.day) * 1440 - date.hours * 60 - date.minutes) / interval) - 2) * 60000
			break
		}
		return (result > 0) ? result : -1
	}
	}

	//day of month restrictions
	if(odm) {
		if(odm.indexOf(date.date) < 0) {
			def lastDayOfMonth = (new Date(date.year, date.month + 1, 0)).date
			if(odm.find{ it < 1 }) {
				//we need to add the last days
				odm = [] + odm //copy the array
				if(odm.indexOf(-1) >= 0) odm.push(lastDayOfMonth)
				if(odm.indexOf(-2) >= 0) odm.push(lastDayOfMonth - 1)
				if(odm.indexOf(-3) >= 0) odm.push(lastDayOfMonth - 2)
				odm.removeAll{ it < 1 }
			}
			switch (level) {
			case 2: //by second
				result = interval * (Math.floor((((odm.sort{ it }.find{ it > date.date } ?: lastDayOfMonth + odm.sort{ it }[0]) - date.date) * 86400 - date.hours * 3600 - date.minutes * 60) / interval) - 2) * 1000
				break
			case 3: //by minute
				result = interval * (Math.floor((((odm.sort{ it }.find{ it > date.date } ?: lastDayOfMonth + odm.sort{ it }[0]) - date.date) * 1440 - date.hours * 60 - date.minutes) / interval) - 2) * 60000
				break
			}
			return (result > 0) ? result : -1
		}
	}

	//day of week restrictions
	if(odw && (odw.indexOf(date.day) < 0)) {
		switch (level) {
		case 2: //by second
			result = interval * (Math.floor((((odw.sort{ it }.find{ it > date.day } ?: 7 + odw.sort{ it }[0]) - date.day) * 86400 - date.hours * 3600 - date.minutes * 60) / interval) - 2) * 1000
			break
		case 3: //by minute
			result = interval * (Math.floor((((odw.sort{ it }.find{ it > date.day } ?: 7 + odw.sort{ it }[0]) - date.day) * 1440 - date.hours * 60 - date.minutes) / interval) - 2) * 60000
			break
		}
		return (result > 0) ? result : -1
	}

	//hour restrictions
	if(oh && (oh.indexOf(date.hours) < 0)) {
		switch (level) {
		case 2: //by second
			result = interval * (Math.floor((((oh.sort{ it }.find{ it > date.hours } ?: 24 + oh.sort{ it }[0]) - date.hours) * 3600 - date.minutes * 60) / interval) - 2) * 1000
			break
		case 3: //by minute
			result = interval * (Math.floor((((oh.sort{ it }.find{ it > date.hours } ?: 24 + oh.sort{ it }[0]) - date.hours) * 60 - date.minutes) / interval) - 2) * 60000
			break
		}
		return (result > 0) ? result : -1
	}

	//minute restrictions
	if(om && (om.indexOf(date.minutes) < 0)) {
		//get the next highest minute
	//suggest an offset to reach the next minute
		result = interval * (Math.floor(((om.sort{ it }.find{ it > date.minutes } ?: 60 + om.sort{ it }[0]) - date.minutes - 1) * 60 / interval) - 2) * 1000
		return (result > 0) ? result : -1
	}
	return 0
}


//return the number of occurrences of same day of week up until the date or from the end of the month if backwards, i.e. last Sunday is -1, second-last Sunday is -2
private int getWeekOfMonth(date = null, boolean backwards = false) {
	def day = date.date
	if(backwards) {
		int month = date.month
		int year = date.year
		def lastDayOfMonth = (new Date(year, month + 1, 0)).date
		return -(1 + Math.floor((lastDayOfMonth - day) / 7))
	} else {
		return 1 + Math.floor((day - 1) / 7) //1 based
	}
}


private void requestWakeUp(Map rtData, Map statement, Map task, long timeOrDelay, data = null) {
	long time = timeOrDelay > 9999999999 ? timeOrDelay : now() + timeOrDelay
	def cs = [] + (((String)statement.tcp == 'b') || ((String)statement.tcp == 'c') ? (rtData.stack?.cs ?: []) : [])
	int ps = ((String)statement.tcp == 'b') || ((String)statement.tcp == 'p') ? 1 : 0
	cs.removeAll{ it == 0 }
// state to save across a sleep
	Map schedule = [
		t: time,
		s: (int)statement.$,
		i: task?.$,
		cs: cs,
		ps: ps,
		d: data,
		evt: rtData.currentEvent,
		args: rtData.args,
		stack: [
			index: getVariable(rtData, '$index').v,
			device: getVariable(rtData, '$device').v,
			devices: getVariable(rtData, '$devices').v,
			json: rtData.json ?: [:],
			response: rtData.response ?: [:]
// what about previousEvent httpContentType  httpStatusCode  httpStatusOk  iftttStatusCode  iftttStatusOk  "\$mediaId" "\$mediaUrl"  "\$mediaType"   mediaData (big)
// currentEvent in case of httpRequest
		]
	]

/*
	if((int)rtData.logging > 2) {
		debug "requestWakeup(rtData, statement, task, timeOrDelay, data)", rtData
		debug "statement: ${statement}  statement.D: ${(int)statement.$}", rtData
		debug "task: ${task}  taskD: ${task.$}", rtData
		debug "cs: ${cs}  ps: ${ps} time: ${time} data: ${data}", rtData
	}
*/
	rtData.schedules.push(schedule)
}


private long do_setLevel(Map rtData, device, List params, String attr, val=null) {
	int arg = val != null ? (int)val : (int)params[0]
	String mstate = params.size() > 1 ? (String)params[1] : ""
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	int delay = params.size() > 2 ? (int)params[2] : 0
	executePhysicalCommand(rtData, device, attr, arg, delay)
	return 0
}

private long cmd_setLevel(Map rtData, device, List params) {
	return do_setLevel(rtData, device, params, 'setLevel')
}

private long cmd_setInfraredLevel(Map rtData, device, List params) {
	return do_setLevel(rtData, device, params, 'setInfraredLevel')
}

private long cmd_setHue(Map rtData, device, List params) {
	int hue = (int)cast(rtData, params[0] / 3.6, 'integer')
	return do_setLevel(rtData, device, params, 'setHue', hue)
}

private long cmd_setSaturation(Map rtData, device, List params) {
	return do_setLevel(rtData, device, params, 'setSaturation')
}

private long cmd_setColorTemperature(Map rtData, device, List params) {
	return do_setLevel(rtData, device, params, 'setColorTemperature')
}

private Map getColor(Map rtData, String colorValue) {
	def color = (colorValue == 'Random') ? getRandomColor(rtData) : getColorByName(rtData, colorValue)
	if(color) {
		color = [
			hex: color.rgb,
			hue: Math.round((int)color.h / 3.6),
			saturation: (int)color.s,
			level: (int)color.l
		]
	} else {
		color = hexToColor(colorValue)
		if(color) {
			color = [
				hex: color.hex,
				hue: Math.round((int)color.hue / 3.6),
				saturation: (int)color.saturation,
				level: (int)color.level
			]
		}
	}
	return color
}

private long cmd_setColor(Map rtData, device, List params) {
	def color = getColor(rtData, params[0])
	if(!color) {
		error "ERROR: Invalid color $params", rtData
		return 0
	}
	String mstate = params.size() > 1 ? (String)params[1] : ""
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	int delay = params.size() > 2 ? (int)params[2] : 0
	executePhysicalCommand(rtData, device, 'setColor', color, delay)
	return 0
}

private long cmd_setAdjustedColor(Map rtData, device, List params) {
	def color = getColor(rtData, (String)params[0])
	if(!color) {
		error "ERROR: Invalid color $params", rtData
		return 0
	}
	long duration = (long)cast(rtData, params[1], 'long')
	String mstate = params.size() > 2 ? (String)params[2] : ""
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	int delay = params.size() > 3 ? (int)params[3] : 0
	executePhysicalCommand(rtData, device, 'setAdjustedColor', [color, duration], delay)
	return 0
}

private long cmd_setAdjustedHSLColor(Map rtData, device, List params) {
	int hue = (int)cast(rtData, params[0] / 3.6, 'integer')
	int saturation = (int)params[1]
	int level = (int)params[2]
	def color = [
		hue: hue,
		saturation: saturation,
		level: level
	]
	long duration = (long)cast(rtData, params[3], 'long')
	String mstate = params.size() > 4 ? (String)params[4] : ""
	int delay = params.size() > 5 ? (int)params[5] : 0
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	executePhysicalCommand(rtData, device, 'setAdjustedColor', [color, duration], delay)
	return 0
}

private long cmd_setLoopDuration(Map rtData, device, List params) {
	int duration = (int)Math.round( (long)cast(rtData, params[0], 'long') / 1000)
	executePhysicalCommand(rtData, device, 'setLoopDuration', duration)
	return 0
}

private long cmd_setVideoLength(Map rtData, device, List params) {
	int duration = (int)Math.round( (long)cast(rtData, params[0], 'long') / 1000)
	executePhysicalCommand(rtData, device, 'setVideoLength', duration)
	return 0
}


private long vcmd_log(Map rtData, device, List params) {
	String command = params[0] ? (String)params[0] : ""
	String message = (String)params[1]
	log message, rtData, null, null, command.toLowerCase().trim(), true
	return 0
}

private long vcmd_setState(Map rtData, device, List params) {
	def value = params[0]
	if(rtData.piston.o?.mps) {
		rtData.state.new = value
		rtData.pistonStateChanged = (boolean)rtData.pistonStateChanged || (rtData.state.old != rtData.state.new)
		setSystemVariableValue(rtData, '$state', rtData.state.new)
	} else {
		error "Cannot set the piston state while in automatic mode. Please edit the piston settings to disable the automatic piston state if you want to manually control the state.", rtData
	}
	return 0
}

private long vcmd_setTileColor(Map rtData, device, List params) {
	int index = (int)cast(rtData, params[0], 'integer')
	if((index < 1) || (index > 16)) return 0
	rtData.state["c$index"] = (String)getColor(rtData, params[1])?.hex
	rtData.state["b$index"] = (String)getColor(rtData, params[2])?.hex
	rtData.state["f$index"] = !!params[3]
	return 0
}

private long vcmd_setTileTitle(Map rtData, device, List params) {
	return helper_setTile(rtData, 'i', params)
}

private long vcmd_setTileText(Map rtData, device, List params) {
	return helper_setTile(rtData, 't', params)
}

private long vcmd_setTileFooter(Map rtData, device, List params) {
	return helper_setTile(rtData, 'o', params)
}

private long vcmd_setTileOTitle(Map rtData, device, List params) {
	return helper_setTile(rtData, 'p', params)
}

private long helper_setTile(Map rtData, String typ, List params) {
	int index = (int)cast(rtData, params[0], 'integer')
	if((index < 1) || (index > 16)) return 0
	rtData.state["${typ}$index"] = (String)params[1]
	return 0
}

private long vcmd_setTile(Map rtData, device, List params) {
	int index = (int)cast(rtData, params[0], 'integer')
	if((index < 1) || (index > 16)) return 0
	rtData.state["i$index"] = (String)params[1]
	rtData.state["t$index"] = (String)params[2]
	rtData.state["o$index"] = (String)params[3]
	rtData.state["c$index"] = (String)getColor(rtData, params[4])?.hex
	rtData.state["b$index"] = (String)getColor(rtData, params[5])?.hex
	rtData.state["f$index"] = !!params[6]
	return 0
}

private long vcmd_clearTile(Map rtData, device, List params) {
	int index = (int)cast(rtData, params[0], 'integer')
	if((index < 1) || (index > 16)) return 0
	rtData.state["i$index"] = ''
	rtData.state["t$index"] = ''
	rtData.state["c$index"] = ''
	rtData.state["o$index"] = ''
	rtData.state["b$index"] = ''
	rtData.state["f$index"] = ''
	rtData.state["p$index"] = ''
	return 0
}

private long vcmd_setLocationMode(Map rtData, device, List params) {
	String  modeIdOrName = params[0]
	def mode = location.getModes()?.find{ (hashId(it.id) == modeIdOrName) || (it.name == modeIdOrName)}
	if(mode) {
		location.setMode(mode.name)
	} else {
		error "Error setting location mode. Mode '$modeIdOrName' does not exist.", rtData
	}
	return 0
}

private long vcmd_setAlarmSystemStatus(Map rtData, device, List params) {
	String statusIdOrName = params[0]
	def dev = VirtualDevices()['alarmSystemStatus']
	def options = dev?.ac
	def status = options?.find{ (it.key == statusIdOrName) || (it.value == statusIdOrName)}.collect{ [id: it.key, name: it.value] }

	if(status && status.size()) {
		sendLocationEvent(name: 'hsmSetArm', value: status[0].id)
	} else {
		error "Error setting HSM status. Status '$statusIdOrName' does not exist.", rtData
	}
	return 0
}

private long vcmd_sendEmail(Map rtData, device, List params) {
	def data = [
		i: rtData.id,
		n: app.label,
		t: params[0],
		s: params[1],
		m: params[2]
	]

	def requestParams = [
		uri: "https://api.webcore.co/email/send/${rtData.locationId}",
		query: null,
		headers: (auth ? [Authorization: auth] : [:]),
		requestContentType: "application/json",
		body: data
	]
	String msg = 'Unknown error'

	try {
		asynchttpPost('ahttpRequestHandler', requestParams, [command: 'sendEmail', em: data])
		return 24000
	} catch (all) {
		error "Error sending email to ${data.t}: $msg", rtData
	}

	return 0
}

private long vcmd_noop(Map rtData, device, List params) {
	return 0
}

private long vcmd_wait(Map rtData, device, List params) {
	return (long)cast(rtData, params[0], 'long')
}

private long vcmd_waitRandom(Map rtData, device, List params) {
	long min = (long)cast(rtData, params[0], 'long')
	long max = (long)cast(rtData, params[1], 'long')
	if(max < min) {
		long v = max
		max = min
		min = v
	}
	return min + (int)Math.round((max - min) * Math.random())
}

private long vcmd_waitForTime(Map rtData, device, List params) {
	long time
	time = (long)cast(rtData, (long)cast(rtData, params[0], 'time'), 'datetime', 'time')
	long rightNow = now()
	time = pushTimeAhead(time, rightNow)
	return time - rightNow
}

private long vcmd_waitForDateTime(Map rtData, device, List params) {
	long time = (long)cast(rtData, params[0], 'datetime')
	long rightNow = now()
	return (time > rightNow) ? time - rightNow : 0
}

private long vcmd_setSwitch(Map rtData, device, List params) {
	if( (boolean)cast(rtData, params[0], 'boolean')) {
		executePhysicalCommand(rtData, device, 'on')
	} else {
		executePhysicalCommand(rtData, device, 'off')
	}
	return 0
}

private long vcmd_toggle(Map rtData, device, List params) {
	if(getDeviceAttributeValue(rtData, device, 'switch') == 'off') {
		executePhysicalCommand(rtData, device, 'on')
	} else {
		executePhysicalCommand(rtData, device, 'off')
	}
	return 0
}

private long vcmd_toggleRandom(Map rtData, device, List params) {
	int probability = (int)cast(rtData, params.size() == 1 ? params[0] : 50, 'integer')
	if(probability <= 0) probability = 50
	if(Math.round(100 * Math.random()) <= probability) {
		executePhysicalCommand(rtData, device, 'on')
	} else {
		executePhysicalCommand(rtData, device, 'off')
	}
	return 0
}

private long vcmd_toggleLevel(Map rtData, device, List params) {
	int level = params[0]
	if(getDeviceAttributeValue(rtData, device, 'level') == level) {
		executePhysicalCommand(rtData, device, 'setLevel', 0)
	} else {
		executePhysicalCommand(rtData, device, 'setLevel', level)
	}
	return 0
}

private long do_adjustLevel(Map rtData, device, List params, String attr, String attr1, val = null, boolean big = false) {
	int arg = val != null ? (int)val : (int)cast(rtData, params[0], 'integer')
	String mstate = params.size() > 1 ? (String)params[1] : ""
	int delay = params.size() > 2 ? (int)params[2] : 0
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	arg = arg + (int)cast(rtData, getDeviceAttributeValue(rtData, device, attr), 'integer')
	int low = big ? 1000 : 0
	int hi = big ? 30000 : 100
	arg = (arg < low) ? low : ((arg > hi) ? hi : arg)
	executePhysicalCommand(rtData, device, attr1, arg, delay)
	return 0
}

private long vcmd_adjustLevel(Map rtData, device, List params) {
	return do_adjustLevel(rtData, device, params, 'level', 'setLevel')
}

private long vcmd_adjustInfraredLevel(Map rtData, device, List params) {
	return do_adjustLevel(rtData, device, params, 'infraredLevel', 'setInfraredLevel')
}

private long vcmd_adjustSaturation(Map rtData, device, List params) {
	return do_adjustLevel(rtData, device, params, 'saturation', 'setSaturation')
}

private long vcmd_adjustHue(Map rtData, device, List params) {
	int hue = (int)cast(rtData, params[0] / 3.6, 'integer')
	return do_adjustLevel(rtData, device, params, 'hue', 'setHue', hue)
}

private long vcmd_adjustColorTemperature(Map rtData, device, List params) {
	return do_adjustLevel(rtData, device, params, 'colorTemperature', 'setColorTemperature', null, true)
}

private long do_fadeLevel(Map rtData, device, params, String attr, String attr1, val = null, val1 = null, boolean big = false) {
	int startlevel
	int endLevel
	if(val == null) {
		startLevel = (params[0] != null) ? (int)cast(rtData, params[0], 'integer') : (int)cast(rtData, getDeviceAttributeValue(rtData, device, attr), 'integer')
		endLevel = (int)cast(rtData, params[1], 'integer')
	} else {
		startlevel = (int)val
		endLevel = (int)val1
	}
	long duration = (long)cast(rtData, params[2], 'long')
	String mstate = params.size() > 3 ? (String)params[3] : ""
//	int delay = params.size() > 4 ? (int)params[4] : 0
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	int low = big ? 1000 : 0
	int hi = big ? 30000 : 100
	startLevel = (startLevel < low) ? low : ((startLevel > hi) ? hi : startLevel)
	endLevel = (endLevel < low) ? low : ((endLevel > hi) ? hi : endLevel)
	return vcmd_internal_fade(rtData, device, attr1, startLevel, endLevel, duration)
}

private long vcmd_fadeLevel(Map rtData, device, List params) {
	return do_fadeLevel(rtData, device, params, 'level', 'setLevel')
}

private long vcmd_fadeInfraredLevel(Map rtData, device, List params) {
	return do_fadeLevel(rtData, device, params, 'infraredLevel', 'setInfraredLevel')
}

private long vcmd_fadeSaturation(Map rtData, device, List params) {
	return do_fadeLevel(rtData, device, params, 'saturation', 'setSaturation')
}

private long vcmd_fadeHue(Map rtData, device, List params) {
	int startLevel = (params[0] != null) ? (int)cast(rtData, (int)params[0] / 3.6, 'integer') : (int)cast(rtData, getDeviceAttributeValue(rtData, device, 'hue'), 'integer')
	int endLevel = (int)cast(rtData, (int)params[1] / 3.6, 'integer')
	return do_fadeLevel(rtData, device, params, 'hue', 'setHue', startLevel, endLevel)
}

private long vcmd_fadeColorTemperature(Map rtData, device, List params) {
	return do_fadeLevel(rtData, device, params, 'colorTemperature', 'setColorTemperature', null, null, true)
}

private long vcmd_internal_fade(Map rtData, device, String command, int startLevel, int endLevel, long duration) {
	long minInterval = 5000
	if(duration <= 5000) {
		minInterval = 500
	} else if(duration <= 10000) {
		minInterval = 1000
	} else if(duration <= 30000) {
		minInterval = 3000
	} else {
		minInterval = 5000
	}
	if((startLevel == endLevel) || (duration <= 500)) {
		//if the fade is too fast, or not changing anything, give it up and go to the end level directly
		executePhysicalCommand(rtData, device, command, endLevel)
		return 0
	}
	int delta = endLevel - startLevel
	//the max number of steps we can do
	int steps = delta > 0 ? delta : -delta
	//figure out the interval
	long interval = Math.round(duration / steps)
	if(interval < minInterval) {
		//intervals too small, adjust to do one change per 500ms
		steps = Math.floor(1.0 * duration / minInterval)
		interval = Math.round(1.0 * duration / steps)
	}
	String scheduleDevice = (duration > 10000) ? hashId(device.id) : (String)null
	int oldLevel = startLevel
	executePhysicalCommand(rtData, device, command, startLevel)
	for(int i = 1; i <= steps; i++) {
		int newLevel = Math.round(startLevel + delta * i / steps)
		if(oldLevel != newLevel) {
			executePhysicalCommand(rtData, device, command, newLevel, i * interval, scheduleDevice, true)
		}
		oldLevel = newLevel
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, command, endLevel, duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_emulatedFlash(Map rtData, device, List params) {
	vcmd_flash(rtData, device, params)
}

private long vcmd_flash(Map rtData, device, List params) {
	long onDuration = (long)cast(rtData, params[0], 'long')
	long offDuration = (long)cast(rtData, params[1], 'long')
	int cycles = (int)cast(rtData, params[2], 'integer')
	String mstate = params.size() > 3 ? (String)params[3] : ""
//	int delay = params.size() > 4 ? (int)params[4] : 0
	String currentState = getDeviceAttributeValue(rtData, device, 'switch')
	if(mstate && (currentState != mstate)) {
		return 0
	}
	long duration = (onDuration + offDuration) * cycles
	if(duration <= 500) {
		//if the flash is too fast, ignore it
		return 0
	}
	//initialize parameters
	String firstCommand = currentState == 'on' ? 'off' : 'on'
	long firstDuration = firstCommand == 'on' ? onDuration : offDuration
	String secondCommand = firstCommand == 'on' ? 'off' : 'on'
	long secondDuration = firstCommand == 'on' ? offDuration : onDuration
	String scheduleDevice = (duration > 10000) ? hashId(device.id) : (String)null
	long dur = 0
	for(int i = 1; i <= cycles; i++) {
		executePhysicalCommand(rtData, device, firstCommand, [], dur, scheduleDevice, true)
		dur += firstDuration
		executePhysicalCommand(rtData, device, secondCommand, [], dur, scheduleDevice, true)
		dur += secondDuration
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, currentState, [], duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_flashLevel(Map rtData, device, List params) {
	int level1 = (int)cast(rtData, params[0], 'integer')
	long duration1 = (long)cast(rtData, params[1], 'long')
	int level2 = (int)cast(rtData, params[2], 'integer')
	long duration2 = (long)cast(rtData, params[3], 'long')
	int cycles = (int)cast(rtData, params[4], 'integer')
	String mstate = params.size() > 5 ? (String)params[5] : ""
//	int delay = params.size() > 6 ? params[6] : 0
	String currentState = getDeviceAttributeValue(rtData, device, 'switch')
	if(mstate && (currentState != mstate)) {
		return 0
	}
	def currentLevel = getDeviceAttributeValue(rtData, device, 'level')
	long duration = (duration1 + duration2) * cycles
	if(duration <= 500) {
		//if the flash is too fast, ignore it
		return 0
	}
	//initialize parameters
	String scheduleDevice = (duration > 10000) ? hashId(device.id) : (String)null
	long dur = 0
	for(int i = 1; i <= cycles; i++) {
		executePhysicalCommand(rtData, device, 'setLevel', [level1], dur, scheduleDevice, true)
		dur += duration1
		executePhysicalCommand(rtData, device, 'setLevel', [level2], dur, scheduleDevice, true)
		dur += duration2
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, 'setLevel', [currentLevel], duration + 98, scheduleDevice, true)
	executePhysicalCommand(rtData, device, currentState, [], duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_flashColor(Map rtData, device, List params) {
	def color1 = getColor(rtData, params[0])
	long duration1 = (long)cast(rtData, params[1], 'long')
	def color2 = getColor(rtData, params[2])
	long duration2 = cast(rtData, params[3], 'long')
	int cycles = (int)cast(rtData, params[4], 'integer')
	String mstate = params.size() > 5 ? (String)params[5] : ""
//	int delay = params.size() > 6 ? params[6] : 0
	String currentState = getDeviceAttributeValue(rtData, device, 'switch')
	if(mstate && (currentState != mstate)) {
		return 0
	}
	long duration = (duration1 + duration2) * cycles
	if(duration <= 500) {
		//if the flash is too fast, ignore it
		return 0
	}
	//initialize parameters
	String scheduleDevice = (duration > 10000) ? hashId(device.id) : (String)null
	long dur = 0
	for(int i = 1; i <= cycles; i++) {
		executePhysicalCommand(rtData, device, 'setColor', [color1], dur, scheduleDevice, true)
		dur += duration1
		executePhysicalCommand(rtData, device, 'setColor', [color2], dur, scheduleDevice, true)
		dur += duration2
	}
	//for good measure, send a last command 100ms after the end of the interval
	executePhysicalCommand(rtData, device, currentState, [], duration + 99, scheduleDevice, true)
	return duration + 100
}

private long vcmd_sendNotification(Map rtData, device, List params) {
	def message = "Hubitat does not support sendNotification " + params[0]
	log message, rtData
	//sendNotificationEvent(message)
	return 0
}

private long vcmd_sendPushNotification(Map rtData, device, List params) {
	String message = params[0]
	if(!rtData.initPush) {
		rtData.pushDev = parent.getPushDev()
		rtData.initPush = true
	}
	def t0 = rtData.pushDev
	try {
		t0*.deviceNotification(message)
	} catch (all) {
		message = "Default push device not set properly in webCoRE " + params[0]
		error message, rtData
	}
	return 0
}

private long vcmd_sendSMSNotification(Map rtData, device, List params) {
	String message = (String)params[0]
	def phones = "${params[1]}".replace(" ", "").replace("-", "").replace("(", "").replace(")", "").tokenize(",;*|").unique()
	//def save = !!params[2]
	for(def phone in phones) {
		sendSms(phone, message) // Hubitat only allows 10 per day
	}
	String msg = "HE SMS notifications are being removed, please convert to a notification device " + params[0]
	warn msg, rtData
	return 0
}

private long vcmd_sendNotificationToContacts(Map rtData, device, List params) {
	// Contact Book has been disabled and we're falling back onto PUSH notifications, if the option is on
	if(!rtData.redirectContactBook) return 0
	def message = params[0]
	def save = !!params[2]
	return vcmd_sendPushNotification(rtData, devices, [message, save])
}

private Map parseVariableName(String name) {
	Map result = [
		name: name,
		index: null
	]
	if(name && !name.startsWith('$') && name.endsWith(']')) {
		def parts = name.replace(']', '').tokenize('[')
		if(parts.size() == 2) {
			result = [
				name: parts[0],
				index: parts[1]
			]
		}
	}
	return result
}

private long vcmd_setVariable(Map rtData, device, List params) {
	String name = (String)params[0]
	def value = params[1]
	if((boolean)rtData.eric) log "setVariable $name  $value", null, null, null, "warn", true
	setVariable(rtData, name, value)
	return 0
}

private long vcmd_executePiston(Map rtData, device, List params) {
	String selfId = rtData.id
	String pistonId = (String)params[0]
	def arguments = (params[1] instanceof List ? params[1] : params[1].toString().tokenize(',')).unique()
	boolean wait = (params.size() > 2) ? (boolean)cast(rtData, params[2], 'boolean') : false
	String description = "webCoRE: Piston $app.label requested execution of piston $pistonId"
	Map data = [:]
	for (String argument in arguments) {
		if(argument) data[argument] = getVariable(rtData, argument).v
	}
	if(wait) {
		wait = !!parent.executePiston(pistonId, data, selfId)
		pause(100)
	}
	if(!wait) {
		sendLocationEvent(name: pistonId, value: selfId, isStateChange: true, displayed: false, linkText: description, descriptionText: description, data: data)
	}
	return 0
}

private long vcmd_pausePiston(Map rtData, device, List params) {
	String selfId = rtData.id
	String pistonId = (String)params[0]
	if(!parent.pausePiston(pistonId)) {
		message = "Piston not found " + pistonId
		error message, rtData
	}
	return 0
}

private long vcmd_resumePiston(Map rtData, device, List params) {
	String selfId = rtData.id
	String pistonId = (String)params[0]
	if(!parent.resumePiston(pistonId)) {
		message = "Piston not found " + pistonId
		error message, rtData
	}
	return 0
}

private long vcmd_executeRule(Map rtData, device, List params) {
	def ruleId = params[0]
	String action = params[1]
	boolean wait = (params.size() > 2) ? (boolean)cast(rtData, params[2], 'boolean') : false
	def rules = RMUtils.getRuleList()
	def myRule = []
	rules.each {rule->
		def t0 = rule.find{ hashId(it.key) == ruleId }.collect {it.key}
		myRule += t0
	}

	if(myRule) {
		String ruleAction
		if(action == "Run") ruleAction = "runRuleAct"
		if(action == "Stop") ruleAction = "stopRuleAct"
		if(action == "Pause") ruleAction = "pauseRule"
		if(action == "Resume") ruleAction = "resumeRule"
		if(action == "Evaluate") ruleAction = "runRule"
		if(action == "Set Boolean True") ruleAction = "setRuleBooleanTrue"
		if(action == "Set Boolean False") ruleAction = "setRuleBooleanFalse"
		RMUtils.sendAction(myRule, ruleAction, app.label)
	} else {
		message = "Rule not found " + ruleId
		error message, rtData
	}
	return 0
}

private long vcmd_setHSLColor(Map rtData, device, List params) {
	int hue = (int)cast(rtData, params[0] / 3.6, 'integer')
	int saturation = params[1]
	int level = params[2]
	def color = [
		hue: hue,
		saturation: saturation,
		level: level
	]
	String mstate = params.size() > 3 ? params[3] : ""
	int delay = params.size() > 4 ? params[4] : 0
	if(mstate && (getDeviceAttributeValue(rtData, device, 'switch') != mstate)) {
		return 0
	}
	executePhysicalCommand(rtData, device, 'setColor', color, delay)
	return 0
}



private long vcmd_wolRequest(Map rtData, device, List params) {
	String mac = params[0]
	String secureCode = params[1]
	mac = mac.replace(":", "").replace("-", "").replace(".", "").replace(" ", "").toLowerCase()

	sendHubCommand(HubActionClass().newInstance(
		"wake on lan $mac",
		HubProtocolClass().LAN,
		null,
		secureCode ? [secureCode: secureCode] : [:]
	))
	return 0
}

private long vcmd_iftttMaker(Map rtData, device, List params) {
	if(rtData.settings == null) {
		error "no settings"
	}
	def key = (rtData.settings.ifttt_url ?: "").trim().replace('https://', '').replace('http://', '').replace('maker.ifttt.com/use/', '')
	if(!key) {
		error "Failed to send IFTTT event, because the IFTTT integration is not properly set up. Please visit Settings in your dashboard and configure the IFTTT integration.", rtData
		return 0
	}
	String event = params[0]
	def value1 = params.size() > 1 ? params[1] : ""
	def value2 = params.size() > 2 ? params[2] : ""
	def value3 = params.size() > 3 ? params[3] : ""
	def body = [:]
	if(value1) body.value1 = value1
	if(value2) body.value2 = value2
	if(value3) body.value3 = value3
	def requestParams = [
		uri: "https://maker.ifttt.com/trigger/${java.net.URLEncoder.encode(event, "UTF-8")}/with/key/" + key,
		requestContentType: "application/json",
		body: body
	]
	httpPost(requestParams) { response ->
		setSystemVariableValue(rtData, '$iftttStatusCode', response.status)
		setSystemVariableValue(rtData, '$iftttStatusOk', response.status == 200)
		return 0
	}
	return 0
}

private long vcmd_httpRequest(Map rtData, device, List params) {
	String uri = params[0].replace(" ", "%20")
	if(!uri) {
		error "Error executing external web request: no URI", rtData
		return 0
	}
	String method = params[1]
	boolean useQueryString = method == 'GET' || method == 'DELETE' || method == 'HEAD'
	String requestBodyType = params[2]
	def variables = params[3]
	def auth = null
	def requestBody = null
	String contentType = null
	if(params.size() == 5) {
		auth = params[4]
	} else if(params.size() == 7) {
		requestBody = params[4]
		contentType = params[5] ?: 'text/plain'
		auth = params[6]
	}
	String protocol = "https"
	def requestContentType = (method == "GET" || requestBodyType == "FORM") ? "application/x-www-form-urlencoded" : (requestBodyType == "JSON") ? "application/json" : contentType
	String userPart = ""
	def uriParts = uri.split("://").toList()
	if(uriParts.size() > 2) {
		warn "Invalid URI for web request: $uri", rtData
		return 0
	}
	if(uriParts.size() == 2) {
		//remove the httpX:// from the uri
		protocol = uriParts[0].toLowerCase()
		uri = uriParts[1]
	}
	//support for user:pass@IP
	if(uri.contains('@')) {
		def uriSubParts = uri.split('@').toList()
		userPart = uriSubParts[0] + '@'
		uri = uriSubParts[1]
	}
	def data = null
	if(requestBodyType == 'CUSTOM' && !useQueryString) {
		data = requestBody
	} else if(variables instanceof List) {
		for(String variable in variables.findAll{ !!it }) {
			data = data ?: [:]
			data[variable] = getVariable(rtData, variable).v
		}
	}
	try {
		def requestParams = [
			uri: "${protocol}://${userPart}${uri}",
			query: useQueryString ? data : null,
			headers: (auth ? ((auth.startsWith('{') && auth.endsWith('}')) ? ( new groovy.json.JsonSlurper().parseText( auth ) ) : [Authorization: auth]) : [:]),
			requestContentType: requestContentType,
			body: !useQueryString ? data : null
		]
		String func = ""
		switch(method) {
			case "GET":
				func = "asynchttpGet"
				break
			case "POST":
				func = "asynchttpPost"
				break
			case "PUT":
				func = "asynchttpPut"
				break
			case "DELETE":
				func = "asynchttpDelete"
				break
			case "HEAD":
				func = "asynchttpHead"
				break
		}
		if((int)rtData.logging > 2) debug "Sending ${func} web request to: $uri", rtData
		if(func) {
			"$func"('ahttpRequestHandler', requestParams, [command: 'httpRequest'])
			return 24000
		}
	} catch (all) {
		error "Error executing external web request: ", rtData, null, all
	}
	return 0
}

public void ahttpRequestHandler(resp, Map callbackData) {
	boolean binary = false
	def t0 = resp.getHeaders()
	def t1 = t0 && t0."Content-Type" ? t0."Content-Type" : null
	String mediaType = t1 ? t1.toLowerCase()?.tokenize(';')[0] : null
	switch (mediaType) {
		case 'image/jpeg':
		case 'image/png':
		case 'image/gif':
			binary = true
	}
	def data = [:]
	//def json = [:]
	def setRtData = [:]
	if(callbackdata?.command == 'sendEmail') {
		boolean success = false
		String msg = 'Unknown error'
		def em = callbackData?.em
		if(resp.status == 200)  {
			data = resp.getData()
			if(data) {
				if(data.result == 'OK') {
					success = true
				} else {
					msg = data.result.replace('ERROR ', '')
				}
			}
		}
		if(!success) {
			error "Error sending email to ${em?.t}: ${msg}"
		}
	} else if(callbackData?.command == 'httpRequest') {
		if((resp.status == 200) && resp.data && !binary) {
			try {
				data = resp.getData()
				//json = resp.getJson()
			} catch (all) {
				//json = [:]
			}
		} else {
			data = null
			if(resp.hasError()) {
				error "http Response Status: ${resp.status}  error Message: ${resp.getErrorMessage()}"
			}
			if(!resp.hasError() && resp.data && (resp.data instanceof java.io.ByteArrayInputStream)) {
				setRtData.mediaType = mediaType
				setRtData.mediaData = data?.getBytes()
			} else {
				setRtData.mediaType = null
				setRtData.mediaData = null
			}
			setRtData.mediaUrl = null
		}
	}

	handleEvents([date: new Date(), device: location, name: 'wc_async_reply', value: callbackData?.command, contentType: mediaType, responseData: data, /*jsonData: json, */ responseCode: resp.status, setRtData: setRtData])
}

private long vcmd_writeToFuelStream(Map rtData, device, List params) {
	String canister = params[0]
	String name = params[1]
	def data = params[2]
	def source = params[3]

	if(rtData.useLocalFuelStreams == null) {
		rtData.useLocalFuelStreams = parent.useLocalFuelStreams()
	}

	def req = [
		c: canister,
		n: name,
		s: source,
		d: data,
		i: (String)rtData.instanceId
	]

	if(rtData.useLocalFuelStreams) {
		parent.writeToFuelStream(req)
	} else {
		log.error "Fuel stream app is not installed. Install it to write to local fuel streams"
	}
	return 0
}

private long vcmd_storeMedia(Map rtData, device, List params) {
	if(!rtData.mediaData || !rtData.mediaType || !(rtData.mediaData) || (rtData.mediaData.size() <= 0)) {
		error "No media is available to store, operation aborted.", rtData
		return 0
	}
	String data = new String(rtData.mediaData, 'ISO_8859_1')
	def requestParams = [
		uri: "https://api-${rtData.region}-${rtData.instanceId[32]}.webcore.co:9247",
		path: "/media/store",
		headers: [
			'ST' : (String)rtData.instanceId,
			'media-type' : rtData.mediaType
		],
		body: data,
		requestContentType: rtData.mediaType
	]
	asynchttpPut(asyncHttpRequestHandler, requestParams, [command: 'storeMedia'])
	return 24000
}

public void asyncHttpRequestHandler(response, Map callbackData) {
	def mediaId
	def mediaUrl
	if(response.status == 200) {
		def data = response.getJson()
		if((data.result == 'OK') && (data.url)) {
			mediaId = data.id
			mediaUrl = data.url
		} else {
			if(data.message) {
				error "Error storing media item: $response.data.message"
			}
		}
	}
	handleEvents([date: new Date(), device: location, name: 'wc_async_reply', value: callbackData?.command, responseCode: response.status, setRtData: [mediaId: mediaId, mediaUrl: mediaUrl]])
}


private long vcmd_saveStateLocally(Map rtData, device, List params, boolean global = false) {
	def attributes = ((String)cast(rtData, params[0], 'string')).tokenize(',')
	String canister = (params.size() > 1 ? (String)cast(rtData, params[1], 'string') + ':' : '') + hashId(device.id) + ':'
	boolean overwrite = !(params.size() > 2 ? (boolean)cast(rtData, params[2], 'boolean') : false)
	for (String attr in attributes) {
		String n = canister + attr
		if(global && !rtData.initGStore) {
			rtData.globalStore = parent.getGStore()
			rtData.initGStore = true
		}
		if(overwrite || (global ? (rtData.globalStore[n] == null) : (rtData.store[n] == null))) {
			def value = getDeviceAttributeValue(rtData, device, attr)
			if(attr == 'hue') value = value * 3.6
			if(global) {
				rtData.globalStore[n] = value
				Map cache = rtData.gvStoreCache ?: [:]
				cache[n] = value
				rtData.gvStoreCache = cache
			} else {
				rtData.store[n] = value
			}
		}
	}
	return 0
}

private long vcmd_saveStateGlobally(Map rtData, device, List params) {
	return vcmd_saveStateLocally(rtData, device, params, true)
}


private long vcmd_loadStateLocally(Map rtData, device, List params, boolean global = false) {
	def attributes = ((String)cast(rtData, params[0], 'string')).tokenize(',')
	String canister = (params.size() > 1 ? (String)cast(rtData, params[1], 'string') + ':' : '') + hashId(device.id) + ':'
	boolean empty = params.size() > 2 ? (boolean)cast(rtData, params[2], 'boolean') : false
	for (String attr in attributes) {
		String n = canister + attr
		if(global && !rtData.initGStore) {
			rtData.globalStore = parent.getGStore()
			rtData.initGStore = true
		}
		def value = global ? rtData.globalStore[n] : rtData.store[n]
		if(attr == 'hue') value = (double)cast(rtData, value, 'decimal') / 3.6
		if(empty) {
			if(global) {
				rtData.globalStore.remove(n)
				Map cache = rtData.gvStoreCache ?: [:]
				cache[n] = null
				rtData.gvStoreCache = cache
			} else {
				rtData.store.remove(n)
			}
		}
		if(value == null) continue
		String exactCommand
		String fuzzyCommand
		//for (command in rtData.commands.physical) {
		for (command in PhysicalCommands()) {
			if((String)command.value.a == attr) {
				if(command.value.v == null) {
					fuzzyCommand = (String)command.key
				} else {
					if(command.value.v == value) {
						exactCommand = (String)command.key
						break
					}
				}
			}
		}
		String t0 = "Restoring attribute '$attr' to value '$value' using command"
		if(exactCommand) {
			if((int)rtData.logging > 2) debug "${t0} $exactCommand()", rtData
			executePhysicalCommand(rtData, device, exactCommand)
			continue
		}
		if(fuzzyCommand) {
			if((int)rtData.logging > 2) debug "${t0} $fuzzyCommand($value)", rtData
			executePhysicalCommand(rtData, device, fuzzyCommand, value)
			continue
		}
		warn "Could not find a command to set attribute '$attr' to value '$value'", rtData
	}
	return 0
}

private long vcmd_loadStateGlobally(Map rtData, device, List params) {
	return vcmd_loadStateLocally(rtData, device, params, true)
}

private long vcmd_parseJson(Map rtData, device, List params) {
	String data = params[0]
	try {
		if(data.startsWith('{') && data.endsWith('}')) {
			rtData.json = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(data)
		} else if(data.startsWith('[') && data.endsWith(']')) {
			rtData.json = (List) new groovy.json.JsonSlurper().parseText(data)
		} else {
			rtData.json = [:]
		}
	} catch (all) {
		error "Error parsing JSON data $data", rtData
	}
	return 0
}

private long vcmd_cancelTasks(Map rtData, device, List params) {
	rtData.cancelations.all = true
	return 0
}

private boolean evaluateFollowedByCondition(Map rtData, Map condition, String collection, boolean async, ladderUpdated) {
	boolean result = evaluateCondition(rtData, condition, collection, async)
}

private boolean evaluateConditions(Map rtData, Map conditions, String collection, boolean async) {
	if((boolean)rtData.eric) log "evaluateConditions", null, 1, null, "warn", true
	long t = now()
	Map msg = timer ''
	//override condition id
	def c = rtData.stack.c
	int myC = conditions.$ ?: 0
	rtData.stack.c = myC
	boolean not = (collection == 'c') ? !!conditions.n : !!conditions.rn
	String grouping = (collection == 'c') ? conditions.o : conditions.rop
	boolean value = (grouping == 'or' ? false : true)


	if((grouping == 'followed by') && (collection == 'c')) {
		if(!rtData.fastForwardTo || (rtData.fastForwardTo == myC)) {
			//we're dealing with a followed by condition
			int ladderIndex = (int)cast(rtData, rtData.cache["c:fbi:${myC}"], 'integer')
			long ladderUpdated = (long)cast(rtData, rtData.cache["c:fbt:${myC}"], 'datetime')
			int steps = conditions[collection] ? conditions[collection].size() : 0
			if(ladderIndex >= steps) {
				value = false
			} else {
				def condition = conditions[collection][ladderIndex]
				long duration = 0
				if(ladderIndex) {
					def tv = evaluateOperand(rtData, null, condition.wd)
					duration = (long)evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: (String)tv.vt], 'long').v
				}
				if(ladderUpdated && duration && (ladderUpdated + duration < now())) {
					//time has expired
					value = ((String)condition.wt == 'n')
					if(!value) {
						if((int)rtData.logging > 2) debug "Conditional ladder step failed due to a timeout", rtData
					}
				} else {
					value = evaluateCondition(rtData, condition, collection, async)
					if((String)condition.wt == 'n') {
						if(value) {
							value = false
						} else {
							value = null
						}
					}
					//we allow loose matches to work even if other events happen
					if(((String)condition.wt == 'l') && (!value)) value = null
				}
				if(value) {
					//successful step, move on
					ladderIndex += 1
					ladderUpdated = now()
					cancelStatementSchedules(rtData, myC)
					if((int)rtData.logging > 2) debug "Condition group #${myC} made progress up the ladder, currently at step $ladderIndex of $steps", rtData
					if(ladderIndex < steps) {
						//delay decision, there are more steps to go through
						value = null
						condition = conditions[collection][ladderIndex]
						def tv = evaluateOperand(rtData, null, condition.wd)
						duration = (long)evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: (String)tv.vt], 'long').v
						requestWakeUp(rtData, conditions, conditions, duration)
					}
				}
			}

			switch (value) {
			case null:
				//we need to exit time events set to work out the timeouts...
				if(rtData.fastForwardTo == myC) rtData.terminated = true
				break
			case true:
			case false:
				//ladder either collapsed or finished, reset data
				ladderIndex = 0
				ladderUpdated = 0
				cancelStatementSchedules(rtData, myC)
				break
			}
			if(rtData.fastForwardTo == myC) rtData.fastForwardTo = null
			rtData.cache["c:fbi:${myC}"] = ladderIndex
			rtData.cache["c:fbt:${myC}"] = ladderUpdated
		}
	} else {
		for(condition in conditions[collection]) {
			boolean res = evaluateCondition(rtData, condition, collection, async)
			value = (grouping == 'or') ? value || res : value && res
			//conditions optimizations go here
			if(!rtData.fastForwardTo && (!rtData.piston.o?.cto) && ((value && (grouping == 'or')) || (!value && (grouping == 'and')))) break
		}
	}
	boolean result
	if(value != null) {
		result = not ? !value : value
	}
	if(value != null && myC != 0) {
		if(!rtData.fastForwardTo) tracePoint(rtData, "c:${myC}", now() - t, result)
		boolean oldResult = !!rtData.cache["c:${myC}"]
		rtData.conditionStateChanged = (oldResult != result)
		if((boolean)rtData.conditionStateChanged) {
			//condition change, perform TCP
			cancelConditionSchedules(rtData, myC)
		}
		rtData.cache["c:${myC}"] = result
		//true/false actions
		if(collection == 'c') {
			if((result || rtData.fastForwardTo) && conditions.ts && conditions.ts.length) executeStatements(rtData, conditions.ts, async)
			if((!result || rtData.fastForwardTo) && conditions.fs && conditions.fs.length) executeStatements(rtData, conditions.fs, async)
		}
		if(!rtData.fastForwardTo) {
			msg.m = "Condition group #${myC} evaluated $result (state ${(boolean)rtData.conditionStateChanged ? 'changed' : 'did not change'})"
			if((int)rtData.logging > 2) debug msg, rtData
		}
	}
	//restore condition id
	rtData.stack.c = c
	if((boolean)rtData.eric) log "evaluateConditions", null, -1, null, "warn", true
	return result
}

private evaluateOperand(Map rtData, Map node, Map operand, index = null, boolean trigger = false, boolean nextMidnight = false) {
//ERS
	if((boolean)rtData.eric) log "evaluateOperand $operand", null, 1, null, "warn", true
	def values = []
	//older pistons don't have the 'to' operand (time offset), we're simulating an empty one
	if(!operand) operand = [t: 'c']
	String ovt = (String)operand.vt
	switch ((String)operand.t) {
	case '': //optional, nothing selected
		values = [[i: "${node?.$}:$index:0", v: [t: ovt, v: null]]]
		break
	case "p": //physical device
		//def j = 0
		//def attribute = rtData.attributes[operand.a]
		def attribute = Attributes()[(String)operand.a]
		for(String deviceId in expandDeviceList(rtData, operand.d)) {
			def value = [i: "${deviceId}:${operand.a}", v:getDeviceAttribute(rtData, deviceId, (String)operand.a, operand.i, trigger) + (ovt ? [vt: ovt] : [:]) + (attribute && attribute.p ? [p: operand.p] : [:])]
			updateCache(rtData, value)
			values.push(value)
			//j++
		}
		if((values.size() > 1) && !((String)operand.g in ['any', 'all'])) {
			//if we have multiple values and a grouping other than any or all we need to apply that function
			try {
				values = [[i: "${node?.$}:$index:0", v:(Map)"func_${operand.g}"(rtData, values*.v) + (ovt ? [vt: ovt] : [:])]]
			} catch(all) {
				error "Error applying grouping method ${operand.g}", rtData
			}
		}
		break
	case 'd': //devices
		def deviceIds = []
		for (d in expandDeviceList(rtData, operand.d)) {
			if(getDevice(rtData, d)) deviceIds.push(d)
		}
		values = [[i: "${node?.$}:d", v:[t: 'device', v: deviceIds.unique()]]]
		break
	case 'v': //virtual devices
		String rEN = rtData.event.name
		switch ((String)operand.v) {
		case 'mode':
			//values = [[i: "${node?.$}:v", v:getDeviceAttribute(rtData, rtData.locationId, (String)operand.v)]]
			//break
		case 'alarmSystemStatus':
			//values = [[i: "${node?.$}:v", v:[t: 'string', v: (rEN == 'hsmStatus' ? rtData.event.value : null)]]]
			values = [[i: "${node?.$}:v", v:getDeviceAttribute(rtData, rtData.locationId, (String)operand.v)]]
			break
		case 'alarmSystemAlert':
			values = [[i: "${node?.$}:v", v:[t: 'string', v: (rEN == 'hsmAlert' ? rtData.event.value : null)]]]
			break
		case 'alarmSystemEvent':
			values = [[i: "${node?.$}:v", v:[t: 'string', v: (rEN == 'hsmSetArm' ? rtData.event.value : null)]]]
			break
		case 'alarmSystemRule':
			values = [[i: "${node?.$}:v", v:[t: 'string', v: (rEN == 'hsmRules' ? rtData.event.value : null)]]]
			break
		case 'powerSource':
			values = [[i: "${node?.$}:v", v:[t: 'enum', v:rtData.powerSource]]]
			break
		case 'time':
		case 'date':
		case 'datetime':
			values = [[i: "${node?.$}:v", v:[t: (String)operand.v, v: (long)cast(rtData, now(), operand.v, 'long')]]]
			break
		case 'routine':
			values = [[i: "${node?.$}:v", v:[t: 'string', v: (rEN == 'routineExecuted' ? hashId(rtData.event.value) : null)]]]
			break
		case 'tile':
			values = [[i: "${node?.$}:v", v:[t: 'string', v: (rtData.event.name == operand.v ? rtData.event.value : null)]]]
			break
		case 'ifttt':
			values = [[i: "${node?.$}:v", v:[t: 'string', v: (rEN == ('ifttt.' + rtData.event.value) /* operand.v*/ ? rtData.event.value : null)]]]
//log.debug "ifttt evaluate operand values: ${values},  operand: ${operand.v}   rtData.event: ${rtData.event.name} ${rtData.event.value}"
			break
		case 'email':
			values = [[i: "${node?.$}:v", v:[t: 'email', v: (rEN == ('email.' + rtData.event.value) /* operand.v*/ ? rtData.event.value : null)]]]
			break
		}
		break
	case "s": //preset
		boolean time = false
		switch (ovt) {
		case 'time':
			time = true
		case 'datetime':
			long v = 0
			switch ((String)operand.s) {
			case 'midnight': v = nextMidnight ? getNextMidnightTime() : getMidnightTime(); break
			case 'sunrise': v = adjustPreset(rtData, "Sunrise", nextMidnight); break
			case 'noon': v = adjustPreset(rtData, "Noon", nextMidnight); break
			case 'sunset': v = adjustPreset(rtData, "Sunset", nextMidnight); break
			}
			if(time) v = (long)cast(rtData, v, ovt, 'datetime')
			values = [[i: "${node?.$}:$index:0", v:[t:ovt, v:v]]]
			break
		default:
			values = [[i: "${node?.$}:$index:0", v:[t:ovt, v:operand.s]]]
			break
		}
		break
	case "x": //variable
		if((ovt == 'device') && (operand.x instanceof List)) {
			//we could have multiple devices selected
			def sum = []
			for (String x in operand.x) {
				def var = getVariable(rtData, x)
				if(var.v instanceof List) {
					sum += var.v
				} else {
					sum.push(var.v)
				}
				values = [[i: "${node?.$}:$index:0", v:[t: 'device', v: sum] + (ovt ? [vt: ovt] : [:])]]
			}
		} else {
			values = [[i: "${node?.$}:$index:0", v:getVariable(rtData, (String)operand.x + (operand.xi != null ? '[' + operand.xi + ']' : '')) + (ovt ? [vt: ovt] : [:])]]
		}
		break
	case "c": //constant
		switch (ovt) {
		case 'time':
			long offset = (operand.c instanceof Integer) ? operand.c : (int)cast(rtData, operand.c, 'integer')
			values = [[i: "${node?.$}:$index:0", v: [t: 'time', v:(offset % 1440) * 60000]]]
			break
		case 'date':
		case 'datetime':
			values = [[i: "${node?.$}:$index:0", v: [t: ovt, v:operand.c]]]
			break
		}
		if(values.size()) break
	case "e": //expression
		values = [[i: "${node?.$}:$index:0", v: [:] + evaluateExpression(rtData, operand.exp) + (ovt ? [vt: ovt] : [:]) ]]
//		def outV = ovt ? evaluateExpression(rtData, operand.exp, ovt) : evaluateExpression(rtData, operand.exp)
//		values = [[i: "${node?.$}:$index:0", v: [:] + outV ]]
		break
	case "u": //expression
		values = [[i: "${node?.$}:$index:0", v: getArgument(rtData, operand.u)]]
		break
	}
	if((boolean)rtData.eric) log "evaluateOperand $operand", null, -1, null, "warn", true
	if(!node) {
		if(values.length) return values[0].v
		return [t: 'dynamic', v: null]
	}
	return values
}

private long adjustPreset(Map rtData, String ttyp, nextMidnight) {
	long t2 = (long)"get${ttyp}Time"(rtData)
	long tnow = now()
	if(tnow < t2) return t2
// this deals with both DST skew and sunrise/sunset skews
	long t0 = (long)"getNext${ttyp}Time"(rtData)
	long t1 = t0 - 86400000
	long delta = (int)location.timeZone.getOffset(t1) - (int)location.timeZone.getOffset(t0)
	long t4 = t1 + delta
	if(tnow > t4) return t4
	return t2
}

private evaluateScalarOperand(Map rtData, node, operand, index = null, String dataType = 'string') {
	def value = evaluateOperand(rtData, null, operand, index)
	return [t: dataType, v: cast(rtData, (value ? value.v: ''), dataType)]
}

private boolean evaluateCondition(Map rtData, Map condition, String collection, boolean async) {
	if((boolean)rtData.eric) log "evaluateCondition $condition", null, 1, null, "warn", true
	long t = now()
	Map msg = timer ''
	//override condition id
	def c = rtData.stack.c
	rtData.stack.c = condition.$
	boolean not = false
	boolean oldResult = !!rtData.cache["c:${condition.$}"]
	boolean result = false
	if((String)condition.t == 'group') {
		def tt1 = evaluateConditions(rtData, condition, collection, async)
		if((boolean)rtData.eric) log "evaluateCondition $condition", null, -1, null, "warn", true
		return tt1
	} else {
		not = !!condition.n
		def comparison = Comparisons().triggers[condition.co]
		boolean trigger = !!comparison
		if(!comparison) comparison = Comparisons().conditions[condition.co]
		rtData.wakingUp = ((String)rtData.event.name == 'time') && (!!rtData.event.schedule) && ((int)rtData.event.schedule.s == (int)condition.$)
		if(rtData.fastForwardTo || comparison) {
			if(!rtData.fastForwardTo || ((int)rtData.fastForwardTo == -9 /*initial run*/)) {
				int paramCount = comparison.p ?: 0
				Map lo = null
				Map ro = null
				Map ro2 = null
				for(int i = 0; i <= paramCount; i++) {
					def operand = (i == 0 ? condition.lo : (i == 1 ? condition.ro : condition.ro2))
					//parse the operand
					def values = evaluateOperand(rtData, condition, operand, i, trigger)
					switch (i) {
					case 0:
						lo = [operand: operand, values: values]
						break
					case 1:
						ro = [operand: operand, values: values]
						break
					case 2:
						ro2 = [operand: operand, values: values]
						break
					}
				}

				//we now have all the operands, their values, and the comparison, let's get to work
				Map options = [
					//we ask for matching/non-matching devices if the user requested it or if the trigger is timed
					//setting matches to true will force the condition group to evaluate all members (disables evaluation optimizations)
					matches: lo.operand.dm || lo.operand.dn || (trigger && comparison.t),
					forceAll: (trigger && comparison.t)
				]
				Map to = (comparison.t || (ro && ((String)lo.operand.t == 'v') && ((String)lo.operand.v == 'time') && ((String)ro.operand.t != 'c'))) && condition.to ? [operand: condition.to, values: evaluateOperand(rtData, null, condition.to)] : null
				Map to2 = ro2 && ((String)lo.operand.t == 'v') && ((String)lo.operand.v == 'time') && ((String)ro2.operand.t != 'c') && condition.to2 ? [operand: condition.to2, values: evaluateOperand(rtData, null, condition.to2)] : null
				result = evaluateComparison(rtData, (String)condition.co, lo, ro, ro2, to, to2, options)
				//save new values to cache
				if(lo) for (value in lo.values) updateCache(rtData, value)
				if(ro) for (value in ro.values) updateCache(rtData, value)
				if(ro2) for (value in ro2.values) updateCache(rtData, value)
				if(rtData.fastForwardTo == null) tracePoint(rtData, "c:${condition.$}", now() - t, result)
				if(lo.operand.dm && options.devices) setVariable(rtData, lo.operand.dm, options.devices?.matched ?: [])
				if(lo.operand.dn && options.devices) setVariable(rtData, lo.operand.dn, options.devices?.unmatched ?: [])
				//do the stay logic here
				if(trigger && comparison.t && (rtData.fastForwardTo == null)) {
					//timed trigger
					if(to) {
						def tvalue = to && to.operand && to.values ? to.values + [f: to.operand.f] : null
						if(tvalue) {
							long delay = (long)evaluateExpression(rtData, [t: 'duration', v: tvalue.v, vt: (String)tvalue.vt], 'long').v
							if((lo.operand.t == 'p') && (lo.operand.g == 'any') && lo.values.size() > 1) {
								List schedules = (boolean)rtData.pep ? atomicState.schedules : state.schedules
								for (value in lo.values) {
									def dev = value.v?.d
									if(dev in options.devices.matched) {
										//schedule one device schedule
										if(!schedules.find{ ((int)it.s == (int)condition.$) && (it.d == dev) }) {
											//schedule a wake up if there's none, otherwise just move on
											if((int)rtData.logging > 2) debug "Adding a timed trigger schedule for device $dev for condition ${condition.$}", rtData
											requestWakeUp(rtData, condition, condition, delay, dev)
										}
									} else {
										//cancel that one device schedule
										if((int)rtData.logging > 2) debug "Cancelling any timed trigger schedules for device $dev for condition ${condition.$}", rtData
										cancelStatementSchedules(rtData, condition.$, dev)
									}
								}
							} else {
								if(result) {
								//if we find the comparison true, set a timer if we haven't already
									List schedules = (boolean)rtData.pep ? atomicState.schedules : state.schedules
									if(!schedules.find{ ((int)it.s == (int)condition.$) }) {
										if((int)rtData.logging > 2) debug "Adding a timed trigger schedule for condition ${condition.$}", rtData
										requestWakeUp(rtData, condition, condition, delay)
									}
								} else {
									if((int)rtData.logging > 2) debug "Cancelling any timed trigger schedules for condition ${condition.$}", rtData
									cancelStatementSchedules(rtData, condition.$)
								}
							}
						}
					}
					result = false
				}
				result = not ? !result : !!result
			} else if(((String)rtData.event.name == 'time') && ((int)rtData.fastForwardTo == (int)condition.$)) {
				rtData.fastForwardTo = null
				rtData.resumed = true
				result = not ? false : true
			} else {
				result = oldResult
			}
		}
	}
	rtData.wakingUp = false
	rtData.conditionStateChanged = oldResult != result
	if((boolean)rtData.conditionStateChanged) {
		//condition change, perform TCP
		cancelConditionSchedules(rtData, condition.$)
	}
	rtData.cache["c:${condition.$}"] = result
	//true/false actions
	if((result || rtData.fastForwardTo) && condition.ts && condition.ts.length) executeStatements(rtData, condition.ts, async)
	if((!result || rtData.fastForwardTo) && condition.fs && condition.fs.length) executeStatements(rtData, condition.fs, async)
	//restore condition id
	rtData.stack.c = c
	if(!rtData.fastForwardTo) {
		msg.m = "Condition #${condition.$} evaluated $result"
		if((int)rtData.logging > 2) debug msg, rtData
	}
	if((rtData.fastForwardTo <= 0) && condition.s && ((String)condition.t == 'condition') && condition.lo && (String)condition.lo.t == 'v') {
		switch (condition.lo.v) {
		case 'time':
		case 'date':
		case 'datetime':
			scheduleTimeCondition(rtData, condition)
			break
		}
	}
	if((boolean)rtData.eric) log "evaluateCondition $condition", null, -1, null, "warn", true
	return result
}

private void updateCache(Map rtData, value) {
	def oldValue = rtData.cache[value.i]
	if(!oldValue || ((String)oldValue.t != (String)value.v.t) || (oldValue.v != value.v.v)) {
		//if((int)rtData.logging > 2) debug "Updating value", rtData
		rtData.newCache[value.i] = value.v + [s: now()]
	} else {
		//if((int)rtData.logging > 2) debug "Not updating value", rtData
	}
}

private boolean evaluateComparison(Map rtData, String comparison, Map lo, Map ro = null, Map ro2 = null, Map to = null, Map to2 = null, options = [:]) {
	if((boolean)rtData.eric) log "evaluateComparison $comparison", null, 1, null, "warn", true
	String fn = "comp_${comparison}"
	boolean result = ((String)lo.operand.g == 'any' ? false : true)
	if(options?.matches) {
		options.devices = [matched: [], unmatched: []]
	}
	//if multiple left values, go through each
	def tvalue = to && to.operand && to.values ? to.values + [f: to.operand.f] : null
	def tvalue2 = to2 && to2.operand && to2.values ? to2.values : null
	for(value in lo.values) {
		boolean res = false
		if(value && value.v && (!value.v.x || options.forceAll)) {
			try {
			//physical support
			//value.p = lo.operand.p
			if(value && ((String)value.v.t == 'device')) value.v = evaluateExpression(rtData, value.v, 'dynamic')
			if(!ro) {
				Map msg = timer ""
if((boolean)rtData.eric) log "$fn $value   $rvalue    $r2value    $tvalue   $tvalue2", null, 1, null, "warn", true
				res = (boolean)"$fn"(rtData, value, null, null, tvalue, tvalue2)
if((boolean)rtData.eric) log "$res  ${myObj(value?.v?.v)}    ${myObj(rvalue?.v?.v)} $fn $value   $rvalue    $r2value    $tvalue   $tvalue2", null, -1, null, "warn", true
				msg.m = "Comparison (${value?.v?.t}) ${value?.v?.v} $comparison = $res"
				if((int)rtData.logging > 2) debug msg, rtData
			} else {
				boolean rres
				res = (ro.operand.g == 'any' ? false : true)
				//if multiple right values, go through each
				for (rvalue in ro.values) {
					if(rvalue && ((String)rvalue.v.t == 'device')) rvalue.v = evaluateExpression(rtData, rvalue.v, 'dynamic')
					if(!ro2) {
						Map msg = timer ""
if((boolean)rtData.eric) log "$fn $value   $rvalue    $r2value    $tvalue   $tvalue2", null, 1, null, "warn", true
						rres = (boolean)"$fn"(rtData, value, rvalue, null, tvalue, tvalue2)
if((boolean)rtData.eric) log "$rres  ${myObj(value?.v?.v)}    ${myObj(rvalue?.v?.v)} $fn $value   $rvalue    $r2value    $tvalue   $tvalue2", null, -1, null, "warn", true
						msg.m = "Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} = $rres"
						if((int)rtData.logging > 2) debug msg, rtData
					} else {
						rres = ((String)ro2.operand.g == 'any' ? false : true)
						//if multiple right2 values, go through each
						for (r2value in ro2.values) {
							if(r2value && ((String)r2value.v.t == 'device')) r2value.v = evaluateExpression(rtData, r2value.v, 'dynamic')
							Map msg = timer ""
if((boolean)rtData.eric) log "$fn $value   $rvalue    $r2value    $tvalue   $tvalue2", null, 1, null, "warn", true
							boolean r2res = (boolean)"$fn"(rtData, value, rvalue, r2value, tvalue, tvalue2)
if((boolean)rtData.eric) log "$r2res  ${myObj(value?.v?.v)}    ${myObj(rvalue?.v?.v)}  $fn $value   $rvalue    $r2value    $tvalue   $tvalue2", null, -1, null, "warn", true
							msg.m = "Comparison (${value?.v?.t}) ${value?.v?.v} $comparison  (${rvalue?.v?.t}) ${rvalue?.v?.v} .. (${r2value?.v?.t}) ${r2value?.v?.v} = $r2res"
							if((int)rtData.logging > 2) debug msg, rtData
							rres = ((String)ro2.operand.g == 'any' ? rres || r2res : rres && r2res)
							if((((String)ro2.operand.g == 'any') && rres) || (((String)ro2.operand.g != 'any') && !rres)) break
						}
					}
					res = ((String)ro.operand.g == 'any' ? res || rres : res && rres)
					if((((String)ro.operand.g == 'any') && res) || (((String)ro.operand.g != 'any') && !res)) break
				}
			}
			} catch(all) {
				error "Error calling comparison $fn:", rtData, null, all
				res = false
			}

			if(res && ((String)lo.operand.t == 'v')) {
				switch ((String)lo.operand.v) {
				case 'time':
				case 'date':
				case 'datetime':
					boolean pass = (checkTimeRestrictions(rtData, lo.operand, now(), 5, 1) == 0) as Boolean
					if((int)rtData.logging > 2) debug "Time restriction check ${pass ? 'passed' : 'failed'}", rtData
					if(!pass) res = false
				}
			}
		}
		result = ((String)lo.operand.g == 'any' ? result || res : result && res)
		if(options?.matches && value.v.d) {
			if(res) {
				options.devices.matched.push(value.v.d)
			} else {
				options.devices.unmatched.push(value.v.d)
			}
		}
		if(((String)lo.operand.g == 'any') && res && !(options?.matches)) {
			//logical OR if we're using the ANY keyword
			break
		}
		if(((String)lo.operand.g == 'all') && !result && !(options?.matches)) {
			//logical AND if we're using the ALL keyword
			break
		}
	}
	if((boolean)rtData.eric) log "evaluateComparison $comparison", null, -1, null, "warn", true
	return result
}

private void cancelStatementSchedules(Map rtData, int statementId, data = null) {
	//cancel all schedules that are pending for statement statementId
	if((int)rtData.logging > 2) debug "Cancelling statement #${statementId}'s schedules...", rtData
	if(!(statementId in rtData.cancelations.statements)) {
		rtData.cancelations.statements.push([id: statementId, data: data])
	}
}

private void cancelConditionSchedules(Map rtData, int conditionId) {
	//cancel all schedules that are pending for condition conditionId
	if((int)rtData.logging > 2) debug "Cancelling condition #${conditionId}'s schedules...", rtData
	if(!(conditionId in rtData.cancelations.conditions)) {
		rtData.cancelations.conditions.push(conditionId)
	}
}

private boolean matchDeviceSubIndex(list, deviceSubIndex) {
	if(!list || !(list instanceof List) || (list.size() == 0)) return true
	return list.collect{ "$it".toString() }.indexOf("$deviceSubIndex".toString()) >= 0
}

private boolean matchDeviceInteraction(String option, boolean isPhysical) {
	return !(((option == 'p') && !isPhysical) || ((option == 's') && isPhysical))
}

private List listPreviousStates(device, String attribute, long threshold, excludeLast) {
	def result = []
	//if(!(device instanceof DeviceWrapper)) return result
	def events = device.events([all: true, max: 100]).findAll{(String)it.name == attribute}
	//if we got any events, let's go through them
	//if we need to exclude last event, we start at the second event, as the first one is the event that triggered this function. The attribute's value has to be different from the current one to qualify for quiet
	if(events.size()) {
		long thresholdTime = now() - threshold
		long endTime = now()
		for(int i = 0; i < events.size(); i++) {
			long startTime = events[i].date.getTime()
			long duration = endTime - startTime
			if((duration >= 1000) && ((i > 0) || !excludeLast)) {
				result.push([value: events[i].value, startTime: startTime, duration: duration])
			}
			if(startTime < thresholdTime)
				break
			endTime = startTime
		}
	} else {
		def currentState = device.currentState(attribute, true)
		if(currentState) {
			long startTime = currentState.getDate().getTime()
			result.push([value: currentState.value, startTime: startTime, duration: now() - startTime])
		}
	}
	return result
}

private Map valueCacheChanged(Map rtData, Map comparisonValue) {
	def oldValue = rtData.cache[comparisonValue.i]
	def newValue = comparisonValue.v
	if(!(oldValue instanceof Map)) oldValue = false
	return (!!oldValue && (((String)oldValue.t != (String)newValue.t) || ("${oldValue.v}" != "${newValue.v}"))) ? [i: comparisonValue.i, v: oldValue] : null
}

private boolean valueWas(Map rtData, Map comparisonValue, Map rightValue, Map rightValue2, Map timeValue, String func) {
	if(!comparisonValue || !comparisonValue.v || !comparisonValue.v.d || !comparisonValue.v.a || !timeValue || !timeValue.v || !timeValue.vt) {
		return false
	}
	def device = getDevice(rtData, comparisonValue.v.d)
	if(!device) return false
	String attribute = (String)comparisonValue.v.a
	long threshold = (long)evaluateExpression(rtData, [t: 'duration', v: timeValue.v, vt: (String)timeValue.vt], 'long').v

	def states = listPreviousStates(device, attribute, threshold, (rtData.event.device?.id == device.id) && ((String)rtData.event.name == attribute))
	boolean result = true
	long duration = 0
	for (stte in states) {
		if(!("comp_$func"(rtData, [i: comparisonValue.i, v: [t: (String)comparisonValue.v.t, v: cast(rtData, stte.value, (String)comparisonValue.v.t)]], rightValue, rightValue2, timeValue))) break
		duration += stte.duration
	}
	if(!duration) return false
	result = (timeValue.f == 'l') ? duration < threshold : duration >= threshold
	if((int)rtData.logging > 2) debug "Duration ${duration}ms for ${func.replace('is_', 'was_')} ${timeValue.f == 'l' ? '<' : '>='} ${threshold}ms threshold = ${result}", rtData
	return result
}

private boolean valueChanged(Map rtData, Map comparisonValue, Map timeValue) {
	if(!comparisonValue || !comparisonValue.v || !comparisonValue.v.d || !comparisonValue.v.a || !timeValue || !timeValue.v || !timeValue.vt) {
		return false
	}
	def device = getDevice(rtData, comparisonValue.v.d)
	if(!device) return false
	String attribute = (String)comparisonValue.v.a
	long threshold = (long)evaluateExpression(rtData, [t: 'duration', v: timeValue.v, vt: (String)timeValue.vt], 'long').v

	def states = listPreviousStates(device, attribute, threshold, false)
	if(!states.size()) return false
	def value = states[0].value
	for (tstate in states) {
		if(tstate.value != value) return true
	}
	return false
}

private boolean match(String string, String pattern) {
	if((pattern.size() > 2) && pattern.startsWith('/') && pattern.endsWith('/')) {
		pattern = ~pattern.substring(1, patern.size() - 1)
		return !!(string =~ pattern)
	}
	return string.contains(pattern)
}

//comparison low level functions
private boolean comp_is					(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return ((String)evaluateExpression(rtData, lv.v, 'string').v == (String)evaluateExpression(rtData, rv.v, 'string').v) || (lv.v.n && ((String)cast(rtData, lv.v.n, 'string') == (String)cast(rtData, rv.v.v, 'string'))) }
private boolean comp_is_not				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !comp_is(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_is_equal_to			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { String dt = ((lv?.v?.t == 'decimal') || (rv?.v?.t == 'decimal') ? 'decimal' : ((lv?.v?.t == 'integer') || (rv?.v?.t == 'integer') ? 'integer' : 'dynamic')); return evaluateExpression(rtData, lv.v, dt).v == evaluateExpression(rtData, rv.v, dt).v }
private boolean comp_is_not_equal_to			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { String dt = ((lv?.v?.t == 'decimal') || (rv?.v?.t == 'decimal') ? 'decimal' : ((lv?.v?.t == 'integer') || (rv?.v?.t == 'integer') ? 'integer' : 'dynamic')); return evaluateExpression(rtData, lv.v, dt).v != evaluateExpression(rtData, rv.v, dt).v }
private boolean comp_is_different_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_not_equal_to(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_is_less_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return (double)evaluateExpression(rtData, lv.v, 'decimal').v < (double)evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_less_than_or_equal_to		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return (double)evaluateExpression(rtData, lv.v, 'decimal').v <= (double)evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_greater_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return (double)evaluateExpression(rtData, lv.v, 'decimal').v > (double)evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_greater_than_or_equal_to	(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return (double)evaluateExpression(rtData, lv.v, 'decimal').v >= (double)evaluateExpression(rtData, rv.v, 'decimal').v }
private boolean comp_is_even				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return ((int)evaluateExpression(rtData, lv.v, 'integer').v).mod(2) == 0 }
private boolean comp_is_odd				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return ((int)evaluateExpression(rtData, lv.v, 'integer').v).mod(2) != 0 }
private boolean comp_is_true				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return (boolean)evaluateExpression(rtData, lv.v, 'boolean').v }
private boolean comp_is_false				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !(boolean)evaluateExpression(rtData, lv.v, 'boolean').v }
private boolean comp_is_inside_of_range			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { double v = (double)evaluateExpression(rtData, lv.v, 'decimal').v; double v1 = (double)evaluateExpression(rtData, rv.v, 'decimal').v; double v2 = (double)evaluateExpression(rtData, rv2.v, 'decimal').v; return (v1 < v2) ? ((v >= v1) && (v <= v2)) : ((v >= v2) && (v <= v1)); }
private boolean comp_is_outside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !comp_is_inside_of_range(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_is_any_of				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { String v = (String)evaluateExpression(rtData, lv.v, 'string').v; for (vi in rv.v.v.tokenize(',')) { if(v == (String)evaluateExpression(rtData, [t: (String)rv.v.t, v: "$vi".toString().trim(), i: rv.v.i, a: rv.v.a, vt: (String)rv.v.vt], 'string').v) return true; }; return false;}
private boolean comp_is_not_any_of			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !comp_is_any_of(rtData, lv, rv, rv2, tv, tv2); }

private boolean comp_was				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is'); }
private boolean comp_was_not				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_not'); }
private boolean comp_was_equal_to			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_equal_to'); }
private boolean comp_was_not_equal_to			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_not_equal_to'); }
private boolean comp_was_different_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_different_than'); }
private boolean comp_was_less_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_less_than'); }
private boolean comp_was_less_than_or_equal_to		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_less_than_or_equal_to'); }
private boolean comp_was_greater_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_greater_than'); }
private boolean comp_was_greater_than_or_equal_to	(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_greater_than_or_equal_to'); }
private boolean comp_was_even				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_even'); }
private boolean comp_was_odd				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_odd'); }
private boolean comp_was_true				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_true'); }
private boolean comp_was_false				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_false'); }
private boolean comp_was_inside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_inside_of_range'); }
private boolean comp_was_outside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_outside_of_range'); }
private boolean comp_was_any_of				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_any_of'); }
private boolean comp_was_not_any_of			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueWas(rtData, lv, rv, rv2, tv, 'is_not_any_of'); }

private boolean comp_changed				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, tv2 = null) { return valueChanged(rtData, lv, tv); }
private boolean comp_did_not_change			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !valueChanged(rtData, lv, tv); }

private boolean comp_is_any				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return true; }
private boolean comp_is_before				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { long offset1 = tv ? (long)evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: (String)tv.vt], 'long').v : 0; return cast(rtData, (long)evaluateExpression(rtData, lv.v, 'datetime').v + 2000, (String)lv.v.t) < cast(rtData, (long)evaluateExpression(rtData, rv.v, 'datetime').v + offset1, (String)lv.v.t); }
private boolean comp_is_after				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { long offset1 = tv ? evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: (String)tv.vt], 'long').v : 0; return cast(rtData, (long)evaluateExpression(rtData, lv.v, 'datetime').v + 2000, (String)lv.v.t) >= cast(rtData, (long)evaluateExpression(rtData, rv.v, 'datetime').v + offset1, (String)lv.v.t); }
private boolean comp_is_between				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { long offset1 = tv ? (long)evaluateExpression(rtData, [t: 'duration', v: tv.v, vt: (String)tv.vt], 'long').v : 0; long offset2 = tv2 ? (long)evaluateExpression(rtData, [t: 'duration', v: tv2.v, vt: (String)tv2.vt], 'long').v : 0; long v = cast(rtData, (long)evaluateExpression(rtData, lv.v, 'datetime').v + 2000, (String)lv.v.t); long v1 = cast(rtData, (long)evaluateExpression(rtData, rv.v, 'datetime').v + offset1, (String)lv.v.t); long v2 = cast(rtData, (long)evaluateExpression(rtData, rv2.v, 'datetime').v + offset2, (String)lv.v.t); return (v1 < v2) ? (v >= v1) && (v < v2) : (v < v2) || (v >= v1); }
private boolean comp_is_not_between			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !comp_is_between(rtData, lv, rv, rv2, tv, tv2); }

/*triggers*/
private boolean comp_gets				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return ((String)cast(rtData, lv.v.v, 'string') == (String)cast(rtData, rv.v.v, 'string')) && matchDeviceSubIndex(lv.v.i, rtData.currentEvent.index)}
private boolean comp_executes				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is(rtData, lv, rv, rv2, tv, tv2) }
private boolean comp_arrives				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return (rtData.event.name == 'email') && match(rtData.event?.jsonData?.from ?: '', (String)evaluateExpression(rtData, rv.v, 'string').v) && match(rtData.event?.jsonData?.message ?: '', (String)evaluateExpression(rtData, rv2.v, 'string').v) }
private boolean comp_happens_daily_at			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return rtData.wakingUp }

private boolean comp_changes				(Map rtData, Map lv, rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueCacheChanged(rtData, lv) && matchDeviceInteraction(lv.v.p, rtData.currentEvent.physical); }
private boolean comp_changes_to				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return valueCacheChanged(rtData, lv) && ("${lv.v.v}" == "${rv.v.v}") && matchDeviceInteraction(lv.v.p, rtData.currentEvent.physical); }
private boolean comp_receives				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return ("${lv.v.v}" == "${rv.v.v}") && matchDeviceInteraction(lv.v.p, rtData.currentEvent.physical); }
private boolean comp_changes_away_from			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && ("${oldValue.v.v}" == "${rv.v.v}") && matchDeviceInteraction(lv.v.p, rtData.currentEvent.physical); }
private boolean comp_drops				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') > cast(rtData, lv.v.v, 'decimal')); }
private boolean comp_does_not_drop			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !comp_drops(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_drops_below			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_drops_to_or_below			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_rises				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') < cast(rtData, lv.v.v, 'decimal')); }
private boolean comp_does_not_rise			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !comp_rises(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_rises_above			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_rises_to_or_above			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_below			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') < cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_below_or_equal_to		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') <= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_above			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') > cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_remains_above_or_equal_to		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')) && (cast(rtData, lv.v.v, 'decimal') >= cast(rtData, rv.v.v, 'decimal')); }
private boolean comp_enters_range			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if(!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if(v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return ((ov < v1) || (ov > v2)) && ((v >= v1) && (v <= v2)); }
private boolean comp_exits_range			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if(!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if(v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return ((ov >= v1) && (ov <= v2)) && ((v < v1) || (v > v2)); }
private boolean comp_remains_inside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if(!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if(v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return (ov >= v1) && (ov <= v2) && (v >= v1) && (v <= v2); }
private boolean comp_remains_outside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); if(!oldValue) return false; def ov = cast(rtData, oldValue.v.v, 'decimal'); def v = cast(rtData, lv.v.v, 'decimal'); def v1 = cast(rtData, rv.v.v, 'decimal'); def v2 = cast(rtData, rv2.v.v, 'decimal'); if(v1 > v2) { def vv = v1; v1 = v2; v2 = vv; }; return ((ov < v1) || (ov > v2)) && ((v < v1) || (v > v2)); }
private boolean comp_becomes_even			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) != 0) && (cast(rtData, lv.v.v, 'integer').mod(2) == 0); }
private boolean comp_becomes_odd			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) == 0) && (cast(rtData, lv.v.v, 'integer').mod(2) != 0); }
private boolean comp_remains_even			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) == 0) && (cast(rtData, lv.v.v, 'integer').mod(2) == 0); }
private boolean comp_remains_odd			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return oldValue && (cast(rtData, oldValue.v.v, 'integer').mod(2) != 0) && (cast(rtData, lv.v.v, 'integer').mod(2) != 0); }

private boolean comp_changes_to_any_of			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return !!valueCacheChanged(rtData, lv) && comp_is_any_of(rtData, lv, rv, rv2, tv, tv2) && matchDeviceInteraction(lv.v.p, rtData.currentEvent.physical); }
private boolean comp_changes_away_from_any_of		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { def oldValue = valueCacheChanged(rtData, lv); return !!oldValue && comp_is_any_of(rtData, oldValue, rv, rv2) && matchDeviceInteraction(lv.v.p, rtData.currentEvent.physical); }

private boolean comp_stays				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_unchanged			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return true; }
private boolean comp_stays_not				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_not(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_equal_to			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_different_than		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_different_than(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_less_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_less_than(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_less_than_or_equal_to	(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_less_than_or_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_greater_than			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_greater_than(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_greater_than_or_equal_to	(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_greater_than_or_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_even				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_even(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_odd				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_odd(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_true				(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_true(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_false			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_false(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_inside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_inside_of_range(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_outside_of_range		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_outside_of_range(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_any_of			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_any_of(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_away_from			(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_not_equal_to(rtData, lv, rv, rv2, tv, tv2); }
private boolean comp_stays_away_from_any_of		(Map rtData, Map lv, Map rv = null, Map rv2 = null, Map tv = null, Map tv2 = null) { return comp_is_not_any_of(rtData, lv, rv, rv2, tv, tv2); }


private void traverseStatements(node, closure, parentNode = null, data = null) {
	if(!node) return
	//if a statements element, go through each item
	if(node instanceof List) {
		for(def item in node) {
			if(!item.di) {
				boolean lastTimer = (data && data.timer)
				if(data && ((String)item.t == 'every')) {
					data.timer = true
				}
				traverseStatements(item, closure, parentNode, data)
				if(data) {
					data.timer = lastTimer
				}
			}
		}
		return
	}

	//got a statement
	if(closure instanceof Closure) {
		closure(node, parentNode, data)
	}

	//if the statements has substatements, go through them
	if(node.s instanceof List) {
		traverseStatements(node.s, closure, node, data)
	}
	if(node.e instanceof List) {
		traverseStatements(node.e, closure, node, data)
	}
}

private void traverseEvents(node, closure, parentNode = null) {
	if(!node) return
	//if a statements element, go through each item
	if(node instanceof List) {
		for(item in node) {
			traverseEvents(item, closure, parentNode)
		}
		return
	}
	//got a condition
	if((closure instanceof Closure)) {
		closure(node, parentNode)
	}
}

private void traverseConditions(node, closure, parentNode = null) {
	if(!node) return
	//if a statements element, go through each item
	if(node instanceof List) {
		for(item in node) {
			traverseConditions(item, closure, parentNode)
		}
		return
	}
	//got a condition
	if((node.t == 'condition') && (closure instanceof Closure)) {
		closure(node, parentNode)
	}
	//if the statements has substatements, go through them
	if(node.c instanceof List) {
		if(closure instanceof Closure) closure(node, parentNode)
		traverseConditions(node.c, closure, node)
	}
}

private void traverseRestrictions(node, closure, parentNode = null) {
	if(!node) return
	//if a statements element, go through each item
	if(node instanceof List) {
		for(item in node) {
			traverseRestrictions(item, closure, parentNode)
		}
	return
	}
	//got a restriction
	if((node.t == 'restriction') && (closure instanceof Closure)) {
		closure(node, parentNode)
	}
	//if the statements has substatements, go through them
	if(node.r instanceof List) {
		if(closure instanceof Closure) closure(node, parentNode)
		traverseRestrictions(node.r, closure, node)
	}
}
private void traverseExpressions(node, closure, param, parentNode = null) {
	if(!node) return
	//if a statements element, go through each item
	if(node instanceof List) {
		for(item in node) {
			traverseExpressions(item, closure, param, parentNode)
		}
	return
	}
	//got a statement
	if(closure instanceof Closure) {
		closure(node, parentNode, param)
	}
	//if the statements has substatements, go through them
	if(node.i instanceof List) {
		traverseExpressions(node.i, closure, param, node)
	}
}

private void updateDeviceList(Map rtData, List deviceIdList) {
	app.updateSetting('dev', [type: /*isHubitat() ?*/ 'capability'/* : 'capability.device'*/, value: deviceIdList.unique()])
	clearMyCache("updateDeviceList")
}

private void subscribeAll(Map rtData, boolean doit=true) {
	try {
	rtData = rtData ?: getRunTimeData()
	def ss = [
		events: 0,
		controls: 0,
		devices: 0,
	]
	def statementData = [timer: false]
	Map msg = timer "Finished subscribing", null, -1
	if(doit) {
		unsubscribe()
		if((int)rtData.logging > 1) trace "Subscribing to devices...", rtData, 1
	}
	Map devices = [:]
	Map rawDevices = [:]
	Map subscriptions = [:]
	boolean hasTriggers = false
	boolean downgradeTriggers = false
	//traverse all statements
	def expressionTraverser
	def operandTraverser
	def eventTraverser
	def conditionTraverser
	def restrictionTraverser
	def statementTraverser
	expressionTraverser = { Map expression, parentExpression, String comparisonType ->
		String subscriptionId = (String)null
		String deviceId = (String)null
		String attribute = (String)null
		if(((String)expression.t == 'device') && (expression.id)) {
			devices[expression.id] = [c: (comparisonType ? 1 : 0) + (devices[expression.id]?.c ?: 0)]
			//subscriptionId = "${expression.id}${expression.a}"
			deviceId = (String)expression.id
			attribute = (String)expression.a
			subscriptionId = "${deviceId}${attribute}"
		}
		String exprX = (String)expression.x
		//if(((String)expression.t == 'variable') && expression.x && expression.x.startsWith('@')) {
		if(((String)expression.t == 'variable') && exprX && exprX.startsWith('@')) {
			subscriptionId = "${exprX}"
			deviceId = rtData.locationId
			attribute = "${exprX.startsWith('@@') ? '@@' + handle() : (String)rtData.instanceId}.${exprX}"
		}
		if(subscriptionId && deviceId) {
			String ct = (String)subscriptions[subscriptionId]?.t ?: (String)null
			if((ct == 'trigger') || (comparisonType == 'trigger')) {
				ct = 'trigger'
			} else {
				ct = ct ?: comparisonType
			}
			//if((boolean)rtData.eric) log "subscribeAll condition is $condition", null, null, null, "warn", true
			subscriptions[subscriptionId] = [d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + [condition]]
			if((deviceId != rtData.locationId) && (deviceId.startsWith(':'))) {
				rawDevices[deviceId] = rtData.devices[deviceId]
				devices[deviceId] = [c: (comparisonType ? 1 : 0) + (devices[deviceId]?.c ?: 0)]
			}
		}
	}
	operandTraverser = { Map node, Map operand, value, String comparisonType ->
		if(!operand) return
		switch ((String)operand.t) {
		case "p": //physical device
			for(String deviceId in expandDeviceList(rtData, operand.d, true)) {
				devices[deviceId] = [c: (comparisonType ? 1 : 0) + (devices[deviceId]?.c ?: 0)]
				//String subscriptionId = "$deviceId${operand.a}"
				String attribute = "${operand.a}"
				String subscriptionId = "$deviceId${attribute}"
				//if we have any trigger, it takes precedence over anything else
				String ct = (String)subscriptions[subscriptionId]?.t ?: (String)null
				String oct = ct
				String msgVal
				if((ct == 'trigger') || (comparisonType == 'trigger')) {
					ct = 'trigger'

					String attrVal
					if( (node.co == 'receives' || node.co == 'gets') && value && ((String)value?.t == 'c') && (value.c)) {
						attrVal = attribute + ".${value.c}"
						msgVal = "Attempting Attribute value subscription"
					}
					String aval = (String)subscriptions[subscriptionId]?.a
					if(aval && subscriptions[subscriptionId]?.d && ((oct == 'trigger') && ( aval != attrVal)) ) {
						msgVal = "Using Attribute subscription"
						attrVal = ""
					}
					if(attrVal) {
						attribute = attrVal
					}
					if(doit && msgVal && rtData.logging > 2) debug msgVal, rtData

				} else {
					ct = ct ?: comparisonType
				}
				subscriptions[subscriptionId] = [d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + (comparisonType?[node]:[])]
				if((deviceId != rtData.locationId) && (deviceId.startsWith(':'))) {
					rawDevices[deviceId] = rtData.devices[deviceId]
				}
			}
			break
		case "v": //virtual device
			String deviceId = rtData.locationId
			//if we have any trigger, it takes precedence over anything else
			devices[deviceId] = [c: (comparisonType ? 1 : 0) + (devices[deviceId]?.c ?: 0)]
			String subscriptionId = (String)null
			String attribute = (String)null
			String operV = (String)operand.v
			switch (operV) {
			case 'alarmSystemStatus':
				subscriptionId = "$deviceId${operV}"
				attribute = "hsmStatus"
				break
			case 'alarmSystemAlert':
				subscriptionId = "$deviceId${operV}"
				attribute = "hsmAlerts"
				break
			case 'alarmSystemEvent':
				subscriptionId = "$deviceId${operV}"
				attribute = "hsmSetArm"
				break
			case 'alarmSystemRule':
				subscriptionId = "$deviceId${operV}"
				attribute = "hsmRules"
				break
			case 'time':
			case 'date':
			case 'datetime':
			case 'mode':
			case 'powerSource':
				subscriptionId = "$deviceId${operV}"
				attribute = operV
				break
			case 'email':
				subscriptionId = "$deviceId${operV}${rtData.id}"
				attribute = "email.${rtData.id}" // receive email does not work in webcore
				break
			case 'ifttt':
				if(value && ((String)value.t == 'c') && (value.c)) {
					def options = VirtualDevices()[operV]?.o
					def item = options ? options[value.c] : value.c
					if(item) {
						subscriptionId = "$deviceId${operV}${item}"

						//def attrVal = isHubitat() ? "" : ".${item}"
						String attrVal = ".${item}"
						attribute = "${operV}${attrVal}"
					}
				}
				break
			}
			if(subscriptionId) {
				String ct = (String)subscriptions[subscriptionId]?.t ?: (String)null
				if((ct == 'trigger') || (comparisonType == 'trigger')) {
					ct = 'trigger'
				} else {
					ct = ct ?: comparisonType
				}
				subscriptions[subscriptionId] = [d: deviceId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + (comparisonType?[node]:[])]
				break
			}
			break
		case 'x':
			String operX = (String)operand.x
			if(operX && operX.startsWith('@')) {
				String subscriptionId = operX
				String attribute = "${operX.startsWith('@@') ? '@@' + handle() : (String)rtData.instanceId}.${operX}"
				String ct = (String)subscriptions[subscriptionId]?.t ?: (String)null
				if((ct == 'trigger') || (comparisonType == 'trigger')) {
					ct = 'trigger'
				} else {
					ct = ct ?: comparisonType
				}
				subscriptions[subscriptionId] = [d: rtData.locationId, a: attribute, t: ct, c: (subscriptions[subscriptionId] ? subscriptions[subscriptionId].c : []) + (comparisonType?[node]:[])]
			}
			break
		case "c": //constant
		case "e": //expression
			traverseExpressions(operand.exp?.i, expressionTraverser, comparisonType)
			break
		}
	}
	eventTraverser = { Map event, parentEvent ->
		if(event.lo) {
			String comparisonType = 'trigger'
			operandTraverser(event, event.lo, null, comparisonType)
		}
	}
	conditionTraverser = { Map condition, parentCondition ->
		if((String)condition.co) {
			//def comparison = rtData.comparisons.conditions[condition.co]
			def comparison = Comparisons().conditions[(String)condition.co]
			String comparisonType = 'condition'
			if(!comparison) {
				hasTriggers = true
				comparisonType = downgradeTriggers || ((String)condition.sm == 'never') ? 'condition' : 'trigger'
				//comparison = rtData.comparisons.triggers[condition.co]
				comparison = Comparisons().triggers[(String)condition.co]
			}
			if(comparison) {
				condition.ct = comparisonType.take(1)
				int paramCount = comparison.p ?: 0
				for(int i = 0; i <= paramCount; i++) {
					//get the operand to parse
					def operand = (i == 0 ? condition.lo : (i == 1 ? condition.ro : condition.ro2))
					operandTraverser(condition, operand, condition.ro, comparisonType)
				}
			}
		}
		if(condition.ts instanceof List) traverseStatements(condition.ts, statementTraverser, condition, statementData)
		if(condition.fs instanceof List) traverseStatements(condition.fs, statementTraverser, condition, statementData)
	}
	restrictionTraverser = { Map restriction, parentRestriction ->
		if((String)restriction.co) {
			//def comparison = rtData.comparisons.conditions[restriction.co]
			def comparison = Comparisons().conditions[(String)restriction.co]
			String comparisonType = 'condition'
			if(!comparison) {
				//hasTriggers = true
				//comparisonType = downgradeTriggers ? 'condition' : 'trigger'
				//comparison = rtData.comparisons.triggers[restriction.co]
				comparison = Comparisons().triggers[(String)restriction.co]
			}
			if(comparison) {
				int paramCount = comparison.p ?: 0
				for(int i = 0; i <= paramCount; i++) {
					//get the operand to parse
					def operand = (i == 0 ? restriction.lo : (i == 1 ? restriction.ro : restriction.ro2))
					operandTraverser(restriction, operand, null, null)
				}
			}
		}
	}
	statementTraverser = { Map node, parentNode, data ->
		downgradeTriggers = data && data.timer
		if(node.r) traverseRestrictions(node.r, restrictionTraverser)
		for(String deviceId in node.d) {
			devices[deviceId] = devices[deviceId] ?: [c: 0]
			if((deviceId != rtData.locationId) && (deviceId.startsWith(':'))) {
				rawDevices[deviceId] = rtData.devices[deviceId]
			}
		}
		switch( (String)node.t ) {
		case 'action':
			break
		case 'if':
			if(node.ei) {
				for (ei in node.ei) {
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
			for (c in node.cs) {
				operandTraverser(c, c.ro, null, null)
				//if case is a range, traverse the second operand too
				if((String)c.t == 'r') operandTraverser(c, c.ro2, null, null)
				if(c.s instanceof List) {
					traverseStatements(c.s, statementTraverser, node, data)
				}
			}
			break
		case 'every':
			hasTriggers = true
			break
		}
	}
	if(rtData.piston.r) traverseRestrictions(rtData.piston.r, restrictionTraverser)
	if(rtData.piston.s) traverseStatements(rtData.piston.s, statementTraverser, null, statementData)
	//device variables
	for(variable in rtData.piston.v.findAll{ ((String)it.t == 'device') && it.v && it.v.d && (it.v.d instanceof List)}) {
		for (String deviceId in variable.v.d) {
			devices[deviceId] = [c: 0 + (devices[deviceId]?.c ?: 0)]
			if(deviceId != rtData.locationId) {
				rawDevices[deviceId] = rtData.devices[deviceId]
			}
		}
	}
	def dds = [:]
//log.debug "subscribeAll subscriptions ${subscriptions}"
	for (subscription in subscriptions) {
		String altSub = 'never'
		for (condition in subscription.value.c) if(condition) {
			condition.s = false
			String tt0 = (String)condition.sm
			altSub = (tt0 == 'always') ? tt0 : ((altSub != 'always') && (tt0 != 'never') ? tt0 : altSub)
		}
		if(!rtData.piston.o.des && (String)subscription.value.t && !!subscription.value.c && (altSub != "never") && (((String)subscription.value.t == "trigger") || (altSub == "always") || !hasTriggers)) {
			def device = ((String)subscription.value.d).startsWith(':') ? getDevice(rtData, (String)subscription.value.d) : null
			String t0 = (String)subscription.value.a
			String a = (t0 == 'orientation') || (t0 == 'axisX') || (t0 == 'axisY') || (t0 == 'axisZ') ? 'threeAxis' : (String)subscription.value.a
			if(device) {
				for (condition in subscription.value.c) if(condition) {
					String t1 = (String)condition.sm
					condition.s = (t1 != 'never') && (((String)condition.ct == 't') || (t1 == 'always') || (!hasTriggers))
				}
				switch (t0) {
				case 'time':
				case 'date':
				case 'datetime':
					break
				default:
					if(doit) {
						if((int)rtData.logging) info "Subscribing to $device.${a}...", rtData
						subscribe(device, a, deviceHandler)
					}
					ss.events = ss.events + 1
					if(!dds[device.id]) {
						ss.devices = ss.devices + 1
						dds[device.id] = 1
					}
				}
			} else {
				error "Failed subscribing to $device.${a}, device not found", rtData
			}
		} else {
			for (condition in subscription.value.c) if(condition) { condition.s = false }
			String tt0 = (String)subscription.value.d
			if(devices[tt0]) {
				devices[tt0].c = devices[tt0].c - 1
			}
		}
	}
	//save devices
	List deviceIdList = rawDevices.collect{ it && it.value ? it.value.id : null }
	deviceIdList.removeAll{ it == null }
	if(doit) updateDeviceList(rtData, deviceIdList)

	//not using fake subscriptions for controlled devices - piston has device in settings
	for (d in devices.findAll{ ((it.value.c <= 0) || (rtData.piston.o.des)) && (it.key != rtData.locationId) }) {
		def device = d.key.startsWith(':') ? getDevice(rtData, d.key) : null
		if(device && (device != location)) {
			if((int)rtData.logging > 1 && doit) trace "Piston controls $device...", rtData
			ss.controls = ss.controls + 1
			if(!dds[device.id]) {
				ss.devices = ss.devices + 1
				dds[device.id] = 1
			}
		}
	}
	if(doit) {
		state.subscriptions = ss
		if((int)rtData.logging > 1) trace msg, rtData

		def event = [date: new Date(), device: location, name: 'time', value: now(), schedule: [t: 0, s: 0, i: -9]]
		//subscribe(app, appHandler)
		subscribe(location, rtData.id, executeHandler)
		executeEvent(rtData, event)
		processSchedules rtData, true
	//save cache collected through dummy run
		for(item in rtData.newCache) rtData.cache[(String)item.key] = item.value
		state.cache = rtData.cache
	}

	} catch (all) {
		error "An error has occurred while subscribing: ", rtData, null, all
	}
}

private List expandDeviceList(Map rtData, List devices, boolean localVarsOnly = false) {
	localVarsOnly = false	//temporary allowing global vars
	List result = []
	for(String deviceId in devices) {
		if(deviceId && (deviceId.size() == 34) && deviceId.startsWith(':') && deviceId.endsWith(':')) {
			result.push(deviceId)
		} else {
			if(localVarsOnly) {
				//during subscriptions we use local vars only to make sure we don't subscribe to "variable" lists of devices
				def var = rtData.localVars[deviceId]
				if(var && ((String)var.t == 'device') && (var.v instanceof Map) && ((String)var.v.t == 'd') && (var.v.d instanceof List) && var.v.d.size()) result += var.v.d
			} else {
				def var = getVariable(rtData, deviceId)
				if(var && ((String)var.t == 'device') && (var.v instanceof List) && var.v.size()) result += var.v
				if(var && ((String)var.t != 'device')) {
					var device = getDevice(rtData, (String)cast(rtData, var.v, 'string'))
					if(device) result += [hashId(device.id)]
				}
			}
		}
	}
	return result.unique()
}

def appHandler(evt) {
}

private String sanitizeVariableName(String name) {
	name = name ? "$name".trim().replace(" ", "_") : null
}

private getDevice(Map rtData, idOrName) {
	if(rtData.locationId == idOrName) return location
	def device = rtData.devices[idOrName] ?: rtData.devices.find{ it.value.getDisplayName() == idOrName }?.value
	if(!device) {
		if(!rtData.allDevices) {
			Map msg = timer "Device missing from piston. Loading all from parent..."
			rtData.allDevices = parent.listAvailableDevices(true)
			if((int)rtData.logging > 2) debug msg, rtData
		}
		if(rtData.allDevices) {
			def deviceMap = rtData.allDevices.find{ (idOrName == (String)it.key) || (idOrName == it.value.getDisplayName()) }
			if(deviceMap) {
				rtData.updateDevices = true
				rtData.devices[deviceMap.key] = deviceMap.value
				device = deviceMap.value
			}
		} else {
			error "Device ${idOrName} was not found. Please review your piston.", rtData
		}
	}
	return device
}

private getDeviceAttributeValue(Map rtData, device, String attributeName) {
	if(rtData.event && ((String)rtData.event.name == attributeName) && (rtData.event.device?.id == device.id)) {
		return rtData.event.value
	} else {
		switch (attributeName) {
		case '$status':
			return device.getStatus()
		case 'orientation':
			return getThreeAxisOrientation(rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue : device.currentValue('threeAxis', true))
		case 'axisX':
			return rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue.x : device.currentValue('threeAxis', true).x
		case 'axisY':
			return rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue.y : device.currentValue('threeAxis', true).y
		case 'axisZ':
			return rtData.event && (rtData.event.name == 'threeAxis') && (rtData.event.device?.id == device.id) ? rtData.event.xyzValue.z : device.currentValue('threeAxis', true).z
		}
		def result
		try {
			result = device.currentValue(attributeName, true)
		} catch (all) {
			error "Error reading current value for $device.$attributeName:", rtData, all
		}
		return result ?: ''
	}
}

private Map getDeviceAttribute(Map rtData, String deviceId, String attributeName, subDeviceIndex = null, Boolean trigger = false) {
	if(deviceId == rtData.locationId) {
		//we have the location here
		switch (attributeName) {
		case 'mode':
			def mode = location.getCurrentMode()
			return [t: 'string', v: hashId(mode.getId()), n: mode.getName()]
		case 'alarmSystemStatus':
			def v = location.hsmStatus
			def n = VirtualDevices()['alarmSystemStatus']?.o[v]
			return [t: 'string', v: v, n: n]
		}
		return [t: 'string', v: location.getName().toString()]
	}
	def device = getDevice(rtData, deviceId)
	if(device) {
		//def attribute = rtData.attributes[attributeName ?: '']
		def attribute = Attributes()[attributeName ?: '']
		if(!attribute) {
			attribute = [t: 'string', m: false]
		}
		//x = eXclude - if a momentary attribute is looked for and the device does not match the current device, then we must ignore this during comparisons
		def t0 = (attributeName ? getDeviceAttributeValue(rtData, device, attributeName) : null)
		String tt1 = (String)attribute.t
//		String tt2 = myObj(t0)
		boolean match = t0 && ((t0 instanceof String && (tt1 in ['string', 'enum']) ) ||
				(t0 instanceof Integer && (tt1 == 'integer')) )
//if(attributeName) log.warn "attributeName $attributeName t0   $t0 of $tt2   tt1 $tt1    match $match }"
		def value = (attributeName ? ( match  ? t0 : cast(rtData, t0, tt1))  : "$device")
		//def value = (attributeName ? cast(rtData, getDeviceAttributeValue(rtData, device, attributeName), (String)attribute.t) : "$device")
		if(attributeName == 'hue') {
			value = cast(rtData, (double)cast(rtData, value, 'decimal') * 3.6, (String)attribute.t)
		}
		//have to compare ids and type for hubitat since the locationid can be the same as the deviceid
		def tt0 = rtData.event?.device ?: location
		boolean deviceMatch = (device?.id == tt0.id) && ( isDeviceLocation(device) == isDeviceLocation(tt0) )
		return [t: (String)attribute.t, v: value, d: deviceId, a: attributeName, i: subDeviceIndex, x: (!!attribute.m || !!trigger) && (!deviceMatch || (((attributeName == 'orientation') || (attributeName == 'axisX') || (attributeName == 'axisY') || (attributeName == 'axisZ') ? 'threeAxis' : attributeName) != (String)rtData.event.name))]
	}
	return [t: "error", v: "Device '${deviceId}' not found"]
}

private Map getJsonData(Map rtData, data, String name, String feature = null) {
	if(data != null) {
	try {
		List parts = name.replace('][', '].[').tokenize('.')
		def args = (data instanceof Map ? [:] + data : (data instanceof List ? [] + data : new groovy.json.JsonSlurper().parseText(data)))
		int partIndex = -1
		for(part in parts) {
			partIndex = partIndex + 1
			if((args instanceof String) || (args instanceof GString)) {
				if(args.startsWith('{') && args.endsWith('}')) {
					args = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(args)
				} else if(args.startsWith('[') && args.endsWith(']')) {
					args = (List) new groovy.json.JsonSlurper().parseText(args)
				}
			}
			if(args instanceof List) {
				switch (part) {
				case 'length':
					return [t: 'integer', v: args.size()]
				case 'first':
					args = args.size() > 0 ? args[0] : ''
					continue
					break
				case 'second':
					args = args.size() > 1 ? args[1] : ''
					continue
					break
				case 'third':
					args = args.size() > 2 ? args[2] : ''
					continue
					break
				case 'fourth':
					args = args.size() > 3 ? args[3] : ''
					continue
					break
				case 'fifth':
					args = args.size() > 4 ? args[4] : ''
					continue
					break
				case 'sixth':
					args = args.size() > 5 ? args[5] : ''
					continue
					break
				case 'seventh':
					args = args.size() > 6 ? args[6] : ''
					continue
					break
				case 'eighth':
					args = args.size() > 7 ? args[7] : ''
					continue
					break
				case 'ninth':
					args = args.size() > 8 ? args[8] : ''
					continue
					break
				case 'tenth':
					args = args.size() > 9 ? args[9] : ''
					continue
					break
				case 'last':
					args = args.size() > 0 ? args[args.size() - 1] : ''
					continue
					break
				}
			}
			if(!(args instanceof Map) && !(args instanceof List)) return [t: 'dynamic', v: '']
			//nfl overrides
			boolean overrideArgs = false
			if((feature == 'NFL') && (partIndex == 1) && !!args && !!args.games) {
				def offset = null
				def start = null
				def end = null
				def date = localDate()
				int dow = date.day
				switch ("$part".tokenize('[')[0].toLowerCase()) {
				case 'yesterday':
					offset = -1
					break
				case 'today':
					offset = 0
					break
				case 'tomorrow':
					offset = 1
					break
				case 'mon':
				case 'monday':
					offset = dow <= 2 ? 1 - dow : 8 - dow
					break
				case 'tue':
				case 'tuesday':
					offset = dow <= 2 ? 2 - dow : 9 - dow
					break
				case 'wed':
				case 'wednesday':
					offset = dow <= 2 ? - 4 - dow : 3 - dow
					break
				case 'thu':
				case 'thursday':
					offset = dow <= 2 ? - 3 - dow : 4 - dow
					break
				case 'fri':
				case 'friday':
					offset = dow <= 2 ? - 2 - dow : 5 - dow
					break
				case 'sat':
				case 'saturday':
					offset = dow <= 2 ? - 1 - dow : 6 - dow
					break
				case 'sun':
				case 'sunday':
					offset = dow <= 2 ? 0 - dow : 7 - dow
					break
				case 'lastweek':
					start = (dow <= 2 ? - 4 - dow : 3 - dow) - 7
					end = (dow <= 2 ? 2 - dow : 9 - dow) - 7
					break
				case 'thisweek':
					start = dow <= 2 ? - 4 - dow : 3 - dow
					end = dow <= 2 ? 2 - dow : 9 - dow
					break
				case 'nextweek':
					start = (dow <= 2 ? - 4 - dow : 3 - dow) + 7
					end = (dow <= 2 ? 2 - dow : 9 - dow) + 7
					break
				}
				if(offset != null) {
					date.setTime(date.getTime() + offset * 86400000)
					def game = args.games.find{ (it.year == date.year + 1900) && (it.month == date.month + 1) && (it.day == date.date) }
					args = game
					continue
				}
				if(start != null) {
					def startDate = localDate()
					startDate.setTime(date.getTime() + start * 86400000)
					def endDate = localDate()
					endDate.setTime(date.getTime() + end * 86400000)
					start = (startDate.year + 1900) * 372 + (startDate.month * 31) + (startDate.date - 1)
					end = (endDate.year + 1900) * 372 + (endDate.month * 31) + (endDate.date - 1)
					if(parts[0].size() > 3) {
						def games = args.games.findAll{ (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) >= start) && (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) <= end) }
						args = games
						overrideArgs = true
					} else {
						def game = args.games.find{ (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) >= start) && (it.year * 372 + (it.month - 1) * 31 + (it.day - 1) <= end) }
						args = game
						continue
					}
				}
			}
			def idx = 0
			if(part.endsWith(']')) {
				//array index
				int start = part.indexOf('[')
				if(start >= 0) {
					idx = part.substring(start + 1, part.size() - 1)
					part = part.substring(0, start)
					if(idx.isInteger()) {
						idx = idx.toInteger()
					} else {
						def var = getVariable(rtData, (String)idx)
						idx = var && ((String)var.t != 'error') ? var.v : idx
					}
				}
				if(!overrideArgs && !!part) args = args[part]
				if(args instanceof List) idx = cast(rtData, idx, 'integer')
				args = args[idx]
				continue
			}
			if(!overrideArgs) args = args[part]
		}
		return [t: 'dynamic', v: "$args".toString()]
	} catch (all) {
		error "Error retrieving JSON data part $part", rtData
		return [t: 'dynamic', v: '']
	}
	}
	return [t: 'dynamic', v: '']
}

private Map getArgument(Map rtData, String name) {
	return getJsonData(rtData, rtData.args, name)
}

private Map getJson(Map rtData, String name) {
	return getJsonData(rtData, rtData.json, name)
}

private Map getPlaces(Map rtData, String name) {
	return getJsonData(rtData, rtData.settings?.places, name)
}

private Map getResponse(Map rtData, String name) {
	return getJsonData(rtData, rtData.response, name)
}

private Map getWeather(Map rtData, String name) {
	if(rtData.weather == null) {
		def t0 = parent.getWData()
		rtData.weather = t0 ?: [:]
	}
	return getJsonData(rtData, rtData.weather, name)
}

private Map getNFLDataFeature(String dataFeature) {
	def requestParams = [
		uri: "https://api.webcore.co/nfl/$dataFeature",
		query: method == "GET" ? data : null
	]
	httpGet(requestParams) { response ->
		if((response.status == 200) && response.data && !binary) {
			try {
				return response.data instanceof Map ? response.data : (LinkedHashMap) new groovy.json.JsonSlurper().parseText(response.data)
			} catch (all) {
				return null
			}
		}
		return null
	}
}

private Map getNFL(Map rtData, String name) {
	List parts = name.tokenize('.')
	rtData.nfl = rtData.nfl ?: [:]
	if(parts.size() > 0) {
		def dataFeature = parts[0].tokenize('[')[0]
		if(rtData.nfl[dataFeature] == null) {
			rtData.nfl[dataFeature] = getNFLDataFeature(dataFeature)
		}
	}
	return getJsonData(rtData, rtData.nfl, name, 'NFL')
}

@Field static boolean initGlobalFLD
@Field static Map globalVarsFLD

public void clearGlobalCache(String meth=null) {
	globalVarsFLD = [:]
	initGlobalFLD = false
	if(eric()) log.warn "clearing Global cache $meth"
}

private void loadGlobalCache() {
	if(!initGlobalFLD) {
		globalVarsFLD = parent.listAvailableVariables()
		initGlobalFLD = true
		if(eric()) log.warn "loading Global cache"
	}
}

private Map getVariable(Map rtData, String name) {
	def var = parseVariableName(name)
	name = sanitizeVariableName((String)var.name)
	if(!name) return [t: "error", v: "Invalid empty variable name"]
	def result
	String tname = name
	if(tname.startsWith("@")) {
		loadGlobalCache()
		result = globalVarsFLD[tname]
		result.v = cast(rtData, result.v, (String)result.t)
		if(!(result instanceof Map)) result = [t: "error", v: "Variable '$tname' not found"]
	} else {
		if(tname.startsWith('$')) {
			if(tname.startsWith('$args.') && (tname.size() > 6)) {
				result = getArgument(rtData, tname.substring(6))
			} else if(tname.startsWith('$args[') && (tname.size() > 6)) {
				result = getArgument(rtData, tname.substring(5))
			} else if(tname.startsWith('$json.') && (tname.size() > 6)) {
				result = getJson(rtData, tname.substring(6))
			} else if(tname.startsWith('$json[') && (tname.size() > 6)) {
				result = getJson(rtData, tname.substring(5))
			} else if(tname.startsWith('$places.') && (tname.size() > 8)) {
				result = getPlaces(rtData, tname.substring(7))
			} else if(tname.startsWith('$places[') && (tname.size() > 8)) {
				result = getPlaces(rtData, tname.substring(7))
			} else if(tname.startsWith('$response.') && (tname.size() > 10)) {
				result = getResponse(rtData, tname.substring(10))
			} else if(tname.startsWith('$response[') && (tname.size() > 10)) {
				result = getResponse(rtData, tname.substring(9))
			} else if(tname.startsWith('$nfl.') && (tname.size() > 5)) {
				result = getNFL(rtData, tname.substring(5))
			} else if(tname.startsWith('$weather.') && (tname.size() > 9)) {
				result = getWeather(rtData, tname.substring(9))
			} else {
				result = rtData.systemVars[tname]
				if(!(result instanceof Map)) result = [t: "error", v: "Variable '$tname' not found"]
				if(result && result.d) {
					result = [t: result.t, v: getSystemVariableValue(rtData, tname)]
				}
			}
		} else {
			def localVar = rtData.localVars[tname]
			if(!(localVar instanceof Map)) {
				result = [t: "error", v: "Variable '$tname' not found"]
			} else {
				result = [t: (String)localVar.t, v: localVar.v]
				//make a local copy of the list
				if(result.v instanceof List) result.v = [] + result.v
				//make a local copy of the map
				if(result.v instanceof Map) result.v = [:] + result.v
			}
		}
	}
	if(result && (result.t.endsWith(']'))) {
		result.t = result.t.replace('[]', '')
		if((result.v instanceof Map) && (var.index != null) && (var.index != '')) {
			Map indirectVar = getVariable(rtData, (String)var.index)
			//indirect variable addressing
			if(indirectVar && ((String)indirectVar.t != 'error')) {
				def value = (String)indirectVar.t == 'decimal' ? cast(rtData, indirectVar.v, 'integer', (String)indirectVar.t) : indirectVar.v
				String dataType = (String)indirectVar.t == 'decimal' ? 'integer' : (String)indirectVar.t
				var.index = cast(rtData, value, 'string', dataType)
			}
			result.v = result.v[var.index]
		}
	} else {
		if(result.v instanceof Map) {
			//we're dealing with an operand, let's parse it
			//result = evaluateExpression(rtData, evaluateOperand(rtData, null, result.v), (String)result.t)
			String tt0 = (String)result.t
			result = evaluateOperand(rtData, null, result.v)
			result = (tt0 && tt0 == (String)result.t) ? result : evaluateExpression(rtData, result, tt0)
		}
	}
	return [t: (String)result.t, v: result.v]
}

private Map setVariable(Map rtData, String name, value) {
	def var = parseVariableName(name)
	name = sanitizeVariableName((String)var.name)
	if(!name) return [t: "error", v: "Invalid empty variable name"]
	String tname = name
	if(tname.startsWith("@")) {
		loadGlobalCache()
		def variable = globalVarsFLD[tname]
		if(variable instanceof Map) {
			variable.v = cast(rtData, value, variable.t)
			Map cache = rtData.gvCache ?: [:]
			cache[tname] = variable
			rtData.gvCache = cache
			return variable
		}
	} else {
		def variable = rtData.localVars[tname]
		if(variable instanceof Map) {
			if(variable.t.endsWith(']')) {
				//we're dealing with a list
				variable.v = (variable.v instanceof Map) ? variable.v : [:]
				Map indirectVar = getVariable(rtData, (String)var.index)
				//indirect variable addressing
				if(indirectVar && (indirectVar.t != 'error')) {
					var.index = cast(rtData, indirectVar.v, 'string', indirectVar.t)
				}
				variable.v[var.index] = cast(rtData, value, variable.t.replace('[]', ''))
			} else {
				variable.v = cast(rtData, value, variable.t)
			}
			if(!variable.f) {
				def vars
				if((boolean)rtData.pep) vars = atomicState.vars
				else vars = state.vars
				vars[tname] = variable.v
				if((boolean)rtData.pep) atomicState.vars = vars
				else state.vars = vars
			}
			return variable

		}
	}
	result = [t: 'error', v: 'Invalid variable']
}

public def setLocalVariable(String name, value) {  // called by parent (IDE) to set value to a variable
	name = sanitizeVariableName(name)
	if(!name || name.startsWith('@')) return
	def vars = atomicState.vars ?: [:]
	vars[name] = value
	atomicState.vars = vars
	return vars
}

/******************************************************************************/
/*** EXPRESSION FUNCTIONS							***/
/******************************************************************************/

public Map proxyEvaluateExpression(Map rtData, Map expression, String dataType = (String)null) {
	rtData = getRunTimeData(rtData)
	resetRandomValues(rtData)
	try {
	def result = evaluateExpression(rtData, expression, dataType)
	if(((String)result.t == 'device') && ((String)result.a)) {
		//def attr = rtData.attributes[result.a]
		def attr = Attributes()[(String)result.a]
		result = evaluateExpression(rtData, result, attr && (String)attr.t ? (String)attr.t : 'string')
	}
	return result
	} catch (all) {
		error "An error occurred while executing the expression", rtData, null, all
	}
	return [t: 'error', v: 'expression error']
}

private Map simplifyExpression(Map expression) {
	while (((String)expression.t == 'expression') && expression.i && (expression.i.size() == 1)) expression = expression.i[0]
	return expression
}

private Map evaluateExpression(Map rtData, Map expression, String dataType = (String)null) {
	//if dealing with an expression that has multiple items, let's evaluate each item one by one
	//let's evaluate this expression
	if(!expression) return [t: 'error', v: 'Null expression']
	//not sure what it was needed for - need to comment more
	//if(expression && expression.v instanceof Map) return evaluateExpression(rtData, expression.v, expression.t)
	expression = simplifyExpression(expression)
	if((boolean)rtData.eric) log "evaluateExpression $expression   dataType: $dataType", null, 1, null, "warn", true
	long time = now()
	Map result = expression
	String exprType = (String)expression.t
	switch (exprType) {
	case "integer":
	case "long":
	case "decimal":
//ERS
		result = [t: exprType, v: expression.v]
		break
	case "time":
		def t0 = expression.v
		boolean found = false
		if("$t0".isNumber() && (t0 < 86400000)) found = true
		result = [t: exprType, v: found ? t0 as long : (long)cast(rtData, t0, exprType, exprType)]
		break
	case "datetime":
		def t0 = expression.v
		boolean found = false
		if("$t0".isNumber() && (t0 >= 86400000)) {
			result = [t: exprType, v: t0 as long ]
			break
		}
		//result = [t: exprType, v: found ? t0 as long : (long)cast(rtData, t0, exprType, exprType)]
		//break
	case "int32":
	case "int64":
	case "date":
		result = [t: exprType, v: cast(rtData, expression.v, exprType, exprType)]
		break
	case "bool":
	case "boolean":
		def t0 = expression.v
		if(t0 instanceof Boolean) {
			result = [t: 'boolean', v: (boolean)t0]
			break
		}
		result = [t: 'boolean', v: (boolean)cast(rtData, t0, exprType, exprType)]
		break
	case "string":
	case "enum":
	case "error":
	case "phone":
	case "uri":
	case "text":
		def t0 = expression.v
		if(t0 instanceof String) {
			result = [t: 'string', v: (String)t0]
			break
		}
		result = [t: 'string', v: (String)cast(rtData, t0, 'string', exprType)]
		break
	//case "bool":
		//result = [t: "boolean", v: (boolean)cast(rtData, expression.v, "boolean", exprType)]
		//break
	case "number":
	case "float":
	case "double":
		result = [t: "decimal", v: (double)cast(rtData, expression.v, "decimal", exprType)]
		break
	case "duration":
		String t0 = (String)expression.vt
		if(!t0 && expression.v instanceof Long) { result = [t: "long", v: (long)expression.v ] }
		else result = [t: 'long', v: (long)cast(rtData, expression.v, t0 ?: 'long') ]
		break
	case "variable":
		//get variable as {n: name, t: type, v: value}
		result = [t: 'error', v: 'Invalid variable']
		result = getVariable(rtData, expression.x + (expression.xi != null ? '[' + expression.xi + ']' : ''))
		break
	case "device":
		//get variable as {n: name, t: type, v: value}
		if(expression.v instanceof List) {
			//already parsed
			result = expression
		} else {
			def deviceIds = (expression.id instanceof List) ? expression.id : (expression.id ? [expression.id] : [])
			if(!deviceIds.size()) {
				def var = getVariable(rtData, (String)expression.x)
				if(var) {
					if((String)var.t == 'device') {
						deviceIds = var.v
					} else {
						def device = getDevice(rtData, var.v)
						if(device) deviceIds = [hashId(device.id)]
					}
				}
			}
			result = [t: 'device', v: deviceIds, a: (String)expression.a]
		}
		break
	case "operand":
		result = [t: "string", v: (String)cast(rtData, expression.v, "string")]
		break
	case "function":
		String fn = "func_${expression.n}"
		//in a function, we look for device parameters, they may be lists - we need to reformat all parameters to send them to the function properly
		try {
		List params = []
		if(expression.i && expression.i.size()) {
			for (i in expression.i) {
				Map param = simplifyExpression(i)
				if(((String)param.t == 'device') || ((String)param.t == 'variable')) {
					//if multiple devices involved, we need to spread the param into multiple params
					param = evaluateExpression(rtData, param)
					int sz = param.v instanceof List ? param.v.size() : 1
					switch (sz) {
						case 0: break
						case 1: params.push(param); break
						default:
							for (v in param.v) {
							params.push([t: (String)param.t, a: (String)param.a, v: [v]])
						}
					}
				} else {
					params.push(param)
				}
			}
		}
		if((boolean)rtData.eric) log "calling function $fn", null, 1, null, "warn", true
		result = (Map) "$fn"(rtData, params)
		} catch (all) {
			//log error
			result = [t: "error", v: all]
		}
		if((boolean)rtData.eric) log "calling function $fn", null, -1, null, "warn", true
		break
	case "expression":
		//if we have a single item, we simply traverse the expression
		List items = []
		int operand = -1
		int lastOperand = -1
		for(Map item in expression.i) {
			if((String)item.t == "operator") {
				if(operand < 0) {
					switch ((String)item.o) {
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
						items.push([t: 'integer', v: 0, o: (String)item.o])
						break
					case ':':
						if(lastOperand >= 0) {
							//groovy-style support for (object ?: value)
							items.push(items[lastOperand] + [o: (String)item.o])
						} else {
							items.push([t: 'integer', v: 0, o: (String)item.o])
						}
						break
					case '*':
					case '/':
						items.push([t: 'integer', v: 1, o: (String)item.o])
						break
					case '&&':
					case '!&':
						items.push([t: 'boolean', v: true, o: (String)item.o])
						break
					case '||':
					case '!|':
					case '^^':
					case '!^':
						items.push([t: 'boolean', v: false, o: (String)item.o])
						break
					}
				} else {
					items[operand].o = (String)item.o
					operand = -1
				}
			} else {
				items.push(evaluateExpression(rtData, item) + [:])
				operand = items.size() - 1
				lastOperand = operand
			}
		}
		//clean up operators, ensure there's one for each
		int idx = 0
		for(Map item in items) {
			if(!item.o) {
				switch ((String)item.t) {
					case "integer":
					case "float":
					case "double":
					case "decimal":
					case "number":
						String nextType = 'string'
						if(idx < items.size() - 1) nextType = (String)items[idx+1].t
						item.o = (nextType == 'string' || nextType == 'text') ? '+' : '*'
						break
					default:
						item.o = '+'
						break
				}
			}
			idx++
		}
		//do the job
		idx = 0
		boolean secondary = false
		while (items.size() > 1) {
			//ternary
			if((items.size() == 3) && ((String)items[0].o == '?') && ((String)items[1].o == ':')) {
				//we have a ternary operator
				if((boolean)evaluateExpression(rtData, items[0], 'boolean').v) {
					items = [items[1]]
				} else {
					items = [items[2]]
				}
				items[0].o = (String)null
				break
			}
			//order of operations :D
			idx = 0
			//#2	!   !!   ~   -	Logical negation, logical double-negation, bitwise NOT, and numeric negation unary operators
			for (Map item in items) {
				String t0 = (String)item.o
				if((t0 == '!') || (t0 == '!!') || (t0 == '~') || (item.t == null && t0 == '-')) break
				secondary = true
				idx++
			}
			//#3	**	Exponent operator
			if(idx >= items.size()) {
				//we then look for power **
				idx = 0
				for (Map item in items) {
					if(((String)item.o) == '**') break
					idx++
				}
			}
			//#4	*   /   \   % MOD	Multiplication, division, modulo
			if(idx >= items.size()) {
				//we then look for * or /
				idx = 0
				for (Map item in items) {
					String t0 = (String)item.o
					if((t0 == '*') || (t0 == '/') || (t0 == '\\') || (t0 == '%')) break
					idx++
				}
			}
			//#5	+   -	Addition and subtraction
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '+') || (((String)item.o) == '-')) break
					idx++
				}
			}
			//#6	<<   >>	Shift left and shift right operators
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '<<') || (((String)item.o) == '>>')) break
					idx++
				}
			}
			//#7	<   <=   >   >=	Comparisons: less than, less than or equal to, greater than, greater than or equal to
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					String t0 = (String)item.o
					if((t0 == '>') || (t0 == '<') || (t0 == '>=') || (t0 == '<=')) break
					idx++
				}
			}
			//#8	==   !=	Comparisons: equal and not equal
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					String t0 = (String)item.o
					if((t0 == '==') || (t0 == '!=') || (t0 == '<>')) break
					idx++
				}
			}
			//#9	&	Bitwise AND
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '&') || (((String)item.o) == '~&')) break
					idx++
				}
			}
			//#10	^	Bitwise exclusive OR (XOR)
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '^') || (((String)item.o) == '~^')) break
					idx++
				}
			}
			//#11	|	Bitwise inclusive (normal) OR
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '|') || (((String)item.o) == '~|')) break
					idx++
				}
			}
			//#12	&&	Logical AND
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '&&') || (((String)item.o) == '!&')) break
					idx++
				}
			}
			//#13	^^	Logical XOR
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '^^') || (((String)item.o) == '~^')) break
					idx++
				}
			}
			//#14	||	Logical OR
			if(idx >= items.size()) {
				idx = 0
				for (Map item in items) {
					if((((String)item.o) == '||') || (((String)item.o) == '!|')) break
					idx++
				}
			}
			if(idx >= items.size()) {
				//just get the first one
				idx = 0
			}
			if(idx >= items.size() - 1) idx = 0
			//we're onto something
			def v = null
			String o = (String)items[idx].o
			String a1 = (String)items[idx].a
			String t1 = (String)items[idx].t
			def v1 = items[idx].v
			String a2 = (String)items[idx + 1].a
			String t2 = (String)items[idx + 1].t
			def v2 = items[idx + 1].v
			String t = t1
			//fix-ups
			//integer with decimal gives decimal, also *, / require decimals
			if((t1 == 'device') && a1) {
				//def attr = rtData.attributes[a1]
				def attr = Attributes()[a1]
				t1 = attr ? (String)attr.t : 'string'
			}
			if((t2 == 'device') && a2) {
				def attr = Attributes()[a2]
				t2 = attr ? (String)attr.t : 'string'
			}
			if((t1 == 'device') && (t2 == 'device') && ((o == '+') || (o == '-'))) {
				v1 = (v1 instanceof List) ? v1 : [v1]
				v2 = (v2 instanceof List) ? v2 : [v2]
				v = (o == '+') ? v1 + v2 : v1 - v2
				//set the results
				items[idx + 1].t = 'device'
				items[idx + 1].v = v
			} else {
				boolean t1d = (t1 == 'datetime') || (t1 == 'date') || (t1 == 'time')
				boolean t2d = (t2 == 'datetime') || (t2 == 'date') || (t2 == 'time')
				boolean t1i = (t1 == 'number') || (t1 == 'integer') || (t1 == 'long')
				boolean t2i = (t2 == 'number') || (t2 == 'integer') || (t2 == 'long')
				boolean t1f = (t1 == 'decimal') || (t1 == 'float')
				boolean t2f = (t2 == 'decimal') || (t2 == 'float')
				boolean t1n = t1i || t1f
				boolean t2n = t2i || t2f
				//warn "Precalc ($t1) $v1 $o ($t2) $v2 >>> t1d = $t1d, t2d = $t2d, t1n = $t1n, t2n = $t2n", rtData
				if(((o == '+') || (o == '-')) && (t1d || t2d) && (t1d || t1n) && (t2n || t2d)) {
					//if dealing with date +/- date/numeric then
					if(t1n) {
						t = t2
					} else if(t2n) {
						t = t1
					} else {
						t = (t1 == 'date') && (t2 == 'date') ? 'date' : ((t1 == 'time') && (t2 == 'time') ? 'time' : 'datetime')
					}
				} else {
					if((o == '+') || (o == '-')) {
						//devices and others play nice
						if(t1 == 'device') {
							t = t2
							t1 = t2
						} else if(t2 == 'device') {
							t = t1
							t2 = t1
						}
					}
					if((o == '*') || (o == '/') || (o == '-') || (o == '**')) {
						t = (t1i && t2i) ? ((t1 == 'long') || (t2 == 'long') ? 'long' : 'integer') : 'decimal'
						t1 = t
						t2 = t
						//if((t1 != 'number') && (t1 != 'integer') && (t1 != 'decimal') && (t1 != 'float') && (t1 != 'datetime') && (t1 != 'date') && (t1 != 'time')) t1 = 'decimal'
						//if((t2 != 'number') && (t2 != 'integer') && (t2 != 'decimal') && (t2 != 'float') && (t2 != 'datetime') && (t2 != 'date') && (t2 != 'time')) t2 = 'decimal'
						//t = (t1 == 'datetime') || (t2 == 'datetime') ? 'datetime' : ((t1 == 'date') && (t2 == 'date') ? 'date' : ((t1 == 'time') && (t2 == 'time') ? 'time' : (((t1 == 'date') && (t2 == 'time')) || ((t1 == 'time') && (t2 == 'date')) ? 'datetime' : 'decimal')))
					}
					if((o == '\\') || (o == '%') || (o == '&') || (o == '|') || (o == '^') || (o == '~&') || (o == '~|') || (o == '~^') || (o == '<<') || (o == '>>')) {
						t = (t1 == 'long') || (t2 == 'long') ? 'long' : 'integer'
						t1 = t
						t2 = t
					}
					if((o == '&&') || (o == '||') || (o == '^^') || (o == '!&') || (o == '!|') || (o == '!^') || (o == '!') || (o == '!!')) {
						t1 = 'boolean'
						t2 = 'boolean'
						t = 'boolean'
					}
					if((o == '+') && ((t1 == 'string') || (t1 == 'text') || (t2 == 'string') || (t2 == 'text'))) {
						t1 = 'string'
						t2 = 'string'
						t = 'string'
					}
					if(t1n && t2n) {
						t = (t1i && t2i) ? ((t1 == 'long') || (t2 == 'long') ? 'long' : 'integer') : 'decimal'
						t1 = t
						t2 = t
					}
					if((o == '==') || (o == '!=') || (o == '<') || (o == '>') || (o == '<=') || (o == '>=') || (o == '<>')) {
						if(t1 == 'device') t1 = 'string'
						if(t2 == 'device') t2 = 'string'
						t1 = t1 == 'string' ? t2 : t1
						t2 = t2 == 'string' ? t1 : t2
						t = 'boolean'
					}
				}
				v1 = evaluateExpression(rtData, items[idx], t1).v
				v2 = evaluateExpression(rtData, items[idx + 1], t2).v
				v1 = v1 == "null" ? null : v1
				v2 = v2 == "null" ? null : v2
				switch (o) {
					case '?':
					case ':':
						error "Invalid ternary operator. Ternary operator's syntax is ( condition ? trueValue : falseValue ). Please check your syntax and try again.", rtData
						v = ''
						break
					case '-':
						v = v1 - v2
						break
					case '*':
						v = v1 * v2
						break
					case '/':
						v = (v2 != 0 ? v1 / v2 : 0)
						break
					case '\\':
						v = (int)Math.floor(v2 != 0 ? v1 / v2 : 0)
						break
					case '%':
						v = (int)(v2 != 0 ? v1 % v2 : 0)
						break
					case '**':
						v = v1 ** v2
						break
					case '&':
						v = v1 & v2
						break
					case '|':
						v = v1 | v2
						break
					case '^':
						v = v1 ^ v2
						break
					case '~&':
						v = ~(v1 & v2)
						break
					case '~|':
						v = ~(v1 | v2)
						break
					case '~^':
						v = ~(v1 ^ v2)
						break
					case '~':
						v = ~v2
						break
					case '<<':
						v = v1 << v2
						break
					case '>>':
						v = v1 >> v2
						break
					case '&&':
						v = !!v1 && !!v2
						break
					case '||':
						v = !!v1 || !!v2
						break
					case '^^':
						v = !v1 != !v2
						break
					case '!&':
						v = !(!!v1 && !!v2)
						break
					case '!|':
						v = !(!!v1 || !!v2)
						break
					case '!^':
						v = !(!v1 != !v2)
						break
					case '==':
						v = v1 == v2
						break
					case '!=':
					case '<>':
						v = v1 != v2
						break
					case '<':
						v = v1 < v2
						break
					case '>':
						v = v1 > v2
						break
					case '<=':
						v = v1 <= v2
						break
					case '>=':
						v = v1 >= v2
						break
					case '!':
						v = !v2
						break
					case '!!':
						v = !!v2
						break
					case '+':
					default:
						v = t == 'string' ? "$v1$v2" : v1 + v2
						break
				}

				if((int)rtData.logging > 2) debug "Calculating ($t1) $v1 $o ($t2) $v2 >> ($t) $v", rtData

				//set the results
				items[idx + 1].t = t
				v = (v instanceof GString) ? "$v".toString() : v
				boolean match = v && ( (v instanceof String && t == 'string') ||
							(v instanceof Long && t == 'long') ||
							(v instanceof Integer && t == 'integer') ||
							(v instanceof Double && t == 'decimal')  )
				if(match) items[idx + 1].v = v
				else items[idx + 1].v = cast(rtData, v, t)
				//items[idx + 1].v = cast(rtData, v, t)
			}
			int sz = items.size()
			items.remove(idx)
		}
		result = items[0] ? (((String)items[0].t == 'device') ? items[0] : evaluateExpression(rtData, items[0])) : [t: 'dynamic', v: null]
		break
	}
	//return the value, either directly or via cast, if certain data type is requested
	//when dealing with devices, they need to be "converted" unless the request is to return devices
	if(dataType && (dataType != 'device') && ((String)result.t == 'device')) {
		//if not a list, make it a list
		if(!(result.v instanceof List)) result.v = [result.v]
		switch (result.v.size()) {
			case 0: result = [t: 'error', v: 'Empty device list']; break
			case 1: result = getDeviceAttribute(rtData, result.v[0], (String)result.a, result.i); break
			default: result = [t: 'string', v: buildDeviceAttributeList(rtData, result.v, (String)result.a)]; break
		}
	}
	if(dataType) {
		String t0 = (String)result.t
		def t1 = result.v
		if(dataType != t0) {
			boolean match =  (dataType in ['string', 'enum']) && (t0 in ['string', 'enum'])
			if(!match) t1 = cast(rtData, result.v, dataType, t0)
		}
		//result = [t: dataType, v: cast(rtData, result.v, dataType, (String)result.t)] + (result.a ? [a: (String)result.a] : [:]) + (result.i ? [a: result.i] : [:])
		result = [t: dataType, v: t1] + (result.a ? [a: (String)result.a] : [:]) + (result.i ? [a: result.i] : [:])
	}
	result.d = now() - time
	if((boolean)rtData.eric) log "evaluateExpression $expression  result: $result", null, -1, null, "warn", true
	return result
}

private String buildList(list, String suffix = 'and') {
	if(!list) return ''
	if(!(list instanceof List)) list = [list]
	int cnt = 1
	String result = ""
	for (item in list) {
		result += "$item" + (cnt < list.size() ? (cnt == list.size() - 1 ? " $suffix " : ", ") : "")
		cnt++
	}
	return result
}

private String buildDeviceList(Map rtData, devices, String suffix = 'and') {
	if(!devices) return ''
	if(!(devices instanceof List)) devices = [devices]
	def list = []
	for (device in devices) {
		def dev = getDevice(rtData, device)
		if(dev) list.push(dev)
	}
	return buildList(list, suffix)
}

private String buildDeviceAttributeList(Map rtData, devices, String attribute, String suffix = 'and') {
	if(!devices) return ''
	if(!(devices instanceof List)) devices = [devices]
	def list = []
	for (device in devices) {
		def value = getDeviceAttribute(rtData, device, attribute).v
		list.push(value)
	}
	return buildList(list, suffix)
}


/******************************************************************************/
/*** dewPoint returns the calculated dew point temperature					***/
/*** Usage: dewPoint(temperature, relativeHumidity[, scale])				***/
/******************************************************************************/
private Map func_dewpoint(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting dewPoint(temperature, relativeHumidity[, scale])"]
	}
	double t = (double)evaluateExpression(rtData, params[0], 'decimal').v
	double rh = (double)evaluateExpression(rtData, params[1], 'decimal').v
	//if no temperature scale is provided, we assume the location's temperature scale
	boolean fahrenheit = ((String)cast(rtData, params.size() > 2 ? evaluateExpression(rtData, params[2]).v : location.temperatureScale, "string")).toUpperCase() == "F"
	if(fahrenheit) {
		t = (t - 32.0) * 5.0 / 9.0
	}
	//convert rh to percentage
	if((rh > 0) && (rh < 1)) {
		rh = rh * 100.0
	}
	double b = (Math.log(rh / 100) + ((17.27 * t) / (237.3 + t))) / 17.27
	double result = (237.3 * b) / (1 - b)
	if(fahrenheit) {
		result = result * 9.0 / 5.0 + 32.0
	}
	return [t: "decimal", v: result]
}

/******************************************************************************/
/*** celsius converts temperature from Fahrenheit to Celsius				***/
/*** Usage: celsius(temperature)											***/
/******************************************************************************/
private Map func_celsius(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting celsius(temperature)"]
	}
	double t = (double)evaluateExpression(rtData, params[0], 'decimal').v
	//convert temperature to Celsius
	return [t: "decimal", v: (double)(t - 32.0) * 5.0 / 9.0]
}


/******************************************************************************/
/*** fahrenheit converts temperature from Celsius to Fahrenheit			***/
/*** Usage: fahrenheit(temperature)						***/
/******************************************************************************/
private Map func_fahrenheit(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting fahrenheit(temperature)"]
	}
	double t = (double)evaluateExpression(rtData, params[0], 'decimal').v
	//convert temperature to Fahrenheit
	return [t: "decimal", v: (double)t * 9.0 / 5.0 + 32.0]
}

/******************************************************************************/
/*** fahrenheit converts temperature between Celsius and Fahrenheit if the  ***/
/*** units differ from location.temperatureScale			***/
/*** Usage: convertTemperatureIfNeeded(celsiusTemperature, 'C')		***/
/******************************************************************************/
private Map func_converttemperatureifneeded(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting convertTemperatureIfNeeded(temperature, unit)"]
	}
	double t = (double)evaluateExpression(rtData, params[0], 'decimal').v
	String u = ((String)evaluateExpression(rtData, params[1], 'string').v).toUpperCase()
	//convert temperature to Fahrenheit
	switch (location.temperatureScale) {
		case u: return [t: "decimal", v: t]
		case 'F': return func_celsius(rtData, [params[0]])
		case 'C': return func_fahrenheit(rtData, [params[0]])
	}
}

/******************************************************************************/
/*** integer converts a decimal to integer value			***/
/*** Usage: integer(decimal or string)						***/
/******************************************************************************/
private Map func_integer(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting integer(decimal or string)"]
	}
	return [t: "integer", v: (int)evaluateExpression(rtData, params[0], 'integer').v]
}
private Map func_int(Map rtData, List params) { return func_integer(rtData, params) }

/******************************************************************************/
/*** decimal/float converts an integer value to it's decimal value		***/
/*** Usage: decimal(integer or string)						***/
/******************************************************************************/
private Map func_decimal(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting decimal(integer or string)"]
	}
	return [t: "decimal", v: (double)evaluateExpression(rtData, params[0], 'decimal').v]
}
private Map func_float(Map rtData, List params) { return func_decimal(rtData, params) }
private Map func_number(Map rtData, List params) { return func_decimal(rtData, params) }

/******************************************************************************/
/*** string converts an value to it's string value				***/
/*** Usage: string(anything)							***/
/******************************************************************************/
private Map func_string(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting string(anything)"]
	}
	String result = ''
	for(param in params) {
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t: "string", v: result]
}
private Map func_concat(Map rtData, List params) { return func_string(rtData, params) }
private Map func_text(Map rtData, List params) { return func_string(rtData, params) }

/******************************************************************************/
/*** boolean converts a value to it's boolean value				***/
/*** Usage: boolean(anything)							***/
/******************************************************************************/
private Map func_boolean(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting boolean(anything)"]
	}
	return [t: "boolean", v: (boolean)evaluateExpression(rtData, params[0], 'boolean').v]
}
private Map func_bool(Map rtData, List params) { return func_boolean(rtData, params) }

/******************************************************************************/
/*** sqr converts a decimal to square decimal value			***/
/*** Usage: sqr(integer or decimal or string)					***/
/******************************************************************************/
private Map func_sqr(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting sqr(integer or decimal or string)"]
	}
	return [t: "decimal", v: (double)evaluateExpression(rtData, params[0], 'decimal').v ** 2]
}

/******************************************************************************/
/*** sqrt converts a decimal to square root decimal value		***/
/*** Usage: sqrt(integer or decimal or string)					***/
/******************************************************************************/
private Map func_sqrt(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting sqrt(integer or decimal or string)"]
	}
	return [t: "decimal", v: Math.sqrt( (double)evaluateExpression(rtData, params[0], 'decimal').v)]
}

/******************************************************************************/
/*** power converts a decimal to power decimal value			***/
/*** Usage: power(integer or decimal or string, power)				***/
/******************************************************************************/
private Map func_power(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting sqrt(integer or decimal or string, power)"]
	}
	return [t: "decimal", v: (double)evaluateExpression(rtData, params[0], 'decimal').v ** (double)evaluateExpression(rtData, params[1], 'decimal').v]
}

/******************************************************************************/
/*** round converts a decimal to rounded value			***/
/*** Usage: round(decimal or string[, precision])				***/
/******************************************************************************/
private Map func_round(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting round(decimal or string[, precision])"]
	}
	int precision = (params.size() > 1) ? (int)evaluateExpression(rtData, params[1], 'integer').v : 0
	return [t: "decimal", v: Math.round( (double)evaluateExpression(rtData, params[0], 'decimal').v * (10 ** precision)) / (10 ** precision)]
}

/******************************************************************************/
/*** floor converts a decimal to closest lower integer value		***/
/*** Usage: floor(decimal or string)						***/
/******************************************************************************/
private Map func_floor(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting floor(decimal or string)"]
	}
	return [t: "integer", v: (int)cast(rtData, Math.floor((double)evaluateExpression(rtData, params[0], 'decimal').v), 'integer')]
}

/******************************************************************************/
/*** ceiling converts a decimal to closest higher integer value	***/
/*** Usage: ceiling(decimal or string)						***/
/******************************************************************************/
private Map func_ceiling(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting ceiling(decimal or string)"]
	}
	return [t: "integer", v: (int)cast(rtData, Math.ceil((double)evaluateExpression(rtData, params[0], 'decimal').v), 'integer')]
}
private Map func_ceil(Map rtData, List params) { return func_ceiling(rtData, params) }


/******************************************************************************/
/*** sprintf converts formats a series of values into a string			***/
/*** Usage: sprintf(format, arguments)						***/
/******************************************************************************/
private Map func_sprintf(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting sprintf(format, arguments)"]
	}
	String format = (String)evaluateExpression(rtData, params[0], 'string').v
	List args = []
	for (int x = 1; x < params.size(); x++) {
		args.push(evaluateExpression(rtData, params[x]).v)
	}
	try {
		return [t: "string", v: sprintf(format, args)]
	} catch(all) {
		return [t: "error", v: "$all"]
	}
}
private Map func_format(Map rtData, List params) { return func_sprintf(rtData, params) }

/******************************************************************************/
/*** left returns a substring of a value					***/
/*** Usage: left(string, count)							***/
/******************************************************************************/
private Map func_left(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting left(string, count)"]
	}
	String value = (String)evaluateExpression(rtData, params[0], 'string').v
	int count = (int)evaluateExpression(rtData, params[1], 'integer').v
	if(count > value.size()) count = value.size()
	return [t: "string", v: value.substring(0, count)]
}

/******************************************************************************/
/*** right returns a substring of a value					***/
/*** Usage: right(string, count)						***/
/******************************************************************************/
private Map func_right(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting right(string, count)"]
	}
	String value = (String)evaluateExpression(rtData, params[0], 'string').v
	int count = (int)evaluateExpression(rtData, params[1], 'integer').v
	if(count > value.size()) count = value.size()
	return [t: "string", v: value.substring(value.size() - count, value.size())]
}

/******************************************************************************/
/*** strlen returns the length of a string value				***/
/*** Usage: strlen(string)							***/
/******************************************************************************/
private Map func_strlen(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting strlen(string)"]
	}
	String value = (String)evaluateExpression(rtData, params[0], 'string').v
	return [t: "integer", v: value.size()]
}
private Map func_length(Map rtData, List params) { return func_strlen(rtData, params) }

/******************************************************************************/
/*** coalesce returns the first non-empty parameter				***/
/*** Usage: coalesce(value1[, value2[, ..., valueN]])				***/
/******************************************************************************/
private Map func_coalesce(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting coalesce(value1[, value2[, ..., valueN]])"]
	}
	for (i = 0; i < params.size(); i++) {
		def value = evaluateExpression(rtData, params[0])
		if(!((value.v instanceof List ? (value.v == [null]) || (value.v == []) || (value.v == ['null']) : false) || (value.v == null) || ((String)value.t == 'error') || (value.v == 'null') || ((String)cast(rtData, value.v, 'string') == ''))) {
			return value
		}
	}
	return [t: "dynamic", v: null]
}

/******************************************************************************/
/*** trim removes leading and trailing spaces from a string			***/
/*** Usage: trim(value)								***/
/******************************************************************************/
private Map func_trim(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting trim(value)"]
	}
	String t0 = (String)evaluateExpression(rtData, params[0], 'string').v
	String value = (String)t0.trim()
	return [t: "string", v: value]
}

/******************************************************************************/
/*** trimleft removes leading spaces from a string				***/
/*** Usage: trimLeft(value)							***/
/******************************************************************************/
private Map func_trimleft(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting trimLeft(value)"]
	}
	String t0 = (String)evaluateExpression(rtData, params[0], 'string').v
	String value = (String)t0.replaceAll('^\\s+', '')
	return [t: "string", v: value]
}
private Map func_ltrim(Map rtData, List params) { return func_trimleft(rtData, params) }

/******************************************************************************/
/*** trimright removes trailing spaces from a string				***/
/*** Usage: trimRight(value)							***/
/******************************************************************************/
private Map func_trimright(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting trimRight(value)"]
	}
	String t0 = (String)evaluateExpression(rtData, params[0], 'string').v
	String value = (String)t0.replaceAll('\\s+$', '')
	return [t: "string", v: value]
}
private Map func_rtrim(Map rtData, List params) { return func_trimright(rtData, params) }

/******************************************************************************/
/*** substring returns a substring of a value					***/
/*** Usage: substring(string, start, count)					***/
/******************************************************************************/
private Map func_substring(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting substring(string, start, count)"]
	}
	String value = (String)evaluateExpression(rtData, params[0], 'string').v
	int start = (int)evaluateExpression(rtData, params[1], 'integer').v
	int count = params.size() > 2 ? (int)evaluateExpression(rtData, params[2], 'integer').v : null
	//def end = null
	String result = ''
	if((start < value.size()) && (start > -value.size())) {
		if(count != null) {
			if(count < 0) {
				//reverse
				start = start < 0 ? -start : value.size() - start
				count = - count
				value = value.reverse()
			}
			if(start >= 0) {
				if(count > value.size() - start) count = value.size() - start
			} else {
				if(count > -start) count = -start
			}
		}
		start = start >= 0 ? start : value.size() + start
		if(count > value.size() - start) count = value.size() - start
		result = (count == null) ? value.substring(start) : value.substring(start, start + count)
	}
	return [t: "string", v: result]
}
private Map func_substr(Map rtData, List params) { return func_substring(rtData, params) }
private Map func_mid(Map rtData, List params) { return func_substring(rtData, params) }

/******************************************************************************/
/*** replace replaces a search text inside of a value				***/
/*** Usage: replace(string, search, replace[, [..], search, replace])		***/
/******************************************************************************/
private Map func_replace(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 3) || (params.size() %2 != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting replace(string, search, replace[, [..], search, replace])"]
	}
	String value = (String)evaluateExpression(rtData, params[0], 'string').v
	int cnt = Math.floor((params.size() - 1) / 2)
	for (int i = 0; i < cnt; i++) {
		String search = (String)evaluateExpression(rtData, params[i * 2 + 1], 'string').v
		String replace = (String)evaluateExpression(rtData, params[i * 2 + 2], 'string').v
		if((search.size() > 2) && search.startsWith('/') && search.endsWith('/')) {
			search = ~search.substring(1, search.size() - 1)
			value = value.replaceAll(search, replace)
		} else {
			value = value.replace(search, replace)
		}
	}
	return [t: "string", v: value]
}

/******************************************************************************/
/*** rangeValue returns the matching value in a range				***/
/*** Usage: rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])***/
/******************************************************************************/
private Map func_rangevalue(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2) || (params.size() %2 != 0)) {
		return [t: "error", v: "Invalid parameters. Expecting rangeValue(input, defaultValue, point1, value1[, [..], pointN, valueN])"]
	}
	double input = (double)evaluateExpression(rtData, params[0], 'decimal').v
	def value = params[1]
	int cnt = Math.floor((params.size() - 2) / 2)
	for (int i = 0; i < cnt; i++) {
		double point = (double)evaluateExpression(rtData, params[i * 2 + 2], 'decimal').v
		if(input >= point) value = params[i * 2 + 3]
	}
	return value
}

/******************************************************************************/
/*** rainbowValue returns the matching value in a range				***/
/*** Usage: rainbowValue(input, minInput, minColor, maxInput, maxColor)		***/
/******************************************************************************/
private Map func_rainbowvalue(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 5)) {
		return [t: "error", v: "Invalid parameters. Expecting rainbowValue(input, minColor, minValue, maxInput, maxColor)"]
	}
	int input = (int)evaluateExpression(rtData, params[0], 'integer').v
	int minInput = (int)evaluateExpression(rtData, params[1], 'integer').v
	def minColor = getColor(rtData, (String)evaluateExpression(rtData, params[2], 'string').v)
	int maxInput = (int)evaluateExpression(rtData, params[3], 'integer').v
	def maxColor = getColor(rtData, (String)evaluateExpression(rtData, params[4], 'string').v)
	if(minInput > maxInput) {
		int x = minInput
		minInput = maxInput
		maxInput = x
		def x1 = minColor
		minColor = maxColor
		maxColor = x1
	}
	input = (input < minInput ? minInput : (input > maxInput ? maxInput : input))
	if((input == minInput) || (minInput == maxInput)) return [t: "string", v: minColor.hex]
	if(input == maxInput) return [t: "string", v: maxColor.hex]
	List start = hexToHsl((String)minColor.hex)
	List end = hexToHsl((String)maxColor.hex)
	float alpha = 1.0000000 * (input - minInput) / (maxInput - minInput + 1)
	int h = Math.round(start[0] - ((input - minInput) * (start[0] - end[0]) / (maxInput - minInput)))
	int s = Math.round(start[1] + (end[1] - start[1]) * alpha)
	int l = Math.round(start[2] + (end[2] - start[2]) * alpha)
	return [t: "string", v: hslToHex(h,s,l)]
}

/******************************************************************************/
/*** indexOf finds the first occurrence of a substring in a string		***/
/*** Usage: indexOf(stringOrDeviceOrList, substringOrItem)			***/
/******************************************************************************/
private Map func_indexof(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2) || ((params[0].t != 'device') && (params.size() != 2))) {
		return [t: "error", v: "Invalid parameters. Expecting indexOf(stringOrDeviceOrList, substringOrItem)"]
	}
	if((params[0].t == 'device') && (params.size() > 2)) {
		String item = (String)evaluateExpression(rtData, params[params.size() - 1], 'string').v
		for (int idx = 0; idx < params.size() - 1; idx++) {
			def it = evaluateExpression(rtData, params[idx], 'string')
			if(it.v == item) {
				return [t: "integer", v: idx]
			}
		}
		return [t: "integer", v: -1]
	} else if(params[0].v instanceof Map) {
		def item = evaluateExpression(rtData, params[1], (String)params[0].t).v
		def key = params[0].v.find{ it.value == item }?.key
		return [t: "string", v: key]
	} else {
		String value = (String)evaluateExpression(rtData, params[0], 'string').v
		String substring = (String)evaluateExpression(rtData, params[1], 'string').v
		return [t: "integer", v: value.indexOf(substring)]
	}
}

/******************************************************************************/
/*** lastIndexOf finds the first occurrence of a substring in a string		***/
/*** Usage: lastIndexOf(string, substring)									***/
/******************************************************************************/
private Map func_lastindexof(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2) || ((params[0].t != 'device') && (params.size() != 2))) {
		return [t: "error", v: "Invalid parameters. Expecting lastIndexOf(string, substring)"]
	}
	if((params[0].t == 'device') && (params.size() > 2)) {
		String item = (String)evaluateExpression(rtData, params[params.size() - 1], 'string').v
		for (int idx = params.size() - 2; idx >= 0; idx--) {
			def it = evaluateExpression(rtData, params[idx], 'string')
			if(it.v == item) {
				return [t: "integer", v: idx]
			}
		}
		return [t: "integer", v: -1]
	} else if(params[0].v instanceof Map) {
		String item = evaluateExpression(rtData, params[1], (String)params[0].t).v
		def key = params[0].v.find{ it.value == item }?.key
		return [t: "string", v: key]
	} else {
		String value = (String)evaluateExpression(rtData, params[0], 'string').v
		String substring = (String)evaluateExpression(rtData, params[1], 'string').v
		return [t: "integer", v: value.lastIndexOf(substring)]
	}
}


/******************************************************************************/
/*** lower returns a lower case value of a string				***/
/*** Usage: lower(string)							***/
/******************************************************************************/
private Map func_lower(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting lower(string)"]
	}
	String result = ''
	for(param in params) {
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t: "string", v: result.toLowerCase()]
}

/******************************************************************************/
/*** upper returns a upper case value of a string				***/
/*** Usage: upper(string)							***/
/******************************************************************************/
private Map func_upper(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting upper(string)"]
	}
	String result = ''
	for(param in params) {
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t: "string", v: result.toUpperCase()]
}

/******************************************************************************/
/*** title returns a title case value of a string				***/
/*** Usage: title(string)							***/
/******************************************************************************/
private Map func_title(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting title(string)"]
	}
	String result = ''
	for(param in params) {
		result += (String)evaluateExpression(rtData, param, 'string').v
	}
	return [t: "string", v: result.tokenize(" ")*.toLowerCase()*.capitalize().join(" ")]
}

/******************************************************************************/
/*** avg calculates the average of a series of numeric values			***/
/*** Usage: avg(values)								***/
/******************************************************************************/
private Map func_avg(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting avg(value1, value2, ..., valueN)"]
	}
	double sum = 0
	for (param in params) {
		sum += (double)evaluateExpression(rtData, param, 'decimal').v
	}
	return [t: "decimal", v: sum / params.size()]
}

/******************************************************************************/
/*** median returns the value in the middle of a sorted array			***/
/*** Usage: median(values)							***/
/******************************************************************************/
private Map func_median(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting median(value1, value2, ..., valueN)"]
	}
	List data = params.collect{ evaluateExpression(rtData, it, 'dynamic') }.sort{ it.v }
	if(data.size()) {
		return data[(int)Math.floor(data.size() / 2)]
	}
	return [t: 'dynamic', v: '']
}


/******************************************************************************/
/*** least returns the value that is least found a series of numeric values	***/
/*** Usage: least(values)							***/
/******************************************************************************/
private Map func_least(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting least(value1, value2, ..., valueN)"]
	}
	Map data = [:]
	for (param in params) {
		def value = evaluateExpression(rtData, param, 'dynamic')
		data[value.v] = [t: (String)value.t, v: value.v, c: (data[value.v]?.c ?: 0) + 1]
	}
	def value = data.sort{ it.value.c }.collect{ it.value }[0]
	return [t: (String)value.t, v: value.v]
}

/******************************************************************************/
/*** most returns the value that is most found a series of numeric values	***/
/*** Usage: most(values)							***/
/******************************************************************************/
private Map func_most(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting most(value1, value2, ..., valueN)"]
	}
	Map data = [:]
	for (param in params) {
		def value = evaluateExpression(rtData, param, 'dynamic')
		data[value.v] = [t: (String)value.t, v: value.v, c: (data[value.v]?.c ?: 0) + 1]
	}
	def value = data.sort{ - it.value.c }.collect{ it.value }[0]
	return [t: (String)value.t, v: value.v]
}

/******************************************************************************/
/*** sum calculates the sum of a series of numeric values			***/
/*** Usage: sum(values)								***/
/******************************************************************************/
private Map func_sum(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting sum(value1, value2, ..., valueN)"]
	}
	double sum = 0
	for (param in params) {
		sum += (double)evaluateExpression(rtData, param, 'decimal').v
	}
	return [t: "decimal", v: sum]
}

/******************************************************************************/
/*** variance calculates the standard deviation of a series of numeric values */
/*** Usage: stdev(values)							***/
/******************************************************************************/
private Map func_variance(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting variance(value1, value2, ..., valueN)"]
	}
	double sum = 0
	List values = []
	for (param in params) {
		double value = (double)evaluateExpression(rtData, param, 'decimal').v
		values.push(value)
		sum += value
	}
	double avg = sum / values.size()
	sum = 0
	for(int i = 0; i < values.size(); i++) {
		sum += ((double)values[i] - avg) ** 2
	}
	return [t: "decimal", v: sum / values.size()]
}

/******************************************************************************/
/*** stdev calculates the standard deviation of a series of numeric values	***/
/*** Usage: stdev(values)							***/
/******************************************************************************/
private Map func_stdev(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting stdev(value1, value2, ..., valueN)"]
	}
	def result = func_variance(rtData, params)
	return [t: "decimal", v: Math.sqrt(result.v)]
}

/******************************************************************************/
/*** min calculates the minimum of a series of numeric values				***/
/*** Usage: min(values)								***/
/******************************************************************************/
private Map func_min(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting min(value1, value2, ..., valueN)"]
	}
	List data = params.collect{ evaluateExpression(rtData, it, 'dynamic') }.sort{ it.v }
	if(data.size()) {
		return data[0]
	}
	return [t: 'dynamic', v: '']
}

/******************************************************************************/
/*** max calculates the maximum of a series of numeric values				***/
/*** Usage: max(values)								***/
/******************************************************************************/
private Map func_max(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "error", v: "Invalid parameters. Expecting max(value1, value2, ..., valueN)"]
	}
	List data = params.collect{ evaluateExpression(rtData, it, 'dynamic') }.sort{ it.v }
	if(data.size()) {
		return data[data.size() - 1]
	}
	return [t: 'dynamic', v: '']
}

/******************************************************************************/
/*** abs calculates the absolute value of a number							***/
/*** Usage: abs(number)								***/
/******************************************************************************/
private Map func_abs(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting abs(value)"]
	}
	double value = (double)evaluateExpression(rtData, params[0], 'decimal').v
	String dataType = (value == Math.round(value) ? 'integer' : 'decimal')
	return [t: dataType, v: (double)cast(rtData, Math.abs(value), dataType, 'decimal')]
}

/******************************************************************************/
/*** hslToHex converts a hue/saturation/level trio to it hex #rrggbb representation ***/
/*** Usage: hslToHex(hue, saturation, level)								***/
/******************************************************************************/
private Map func_hsltohex(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 3)) {
		return [t: "error", v: "Invalid parameters. Expecting hsl(hue, saturation, level)"]
	}
	float hue = (double)evaluateExpression(rtData, params[0], 'decimal').v
	float saturation = (double)evaluateExpression(rtData, params[1], 'decimal').v
	float level = (double)evaluateExpression(rtData, params[2], 'decimal').v
	return [t: 'string', v: hslToHex(hue, saturation, level)]
}

/******************************************************************************/
/*** count calculates the number of true/non-zero/non-empty items in a series of numeric values		***/
/*** Usage: count(values)								***/
/******************************************************************************/
private Map func_count(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "integer", v: 0]
	}
	int count = 0
	if((params.size() == 1) && (((String)params[0].t == 'string') || ((String)params[0].t == 'dynamic'))) {
		def list = ((String)evaluateExpression(rtData, params[0], 'string').v).split(',').toList()
		for (int i=0; i< list.size(); i++) {
			count += (boolean)cast(rtData, list[i], 'boolean') ? 1 : 0
		}
	} else {
		for (param in params) {
			count += ((boolean)evaluateExpression(rtData, param, 'boolean').v) ? 1 : 0
		}
	}
	return [t: "integer", v: count]
}

/******************************************************************************/
/*** size returns the number of values provided								***/
/*** Usage: size(values)							***/
/******************************************************************************/
private Map func_size(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1)) {
		return [t: "integer", v: 0]
	}
	int count = 0
	if((params.size() == 1) && ((params[0].t == 'string') || (params[0].t == 'dynamic'))) {
		def list = ((String)evaluateExpression(rtData, params[0], 'string').v).split(',').toList()
		count = list.size()
	} else {
		count = params.size()
	}
	return [t: "integer", v: count]
}

/******************************************************************************/
/*** age returns the number of milliseconds an attribute had the current value*/
/*** Usage: age([device:attribute])											***/
/******************************************************************************/
private Map func_age(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting age([device:attribute])"]
	}
	def param = evaluateExpression(rtData, params[0], 'device')
	if((param.t == 'device') && (param.a) && param.v.size()) {
		def device = getDevice(rtData, param.v[0])
		if(device) {
			def dstate = device.currentState(param.a, true)
			if(dstate) {
				long result = now() - dstate.getDate().getTime()
				return [t: "long", v: result]
			}
		}
	}
	return [t: "error", v: "Invalid device"]
}

/******************************************************************************/
/*** previousAge returns the number of milliseconds an attribute had the	***/
/*** previous value								***/
/*** Usage: previousAge([device:attribute])									***/
/******************************************************************************/
private Map func_previousage(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting previousAge([device:attribute])"]
	}
	def param = evaluateExpression(rtData, params[0], 'device')
	if((param.t == 'device') && (param.a) && param.v.size()) {
		def device = getDevice(rtData, param.v[0])
		if(device && !isDeviceLocation(device)) {
			def states = device.statesSince(param.a, new Date(now() - 604500000), [max: 5])
			if(states.size() > 1) {
				def newValue = states[0].getValue()
				//some events get duplicated, so we really want to look for the last "different valued" state
				for(int i = 1; i < states.size(); i++) {
					if(states[i].getValue() != newValue) {
						def result = now() - states[i].getDate().getTime()
						return [t: "long", v: result]
					}
				}
			}
			//we're saying 7 days, though it may be wrong - but we have no data
			return [t: "long", v: 604800000]
		}
	}
	return [t: "error", v: "Invalid device"]
}

/******************************************************************************/
/*** previousValue returns the previous value of the attribute				***/
/*** Usage: previousValue([device:attribute])								***/
/******************************************************************************/
private Map func_previousvalue(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting previousValue([device:attribute])"]
	}
	def param = evaluateExpression(rtData, params[0], 'device')
	if(((String)param.t == 'device') && ((String)param.a) && param.v.size()) {
		//def attribute = rtData.attributes[param.a]
		def attribute = Attributes()[(String)param.a]
		if(attribute) {
			def device = getDevice(rtData, param.v[0])
			if(device && !isDeviceLocation(device)) {
				def states = device.statesSince((String)param.a, new Date(now() - 604500000), [max: 5])
				if(states.size() > 1) {
					def newValue = states[0].getValue()
					//some events get duplicated, so we really want to look for the last "different valued" state
					for(int i = 1; i < states.size(); i++) {
						def result = states[i].getValue()
						if(result != newValue) {
							return [t: (String)attribute.t, v: cast(rtData, result, (String)attribute.t)]
						}
					}
				}
				//we're saying 7 days, though it may be wrong - but we have no data
				return [t: 'string', v: '']
			}
		}
	}
	return [t: "error", v: "Invalid device"]
}

/******************************************************************************/
/*** newer returns the number of devices whose attribute had the current  ***/
/*** value for less than the specified number of milliseconds		***/
/*** Usage: newer([device:attribute] [,.., [device:attribute]], threshold)***/
/******************************************************************************/
private Map func_newer(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting newer([device:attribute] [,.., [device:attribute]], threshold)"]
	}
	int threshold = (int)evaluateExpression(rtData, params[params.size() - 1], 'integer').v
	int result = 0
	for (int i = 0; i < params.size() - 1; i++) {
		def age = func_age(rtData, [params[i]])
		if((age.t != 'error') && (age.v < threshold)) result++
	}
	return [t: "integer", v: result]
}

/******************************************************************************/
/*** older returns the number of devices whose attribute had the current  ***/
/*** value for more than the specified number of milliseconds		***/
/*** Usage: older([device:attribute] [,.., [device:attribute]], threshold)	***/
/******************************************************************************/
private Map func_older(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting older([device:attribute] [,.., [device:attribute]], threshold)"]
	}
	int threshold = (int)evaluateExpression(rtData, params[params.size() - 1], 'integer').v
	int result = 0
	for (int i = 0; i < params.size() - 1; i++) {
		def age = func_age(rtData, [params[i]])
		if(((String)age.t != 'error') && (age.v >= threshold)) result++
	}
	return [t: "integer", v: result]
}

/******************************************************************************/
/*** startsWith returns true if a string starts with a substring			***/
/*** Usage: startsWith(string, substring)									***/
/******************************************************************************/
private Map func_startswith(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting startsWith(string, substring)"]
	}
	String string = (String)evaluateExpression(rtData, params[0], 'string').v
	String substring = (String)evaluateExpression(rtData, params[1], 'string').v
	return [t: "boolean", v: string.startsWith(substring)]
}

/******************************************************************************/
/*** endsWith returns true if a string ends with a substring				***/
/*** Usage: endsWith(string, substring)										***/
/******************************************************************************/
private Map func_endswith(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting endsWith(string, substring)"]
	}
	String string = (String)evaluateExpression(rtData, params[0], 'string').v
	String substring = (String)evaluateExpression(rtData, params[1], 'string').v
	return [t: "boolean", v: string.endsWith(substring)]
}

/******************************************************************************/
/*** contains returns true if a string contains a substring					***/
/*** Usage: contains(string, substring)										***/
/******************************************************************************/
private Map func_contains(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2) || ((params[0].t != 'device') && (params.size() != 2))) {
		return [t: "error", v: "Invalid parameters. Expecting contains(string, substring)"]
	}
	if((params[0].t == 'device') && (params.size() > 2)) {
		String item = evaluateExpression(rtData, params[params.size() - 1], 'string').v
		for (int idx = 0; idx < params.size() - 1; idx++) {
			def it = evaluateExpression(rtData, params[idx], 'string')
			if(it.v == item) {
				return [t: "boolean", v: true]
			}
		}
		return [t: "boolean", v: false]
	} else {
		String string = (String)evaluateExpression(rtData, params[0], 'string').v
		String substring = (String)evaluateExpression(rtData, params[1], 'string').v
		return [t: "boolean", v: string.contains(substring)]
	}
}


/******************************************************************************/
/*** matches returns true if a string matches a pattern						***/
/*** Usage: matches(string, pattern)										***/
/******************************************************************************/
private Map func_matches(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting matches(string, pattern)"]
	}
	String string = (String)evaluateExpression(rtData, params[0], 'string').v
	String pattern = (String)evaluateExpression(rtData, params[1], 'string').v
	if((pattern.size() > 2) && pattern.startsWith('/') && pattern.endsWith('/')) {
		pattern = ~pattern.substring(1, pattern.size() - 1)
		return [t: "boolean", v: !!(string =~ pattern)]
	}
	return [t: "boolean", v: string.contains(pattern)]
}

/******************************************************************************/
/*** eq returns true if two values are equal								***/
/*** Usage: eq(value1, value2)							***/
/******************************************************************************/
private Map func_eq(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting eq(value1, value2)"]
	}
	String t = params[0].t == 'device' ? (String)params[1].t : (String)params[0].t
	def value1 = evaluateExpression(rtData, params[0], t)
	def value2 = evaluateExpression(rtData, params[1], t)
	return [t: "boolean", v: value1.v == value2.v]
}

/******************************************************************************/
/*** lt returns true if value1 < value2										***/
/*** Usage: lt(value1, value2)							***/
/******************************************************************************/
private Map func_lt(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting lt(value1, value2)"]
	}
	def value1 = evaluateExpression(rtData, params[0])
	def value2 = evaluateExpression(rtData, params[1], (String)value1.t)
	return [t: "boolean", v: value1.v < value2.v]
}

/******************************************************************************/
/*** le returns true if value1 <= value2									***/
/*** Usage: le(value1, value2)							***/
/******************************************************************************/
private Map func_le(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting le(value1, value2)"]
	}
	def value1 = evaluateExpression(rtData, params[0])
	def value2 = evaluateExpression(rtData, params[1], (String)value1.t)
	return [t: "boolean", v: value1.v <= value2.v]
}

/******************************************************************************/
/*** gt returns true if value1 > value2									***/
/*** Usage: gt(value1, value2)							***/
/******************************************************************************/
private Map func_gt(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting gt(value1, value2)"]
	}
	def value1 = evaluateExpression(rtData, params[0])
	def value2 = evaluateExpression(rtData, params[1], (String)value1.t)
	return [t: "boolean", v: value1.v > value2.v]
}

/******************************************************************************/
/*** ge returns true if value1 >= value2									***/
/*** Usage: ge(value1, value2)							***/
/******************************************************************************/
private Map func_ge(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 2)) {
		return [t: "error", v: "Invalid parameters. Expecting ge(value1, value2)"]
	}
	Map value1 = evaluateExpression(rtData, params[0])
	Map value2 = evaluateExpression(rtData, params[1], (String)value1.t)
	return [t: "boolean", v: value1.v >= value2.v]
}

/******************************************************************************/
/*** not returns the negative boolean value								***/
/*** Usage: not(value)								***/
/******************************************************************************/
private Map func_not(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting not(value)"]
	}
	boolean value = (boolean)evaluateExpression(rtData, params[0], 'boolean').v
	return [t: "boolean", v: !value]
}

/******************************************************************************/
/*** if evaluates a boolean and returns value1 if true, or value2 otherwise ***/
/*** Usage: if(condition, valueIfTrue, valueIfFalse)						***/
/******************************************************************************/
private Map func_if(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 3)) {
		return [t: "error", v: "Invalid parameters. Expecting if(condition, valueIfTrue, valueIfFalse)"]
	}
	boolean value = (boolean)evaluateExpression(rtData, params[0], 'boolean').v
	return value ? evaluateExpression(rtData, params[1]) : evaluateExpression(rtData, params[2])
}

/******************************************************************************/
/*** isEmpty returns true if the value is empty								***/
/*** Usage: isEmpty(value)							***/
/******************************************************************************/
private Map func_isempty(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting isEmpty(value)"]
	}
	def value = evaluateExpression(rtData, params[0])
	boolean result = (value.v instanceof List ? (value.v == [null]) || (value.v == []) || (value.v == ['null']) : false) || (value.v == null) || ((String)value.t == 'error') || (value.v == 'null') || ((String)cast(rtData, value.v, 'string') == '') || ("$value.v" == '')
	return [t: "boolean", v: result]
}

/******************************************************************************/
/*** datetime returns the value as a datetime type							***/
/*** Usage: datetime([value])							***/
/******************************************************************************/
private Map func_datetime(Map rtData, List params) {
	if(!(params instanceof List) || (params.size() > 1)) {
		return [t: "error", v: "Invalid parameters. Expecting datetime([value])"]
	}
	long value = params.size() > 0 ? (long)evaluateExpression(rtData, params[0], 'datetime').v : now()
	return [t: "datetime", v: value]
}

/******************************************************************************/
/*** date returns the value as a date type									***/
/*** Usage: date([value])							***/
/******************************************************************************/
private Map func_date(Map rtData, List params) {
	if(!(params instanceof List) || (params.size() > 1)) {
		return [t: "error", v: "Invalid parameters. Expecting date([value])"]
	}
	long value = params.size() > 0 ? (long)evaluateExpression(rtData, params[0], 'date').v : (long)cast(rtData, now(), 'date', 'datetime')
	return [t: "date", v: value]
}

/******************************************************************************/
/*** time returns the value as a time type									***/
/*** Usage: time([value])							***/
/******************************************************************************/
private Map func_time(Map rtData, List params) {
	if(!(params instanceof List) || (params.size() > 1)) {
		return [t: "error", v: "Invalid parameters. Expecting time([value])"]
	}
	long value = params.size() > 0 ? (long)evaluateExpression(rtData, params[0], 'time').v : (long)cast(rtData, now(), 'time', 'datetime')
	return [t: "time", v: value]
}

/******************************************************************************/
/*** addSeconds returns the value as a time type							***/
/*** Usage: addSeconds([dateTime, ]seconds)									***/
/******************************************************************************/
private Map func_addseconds(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting addSeconds([dateTime, ]seconds)"]
	}
	long value = params.size() == 2 ? evaluateExpression(rtData, params[0], 'datetime').v : now()
	long delta = (long)evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 1000
	return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addMinutes returns the value as a time type							***/
/*** Usage: addMinutes([dateTime, ]minutes)									***/
/******************************************************************************/
private Map func_addminutes(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting addMinutes([dateTime, ]minutes)"]
	}
	long value = params.size() == 2 ? (long)evaluateExpression(rtData, params[0], 'datetime').v : now()
	long delta = (long)evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 60000
	return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addHours returns the value as a time type								***/
/*** Usage: addHours([dateTime, ]hours)										***/
/******************************************************************************/
private Map func_addhours(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting addHours([dateTime, ]hours)"]
	}
	long value = params.size() == 2 ? (long)evaluateExpression(rtData, params[0], 'datetime').v : now()
	long delta = (long)evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 3600000
	return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addDays returns the value as a time type								***/
/*** Usage: addDays([dateTime, ]days)										***/
/******************************************************************************/
private Map func_adddays(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting addDays([dateTime, ]days)"]
	}
	long value = params.size() == 2 ? (long)evaluateExpression(rtData, params[0], 'datetime').v : now()
	long delta = (long)evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 86400000
	return [t: "datetime", v: value + delta]
}

/******************************************************************************/
/*** addWeeks returns the value as a time type								***/
/*** Usage: addWeeks([dateTime, ]weeks)										***/
/******************************************************************************/
private Map func_addweeks(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting addWeeks([dateTime, ]weeks)"]
	}
	long value = params.size() == 2 ? (long)evaluateExpression(rtData, params[0], 'datetime').v : now()
	long delta = (long)evaluateExpression(rtData, (params.size() == 2 ? params[1] : params[0]), 'long').v * 604800000
	return [t: "datetime", v: value + delta]
}


/******************************************************************************/
/*** weekDayName returns the name of the week day							***/
/*** Usage: weekDayName(dateTimeOrWeekDayIndex)								***/
/******************************************************************************/
private Map func_weekdayname(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting weekDayName(dateTimeOrWeekDayIndex)"]
	}
	long value = (long)evaluateExpression(rtData, params[0], 'long').v
	int index = ((value >= 86400000) ? (int)utcToLocalDate(value).day : value) % 7
	return [t: "string", v: weekDays()[index]]
}

/******************************************************************************/
/*** monthName returns the name of the month								***/
/*** Usage: monthName(dateTimeOrMonthNumber)								***/
/******************************************************************************/
private Map func_monthname(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting monthName(dateTimeOrMonthNumber)"]
	}
	long value = (long)evaluateExpression(rtData, params[0], 'long').v
	int index = ((value >= 86400000) ? utcToLocalDate(value).month : value - 1) % 12 + 1
	return [t: "string", v: yearMonths()[index]]
}

/******************************************************************************/
/*** arrayItem returns the nth item in the parameter list					***/
/*** Usage: arrayItem(index, item0[, item1[, .., itemN]])					***/
/******************************************************************************/
private Map func_arrayitem(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2)) {
		return [t: "error", v: "Invalid parameters. Expecting arrayItem(index, item0[, item1[, .., itemN]])"]
	}
	int index = (int)evaluateExpression(rtData, params[0], 'integer').v
	if((params.size() == 2) && ((params[1].t == 'string') || (params[1].t == 'dynamic'))) {
		def list = ((String)evaluateExpression(rtData, params[1], 'string').v).split(',').toList()
		if((index < 0) || (index >= list.size())) {
			return [t: "error", v: "Array item index is outside of bounds."]
		}
		return [t: 'string', v: list[index]]
	}
	int sz = params.size() - 1
	if((index < 0) || (index >= sz)) {
		return [t: "error", v: "Array item index is outside of bounds."]
	}
	return params[index + 1]
}


/******************************************************************************/
/*** isBetween returns true if value >= startValue and value <= endValue	***/
/*** Usage: isBetween(value, startValue, endValue)							***/
/******************************************************************************/
private Map func_isbetween(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 3)) {
		return [t: "error", v: "Invalid parameters. Expecting isBetween(value, startValue, endValue)"]
	}
	def value = evaluateExpression(rtData, params[0])
	def startValue = evaluateExpression(rtData, params[1], (String)value.t)
	def endValue = evaluateExpression(rtData, params[2], (String)value.t)
	return [t: "boolean", v: (value.v >= startValue.v) && (value.v <= endValue.v)]
}

/******************************************************************************/
/*** formatDuration returns a duration in a readable format					***/
/*** Usage: formatDuration(value[, friendly = false[, granularity = 's'[, showAdverbs = false]]])	***/
/******************************************************************************/
private Map func_formatduration(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 4)) {
		return [t: "error", v: "Invalid parameters. Expecting formatDuration(value[, friendly = false[, granularity = 's'[, showAdverbs = false]]])"]
	}
	long value = (long)evaluateExpression(rtData, params[0], 'long').v
	boolean friendly = params.size() > 1 ? (boolean)evaluateExpression(rtData, params[1], 'boolean').v : false
	String granularity = params.size() > 2 ? (String)evaluateExpression(rtData, params[2], 'string').v : 's'
	boolean showAdverbs = params.size() > 3 ? (boolean)evaluateExpression(rtData, params[3], 'boolean').v : false

	int sign = (value >= 0) ? 1 : -1
	if(sign < 0) value = -value
	int ms = value % 1000
	value = Math.floor((value - ms) / 1000)
	int s = value % 60
	value = Math.floor((value - s) / 60)
	int m = value % 60
	value = Math.floor((value - m) / 60)
	int h = value % 24
	value = Math.floor((value - h) / 24)
	int d = value

	int parts = 0
	String partName = ''
	switch (granularity) {
		case 'd': parts = 1; partName = 'day'; break
		case 'h': parts = 2; partName = 'hour'; break
		case 'm': parts = 3; partName = 'minute'; break
		case 'ms': parts = 5; partName = 'millisecond'; break
		default: parts = 4; partName = 'second'; break
	}
	parts = friendly ? parts : (parts < 3 ? 3 : parts)
	String result = ''
	if(friendly) {
		List p = []
		if(d) p.push("$d day" + (d > 1 ? 's' : ''))
		if((parts > 1) && h) p.push("$h hour" + (h > 1 ? 's' : ''))
		if((parts > 2) && m) p.push("$m minute" + (m > 1 ? 's' : ''))
		if((parts > 3) && s) p.push("$s second" + (s > 1 ? 's' : ''))
		if((parts > 4) && ms) p.push("$ms millisecond" + (ms > 1 ? 's' : ''))
		switch (p.size()) {
			case 0:
				result = showAdverbs ? 'now' : '0 ' + partName + 's'
				break
			case 1:
				result = p[0]
				break
			default:
				result = ''
				int sz = p.size()
				for (int i=0; i < sz; i++) {
					result += (i ? (sz > 2 ? ', ' : ' ') : '') + (i == sz - 1 ? 'and ' : '') + p[i]
				}
				result = (showAdverbs && (sign > 0) ? 'in ' : '') + result + (showAdverbs && (sign < 0) ? ' ago' : '')
				break
		}
	} else {
		result = (sign < 0 ? '-' : '') + (d > 0 ? sprintf("%dd ", d) : '') + sprintf("%02d:%02d", h, m) + (parts > 3 ? sprintf(":%02d", s) : '') + (parts > 4 ? sprintf(".%03d", ms) : '')
	}
	return [t: "string", v: result]
}

/******************************************************************************/
/*** formatDateTime returns a datetime in a readable format					***/
/*** Usage: formatDateTime(value[, format])									***/
/******************************************************************************/
private Map func_formatdatetime(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting formatDateTime(value[, format])"]
	}
	long value = (long)evaluateExpression(rtData, params[0], 'datetime').v
	String format = params.size() > 1 ? (String)evaluateExpression(rtData, params[1], 'string').v : (String)null
	return [t: 'string', v: (format ? formatLocalTime(value, format) : formatLocalTime(value))]
}

/******************************************************************************/
/*** random returns a random value											***/
/*** Usage: random([range | value1, value2[, ..,valueN]])	***/
/******************************************************************************/
private Map func_random(Map rtData, List params) {
	int sz = params && (params instanceof List) ? params.size() : 0
	switch (sz) {
		case 0:
			return [t: 'decimal', v: Math.random()]
		case 1:
			double range = (double)evaluateExpression(rtData, params[0], 'decimal').v
			return [t: 'integer', v: (int)Math.round(range * Math.random())]
		case 2:
			if((((String)params[0].t == 'integer') || ((String)params[0].t == 'decimal')) && (((String)params[1].t == 'integer') || ((String)params[1].t == 'decimal'))) {
				double min = (double)evaluateExpression(rtData, params[0], 'decimal').v
				double max = (double)evaluateExpression(rtData, params[1], 'decimal').v
				if(min > max) {
				double swap = min
				min = max
				max = swap
			}
			return [t: 'integer', v: (int)Math.round(min + (max - min) * Math.random())]
		}
	}
	int choice = (int)Math.round((sz - 1) * Math.random())
	if(choice >= sz) choice = sz - 1
	return params[choice]
}


/******************************************************************************/
/*** random returns a random value											***/
/*** Usage: distance((device | latitude, longitude), (device | latitude, longitude)[, unit])	***/
/******************************************************************************/
private Map func_distance(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 2) || (params.size() > 4)) {
		return [t: "error", v: "Invalid parameters. Expecting distance((device | latitude, longitude), (device | latitude, longitude)[, unit])"]
	}
	double lat1, lng1, lat2, lng2
	String unit
	int idx = 0
	int pidx = 0
	String errMsg = ''
	while (pidx < params.size()) {
		if((params[pidx].t != 'device') || ((params[pidx].t == 'device') && !!params[pidx].a)) {
			//a decimal or device attribute is provided
			switch (idx) {
			case 0:
				lat1 = (double)evaluateExpression(rtData, params[pidx], 'decimal').v
				break
			case 1:
				lng1 = (double)evaluateExpression(rtData, params[pidx], 'decimal').v
				break
			case 2:
				lat2 = (double)evaluateExpression(rtData, params[pidx], 'decimal').v
				break
			case 3:
				lng2 = (double)evaluateExpression(rtData, params[pidx], 'decimal').v
				break
			case 4:
				unit = (String)evaluateExpression(rtData, params[pidx], 'string').v
			}
			idx += 1
			pidx += 1
			continue
		} else {
			switch (idx) {
			case 0:
			case 2:
				params[pidx].a = 'latitude'
				double lat = (double)evaluateExpression(rtData, params[pidx], 'decimal').v
				params[pidx].a = 'longitude'
				double lng = (double)evaluateExpression(rtData, params[pidx], 'decimal').v
				if(idx == 0) {
					lat1 = lat
					lng1 = lng
				} else {
					lat2 = lat
					lng2 = lng
				}
				idx += 2
				pidx += 1
				continue
			default:
				errMsg = 'Invalid parameter order. Expecting parameter #${idx+1} to be a decimal, not a device.'
				pidx = -1
				break
			}
		}
		if(pidx == -1) break
	}
	if(errMsg) return [t: 'error', v: errMsg]
	if((idx < 4) || (idx > 5)) return [t: 'error', v: 'Invalid parameter combination. Expecting either two devices, a device and two decimals, or four decimals, followed by an optional unit.']
	double earthRadius = 6371000; //meters
	double dLat = Math.toRadians(lat2-lat1)
	double dLng = Math.toRadians(lng2-lng1)
	double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		Math.sin(dLng/2) * Math.sin(dLng/2)
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
	float dist = (float) (earthRadius * c)
	switch (unit ?: 'm') {
		case 'km':
		case 'kilometer':
		case 'kilometers':
			return [t: 'decimal', v: dist / 1000.0]
		case 'mi':
		case 'mile':
		case 'miles':
			return [t: 'decimal', v: dist / 1609.3440]
		case 'ft':
		case 'foot':
		case 'feet':
			return [t: 'decimal', v: dist / 0.3048]
		case 'yd':
		case 'yard':
		case 'yards':
			return [t: 'decimal', v: dist / 0.9144]
	}
	return [t: 'decimal', v: dist]
}

/******************************************************************************/
/*** json encodes data as a JSON string					***/
/*** Usage: json(value[, pretty])									***/
/******************************************************************************/
private Map func_json(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() < 1) || (params.size() > 2)) {
		return [t: "error", v: "Invalid parameters. Expecting json(value[, format])"]
	}
	def builder = new groovy.json.JsonBuilder([params[0].v])
	String op = params[1] ? 'toPrettyString' : 'toString'
	String json = builder."${op}"()
	return [t: 'string', v: json[1..-2].trim()]
}

/******************************************************************************/
/*** percent encodes data for use in a URL					***/
/*** Usage: urlencode(value)									***/
/******************************************************************************/
private Map func_urlencode(Map rtData, List params) {
	if(!params || !(params instanceof List) || (params.size() != 1)) {
		return [t: "error", v: "Invalid parameters. Expecting urlencode(value])"]
	}
	// URLEncoder converts spaces to + which is then indistinguishable from any
	// actual + characters in the value. Match encodeURIComponent in ECMAScript
	// which encodes "a+b c" as "a+b%20c" rather than URLEncoder's "a+b+c"
	String t0 = (String)evaluateExpression(rtData, params[0], 'string').v
	String value = (t0 ?: '').replaceAll('\\+', '__wc_plus__')
	return [t: 'string', v: URLEncoder.encode(value, 'UTF-8').replaceAll('\\+', '%20').replaceAll('__wc_plus__', '+')]
}
private Map func_encodeuricomponent(Map rtData, List params) { return func_urlencode(rtData, params); }

/******************************************************************************/
/***										***/
/*** COMMON PUBLISHED METHODS							***/
/***										***/
/******************************************************************************/

private mem(showBytes = true) {
	def mbytes = new groovy.json.JsonOutput().toJson(state)
	int bytes = mbytes.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}
/******************************************************************************/
/***										***/
/*** UTILITIES									***/
/***										***/
/******************************************************************************/

private String md5(String md5) {
//log.debug "doing md5 $md5"
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
	return (String)null
}

@Field static Map theHashMapFLD

private String hashId(id, updateCache=true) {
	//enabled hash caching for faster processing
	String result
	String myId = id.toString()
	if(theHashMapFLD) {
		result = (String)theHashMapFLD."${myId}"
	} else theHashMapFLD = [:]
	if(!result) {
		result = ":${md5("core." + myId)}:"
		if(updateCache) {
			theHashMapFLD."${myId}" = result
		}
		if(eric()) log.warn "doing md5  $updateCache"
	}
	return result
}

private getThreeAxisOrientation(value, boolean getIndex = false) {
	if(value instanceof Map) {
		if((value.x != null) && (value.y != null) && (value.z != null)) {
			int x = Math.abs(value.x)
			int y = Math.abs(value.y)
			int z = Math.abs(value.z)
			int side = (x > y ? (x > z ? 0 : 2) : (y > z ? 1 : 2))
			side = side + (((side == 0) && (value.x < 0)) || ((side == 1) && (value.y < 0)) || ((side == 2) && (value.z < 0)) ? 3 : 0)
			def orientations = ['rear', 'down', 'left', 'front', 'up', 'right']
			def result = getIndex ? side : orientations[side] + ' side up'
			return result
		}
	}
	return value
}

private long getTimeToday(long time) {
	long t0 = getMidnightTime()
	long result = time + t0
	//we need to adjust for time overlapping during DST changes
	return result + ((int)location.timeZone.getOffset(t0) - (int)location.timeZone.getOffset(result))
}

private cast(Map rtData, value, String dataType, String srcDataType = (String)null) {
//ERS
	//error "CASTING ($srcDataType) $value as $dataType", rtData
	//if(srcDataType == 'vector3') error "got x = $value.x", rtData
	if(dataType == 'dynamic') return value
	List trueStrings = ["1", "true", "on", "open", "locked", "active", "wet", "detected", "present", "occupied", "muted", "sleeping"]
	List falseStrings = ["0", "false", "off", "closed", "unlocked", "inactive", "dry", "clear", "not detected", "not present", "not occupied", "unmuted", "not sleeping", "null"]
	//get rid of GStrings
	if(value == null) {
		value = ''
		srcDataType = 'string'
	}
	value = (value instanceof GString) ? "$value".toString() : value
	if(!srcDataType || (srcDataType == 'boolean') || (srcDataType == 'dynamic')) {
		if(value instanceof List) { srcDataType = 'device' } else
		if(value instanceof Boolean) { srcDataType = 'boolean' } else
		//if(value instanceof String) { srcDataType = 'string' } else
		if(value instanceof String) { srcDataType = 'string' } else
		if(value instanceof Integer) { srcDataType = 'integer' } else
		if(value instanceof BigInteger) { srcDataType = 'long' } else
		if(value instanceof Long) { srcDataType = 'long' } else
		if(value instanceof Double) { srcDataType = 'decimal' } else
		if(value instanceof Float) { srcDataType = 'decimal' } else
		if(value instanceof BigDecimal) { srcDataType = 'decimal' } else
		if((value instanceof Map) && (value.x != null) && (value.y != null) && (value.z != null)) { srcDataType = 'vector3' } else {
			value = "$value".toString()
			srcDataType = 'string'
		}
	}
	//overrides
	switch (srcDataType) {
		case 'bool': srcDataType = 'boolean'; break
		case 'number': srcDataType = 'decimal'; break
		case 'enum': srcDataType = 'string'; break
	}
	switch (dataType) {
		case 'bool': dataType = 'boolean'; break
		case 'number': dataType = 'decimal'; break
		case 'enum': dataType = 'string'; break
	}
	if((boolean)rtData.eric) log "cast $srcDataType $value as $dataType", null, null, null, "warn", true
	switch (dataType) {
		case "string":
		case "text":
			switch (srcDataType) {
				case 'boolean': return value ? "true" : "false"
				case 'decimal':
					//if(value instanceof Double) return sprintf('%f', value)
					// strip trailing zeroes (e.g. 5.00 to 5 and 5.030 to 5.03)
					return value.toString().replaceFirst(/(?:\.|(\.\d*?))0+$/, '$1')
				case 'integer':
				case 'long': break; // if(value > 9999999999) { return formatLocalTime(value) }; break
				case 'time': return formatLocalTime(value, 'h:mm:ss a z')
				case 'date': return formatLocalTime(value, 'EEE, MMM d yyyy')
				case 'datetime': return formatLocalTime(value)
				case 'device': return buildDeviceList(rtData, value)
			}
			return "$value".toString()
		case "integer":
			switch (srcDataType) {
				case 'string':
					value = value.replaceAll(/[^-\d.-E]/, '')
					if(value.isInteger())
						return (int)value.toInteger()
					if(value.isFloat())
						return (int)Math.floor(value.toDouble())
					if(value in trueStrings)
						return (int)1
					break
				case 'boolean': return (int)(value ? 1 : 0)
			}
			int result = (int)0
			try {
				result = (int)value
			} catch(all) {
				result = (int)0
			}
			return result ? result : (int)0
		case "long":
			switch (srcDataType) {
				case 'string':
					value = value.replaceAll(/[^-\d.-E]/, '')
					if(value.isLong())
						return (long)value.toLong()
					if(value.isInteger())
						return (long)value.toInteger()
					if(value.isFloat())
						return (long)Math.floor(value.toDouble())
					if(value in trueStrings)
						return (long)1
					break
				case 'boolean': return (long)(value ? 1 : 0)
			}
			long result = (long)0
			try {
				result = (long)value
			} catch(all) {
				result = (long)0
			}
			return result ? result : (long)0
		case "decimal":
			switch (srcDataType) {
				case 'string':
					value = value.replaceAll(/[^-\d.-E]/, '')
					if(value.isDouble())
						return (double)value.toDouble()
					if(value.isFloat())
						return (double)value.toDouble()
					if(value.isLong())
						return (double)value.toLong()
					if(value.isInteger())
						return (double)value.toInteger()
					if(value in trueStrings)
						return (double)1
					break
				case 'boolean': return (double)(value ? 1 : 0)
			}
			double result = (double)0
			try {
				result = (double)value
			} catch(all) {
			}
			return result ? result : (double)0
		case "boolean":
			switch (srcDataType) {
				case 'integer':
				case 'decimal':
				case 'boolean':
					return !!value
				case 'device':
					return (value instanceof List) && value.size()
			}
			if(value) {
				if("$value".toLowerCase().trim() in trueStrings) return true
				if("$value".toLowerCase().trim() in falseStrings) return false
			}
			return !!value
		case "time":
			if("$value".isNumber() && (value < 86400000)) return value
			long d = (srcDataType == 'string') ? stringToTime(value) : (long)value // (long)cast(rtData, value, "long")
			def t1 = new Date(d)
			long t2 = ((int)t1.hours * 3600 + (int)t1.minutes * 60 + (int)t1.seconds) * 1000
			return t2
		case "date":
			if((srcDataType == 'time') && (value < 86400000)) value = getTimeToday(value)
			long d = (srcDataType == 'string') ? stringToTime(value) : (long)value // (long)cast(rtData, value, "long")
			def t1 = new Date(d)
			long t2 = ((d/1000) * 1000) - (((int)t1.hours * 3600 + (int)t1.minutes * 60 + (int)t1.seconds) * 1000) // take ms off and first guess at midnight (could be earlier/later depending if DST change day
			long t3 = ( t2 - (1 * 3600000) ) // guess at 11 PM
			long t4 = ( t2 + (4 * 3600000) ) // guess at 04 AM
			long t5 = ( t2 + (3 * 3600000) + ((int)location.timeZone.getOffset(t3) - (int)location.timeZone.getOffset(t4)) ) // normalize to 3:00 AM for DST
			return t5
		case "datetime":
			if((srcDataType == 'time') && (value < 86400000)) return getTimeToday(value)
			return (srcDataType == 'string') ? stringToTime(value) : (long)value // (long)cast(rtData, value, "long")
		case "vector3":
			return (value instanceof Map) && (value.x != null) && (value.y != null) && (value.z != null) ? value : [x:0, y:0, z:0]
		case "orientation":
			return getThreeAxisOrientation(value)
		case 'ms':
		case 's':
		case 'm':
		case 'h':
		case 'd':
		case 'w':
		case 'n':
		case 'y':
			long t1 = 0L
			switch (srcDataType) {
				case 'integer':
				case 'long':
					t1 = value; break
				default:
					t1 = (long)cast(rtData, value, 'long')
			}
			switch (dataType) {
				case 'ms': return t1
				case 's': return t1 * 1000
				case 'm': return t1 * 60000
				case 'h': return t1 * 3600000
				case 'd': return t1 * 86400000
				case 'w': return t1 * 604800000
				case 'n': return t1 * 2592000000
				case 'y': return t1 * 31536000000
			}
		case 'device':
		//device type is an array of device Ids
			if(value instanceof List) {
				//def x = value.size()
				value.removeAll{ !it }
				return value
			}
			String v = (String)cast(rtData, value, 'string')
			if(v) return [v]
			return []
	}
	//anything else...
	return value
}

private utcToLocalDate(dateOrTimeOrString = null) { // this is really cast something to Date
	if(dateOrTimeOrString instanceof String) {
		//get time
//		try {
			dateOrTimeOrString = stringToTime(dateOrTimeOrString)
//		} catch (all) {
//			error "Error converting $dateOrTimeOrString to Date: ", null, null, all
//		}
	}
	if(dateOrTimeOrString instanceof Date) {
		//get unix time
		dateOrTimeOrString = dateOrTimeOrString.getTime()
	}
	if(!dateOrTimeOrString) {
		dateOrTimeOrString = now()
	}
	if(dateOrTimeOrString instanceof Long) {
		//HE adjusts Date fields (except for getTime() to local timezone of hub)
		return new Date(dateOrTimeOrString)
	}
	return null
}
private localDate() { return utcToLocalDate() }

private long localTime() { return now() } //utcToLocalTime() }

private long stringToTime(dateOrTimeOrString) { // this is convert something to time
//debug "stringToTime ${dateOrTimeOrString}"

	if(dateOrTimeOrString instanceof String) {
		long result

		try {
			result = (new Date()).parse(dateOrTimeOrString)
//debug "0 worked $result"
			return result
		} catch (all0) {
		}

		try {
		//get unix time
			if(!(dateOrTimeOrString =~ /(\s[A-Z]{3}((\+|\-)[0-9]{2}\:[0-9]{2}|\s[0-9]{4})?$)/)) {
				def newDate = (new Date()).parse(dateOrTimeOrString + ' ' + formatLocalTime(now(), 'Z'))
				result = newDate
//debug "1 worked $result"
				return result
			}
			result = (new Date()).parse(dateOrTimeOrString)
//debug "2 worked $result"
			return result
		} catch (all) {
		}

		try {
			def tt1 = toDateTime(dateOrTimeOrString)
			result = tt1.getTime()
			//result = (new Date(dateOrTimeOrString)).getTime()
//debug "5 worked $result"
			return result
		} catch(all3) {
		}

		try {
			def tz = location.timeZone
			if(dateOrTimeOrString =~ /\s[A-Z]{3}$/) { // this is not the timezone... strings like CET are not unique.
				try {
					tz = TimeZone.getTimeZone(dateOrTimeOrString[-3..-1])
					dateOrTimeOrString = dateOrTimeOrString.take(dateOrTimeOrString.size() - 3).trim()
				} catch (all4) {
				}
			}

			String t0 = dateOrTimeOrString?.trim() ?: ""
			boolean hasMeridian = false
			boolean hasAM
			if(t0.toLowerCase().endsWith('am')) {
				hasMeridian = true
				hasAM = true
			}
			if(t0.toLowerCase().endsWith('pm')) {
				hasMeridian = true
				hasAM = false
			}
			if(hasMeridian) t0 = t0[0..-3].trim()

			long time = timeToday(t0, tz).getTime() //DST

			if(hasMeridian) {
				def t1 = new Date( time )
				int hr = t1.hours
				int min = t1.minutes
				boolean twelve = hr == 12 ? true : false
				if(twelve && hasAM) hr -= 12
				if(!twelve && !hasAM) hr += 12
				String str1 = "${hr}"
				String str2 = "${min}"
				if(hr < 10) str1 = String.format('%02d', hr)
				if(min < 10) str2 = String.format('%02d', min)
				String str = str1 + ':' + str2
				time = timeToday(str, tz).getTime()
			}
			result = time
//debug "6 worked $result"
			return result
		} catch (all5) {
		}

//debug "returning local time"
		result = (new Date()).getTime()
		return result
	}

	if(dateOrTimeOrString instanceof Date) {
		dateOrTimeOrString = dateOrTimeOrString.getTime()
//debug "7 worked $result"
	}
	if("$dateOrTimeOrString".isNumber()) {
		if(dateOrTimeOrString < 86400000) dateOrTimeOrString = getTimeToday(dateOrTimeOrString)
//debug "8 worked $result"
		return dateOrTimeOrString
	}
//debug "erroring out"
	return 0L
}

private String formatLocalTime(time, String format = "EEE, MMM d yyyy @ h:mm:ss a z") {
	if("$time".isNumber()) {
		if(time < 86400000) time = getTimeToday(time)
//DST??
		time = new Date(time)
	}
	if(time instanceof String) {
		//get time
		time = new Date(stringToTime(time))
	}
	if(!(time instanceof Date)) {
		return (String)null
	}
//log.debug "formatTime ${time.getTime()} ${format}"
	def formatter = new java.text.SimpleDateFormat(format)
	formatter.setTimeZone(location.timeZone)
//log.debug "formatTime ${time.getTime()} ${formatter.format(time)}"
	return formatter.format(time)
}

private Map hexToColor(String hex) {
	hex = hex ?: '000000'
	if(hex.startsWith('#')) hex = hex.substring(1)
	if(hex.size() != 6) hex = '000000'
	List myHsl = hexToHsl(hex)
	return [
		hue: Math.round(myHsl[0]),
		saturation: myHsl[1],
		level: myHsl[2],
		hex: '#' + hex
	]
}

private float _hue2rgb(p, q, t) {
	if(t < 0) t += 1
	if(t >= 1) t -= 1
	if(t < 1/6) return p + (q - p) * 6 * t
	if(t < 1/2) return q
	if(t < 2/3) return p + (q - p) * (2/3 - t) * 6
	return p
}

private String hslToHex(hue, saturation, level) {
	float h = hue / 360.0
	float s = saturation / 100.0
	float l = level / 100.0
// argument checking for user calls
	if(h < 0) h = 0
	if(h > 1) h = 1
	if(s < 0) s = 0
	if(s > 1) s = 1
	if(l < 0) l = 0
	if(l > 1) l = 1

	float r, g, b
	if(s == 0) {
		r = g = b = l; // achromatic
	} else {
		float q = l < 0.5 ? l * (1 + s) : l + s - (l * s)
		float p = 2 * l - q
		r = _hue2rgb(p, q, h + 1/3)
		g = _hue2rgb(p, q, h)
		b = _hue2rgb(p, q, h - 1/3)
	}

	return sprintf('#%02X%02X%02X', Math.round(r * 255), Math.round(g * 255), Math.round(b * 255))
}

private Map hexToRgb(String hex) {
	hex = hex ?: '000000'
	if(hex.startsWith('#')) hex = hex.substring(1)
	if(hex.size() != 6) hex = '000000'
	int r1 = Integer.parseInt(hex.substring(0, 2), 16)
	int g1 = Integer.parseInt(hex.substring(2, 4), 16)
	int b1 = Integer.parseInt(hex.substring(4, 6), 16)
	return [r: r1, g: g1, b: b1]
}

private List hexToHsl(String hex) {
	hex = hex ?: '000000'
	if(hex.startsWith('#')) hex = hex.substring(1)
	if(hex.size() != 6) hex = '000000'
	float r = Integer.parseInt(hex.substring(0, 2), 16) / 255.0
	float g = Integer.parseInt(hex.substring(2, 4), 16) / 255.0
	float b = Integer.parseInt(hex.substring(4, 6), 16) / 255.0

	float max = Math.max(Math.max(r, g), b)
	float min = Math.min(Math.min(r, g), b)
	float h, s, l = (max + min) / 2

	if(max == min) {
		h = s = 0 // achromatic
	} else {
		float d = max - min
		s = l > 0.5 ? d / (2 - max - min) : d / (max + min)
		switch(max) {
			case r: h = (g - b) / d + (g < b ? 6 : 0); break
			case g: h = (b - r) / d + 2; break
			case b: h = (r - g) / d + 4; break
		}
		h /= 6
	}
	return [Math.round(h * 360), Math.round(s * 100), Math.round(l * 100)]
}

//hubitat device ids can be the same as the location id
private isDeviceLocation(device) {
	return device?.id.toString() == location.id.toString() && (/*isHubitat() ? */ ((device?.hubs?.size() ?: 0) > 0) /*: true*/ )
}

/******************************************************************************/
/*** DEBUG FUNCTIONS									***/
/******************************************************************************/
private log(message, Map rtData = null, shift = null, err = null, String cmd = null, boolean force = false) {
	if(cmd == "timer") {
		return [m: message, t: now(), s: shift, e: err]
	}
	if(message instanceof Map) {
		shift = message.s
		err = message.e
		message = (String)message.m + " (${now() - (long)message.t}ms)"
	}
	String myMsg = (String)message
	//if(!force && rtData && rtData.logging && !rtData.logging[cmd] && (cmd != "error")) {
	//	return
	//}
	cmd = cmd ? cmd : "debug"
	//mode is
	// 0 - initialize level, level set to 1
	// 1 - start of routine, level up
	// -1 - end of routine, level down
	// anything else - nothing happens
	int maxLevel = 4
	int level = state.debugLevel ? state.debugLevel : 0
	int levelDelta = 0
	String prefix = "║"
	String prefix2 = "║"
	String pad = "" //"░"
	switch (shift) {
		case 0:
			level = 0
		case 1:
			level += 1
			prefix = "╚"
			prefix2 = "╔"
			pad = "═"
			break
		case -1:
			level -= 1
			//levelDelta = -(level > 0 ? 1 : 0)
			pad = "═"
			prefix = "╔"
			prefix2 = "╚"
			break
	}

	if(level > 0) {
		prefix = prefix.padLeft(level + (shift == -1 ? 1 : 0), "║")
		prefix2 = prefix2.padLeft(level + (shift == -1 ? 1 : 0), "║")
	}

	//level += levelDelta
	state.debugLevel = level

	if(rtData && (rtData instanceof Map) && (rtData.logs instanceof List)) {
		myMsg = myMsg.replaceAll(/(\r\n|\r|\n|\\r\\n|\\r|\\n)+/, "\r")
		if(myMsg.size() > 1024) {
			myMsg = myMsg[0..1023] + '...[TRUNCATED]'
		}
		List msgs = !err ? myMsg.tokenize("\r") : [myMsg]
		for(msg in msgs) {
			rtData.logs.push([o: now() - (long)rtData.timestamp, p: prefix2, m: msg + (!!err ? " $err" : ""), c: cmd])
		}
	}
		if(err) {
			log."$cmd" "$prefix $myMsg $err"
		}
		else {
			log."$cmd" "$prefix $myMsg"
		}
}

private void info(message, Map rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'info' }
private void trace(message, Map rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'trace' }
private void debug(message, Map rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'debug' }
private void warn(message, Map rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'warn' }
private void error(message, Map rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'error' }
private Map timer(message, Map rtData = null, shift = null, err = null) { log message, rtData, shift, err, 'timer' }

private void tracePoint(Map rtData, String objectId, long duration, value) {
	if(objectId && rtData && rtData.trace) {
		rtData.trace.points[objectId] = [o: now() - (long)rtData.trace.t - duration, d: duration, v: value]
	} else {
		error "Invalid object ID $objectID for trace point...", rtData
	}
}


private static Map weekDays() {
	return [
		0: "Sunday",
		1: "Monday",
		2: "Tuesday",
		3: "Wednesday",
		4: "Thursday",
		5: "Friday",
		6: "Saturday"
	]
}

private static Map yearMonths() {
	return [
		1: "January",
		2: "February",
		3: "March",
		4: "April",
		5: "May",
		6: "June",
		7: "July",
		8: "August",
		9: "September",
		10: "October",
		11: "November",
		12: "December"
	]
}

@Field static Map svSunTFLD

private void initSunriseAndSunset(Map rtData) {
	def t0 = svSunTFLD
	long t = now()
	if(t0) {
		if(t < t0.nextM) {
			rtData.sunTimes = [:] + t0
		} else { t0 = null; svSunTFLD = null }
	}
	if(!t0) {
		def sunTimes = app.getSunriseAndSunset()
		if(!sunTimes.sunrise) {
			warn "Actual sunrise and sunset times are unavailable; please reset the location for your hub", rtData
			long t1 = getMidnightTime()
			sunTimes.sunrise = new Date(t0 + 7 * 3600000)
			sunTimes.sunset = new Date(t0 + 19 * 3600000)
			t = 0L
		}
		t0 = [
			sunrise: sunTimes.sunrise.time,
			sunset: sunTimes.sunset.time,
			updated: t,
			nextM: getNextMidnightTime()
		]
		rtData.sunTimes = t0
		if(t) svSunTFLD = t0
		if(eric()) log.warn "updating global sunrise"
	}
	rtData.sunrise = rtData.sunTimes.sunrise
	rtData.sunset = rtData.sunTimes.sunset
}

private long getSunriseTime(Map rtData) {
	initSunriseAndSunset(rtData)
	return (long)rtData.sunrise
}

private long getSunsetTime(Map rtData) {
	initSunriseAndSunset(rtData)
	return (long)rtData.sunset
}

private long getNextSunriseTime(Map rtData) {
	if(!rtData.nextsunrise) rtData.nextsunrise = getNextOccurance(rtData, "Sunrise")
	return (long)rtData.nextsunrise
}

private long getNextSunsetTime(Map rtData) {
	if(!rtData.nextsunset) rtData.nextsunset = getNextOccurance(rtData, "Sunset")
	return (long)rtData.nextsunset
}

// This is trying to ensure we don't fire sunsets or sunrises twice in same day by ensuring we fire a bit later than actual sunrise or sunset
private long getNextOccurance(Map rtData, String ttyp) {
	long t0 = (long)"get${ttyp}Time"(rtData)
	if(now() > t0) {
		def t1 = getLocationEventsSince("${ttyp.toLowerCase()}Time", new Date() -2)
		def t2
		if(t1.size()) {
			t2 = t1[0]
		}
		if(t2 && t2.value) { return stringToTime(t2.value) + 1000 }
	}
	long t4 = t0 + 86400000
	t4 = t4 + ((int)location.timeZone.getOffset(t0) - (int)location.timeZone.getOffset(t4))

	def t1 = new Date(t4)
	int curMon = (int)t1.month
	curMon = location.latitude > 0 ? curMon : ((curMon+6) % 12) // normalize for southern hemisphere

	int addr = 0
	if( (curMon > 5 && ttyp == "Sunset") || (curMon <= 5 && ttyp == "Sunrise")) addr = 1000 // minimize skew when sunrise or sunset moving earlier in day
	else {
		int t2 = Math.abs(location.latitude)
		int t3 = curMon % 6
		int t5 = (int)Math.round(t3 * 365/12 + (int)t1.date) // days into period
		addr = (t5 > 37 && t5 < (182-37) ? (int)Math.round(t2 * 2.8) : (int)Math.round(t2 * 1.9)) * 1000
	}
	return t4+addr
}

private long getMidnightTime() {
	return timeToday("00:00", location.timeZone).getTime()
}

private long getNextMidnightTime() {
	return timeTodayAfter("23:59", "00:00", location.timeZone).getTime()
}

private long getNoonTime(Map rtData=null) {
	return timeToday("12:00", location.timeZone).getTime()
}

private long getNextNoonTime(Map rtData=null) {
	return timeTodayAfter("23:59", "12:00", location.timeZone).getTime()
}

private Map getLocalVariables(Map rtData, List vars, Map atomState) {
	rtData.localVars = [:]
	def values = atomState.vars
	for (var in vars) {
		String t0 = (String)var.t
		def t1 = values[(String)var.n]
		def variable = [t: t0, v: var.v ?: (t0.endsWith(']') ? (t1 instanceof Map ? t1 : {}) : cast(rtData, t1, t0)), f: !!var.v] //f means fixed value - we won't save this to the state
		if(rtData && var.v && ((String)var.a == 's') && !t0.endsWith(']')) {
			variable.v = evaluateExpression(rtData, evaluateOperand(rtData, null, var.v), t0).v
		}
		rtData.localVars[(String)var.n] = variable
	}
	return rtData.localVars
}

def Map getSystemVariablesAndValues(Map rtData) {
	rtData = rtData ?: [:]
	Map result = [:] + getSystemVariables
	for(variable in result) {
		if(variable.value.d) variable.value.v = getSystemVariableValue(rtData, (String)variable.key)
	}
	return result
}

@Field final Map getSystemVariables = [
		'$args': [t: "dynamic", d: true],
		'$json': [t: "dynamic", d: true],
		'$places': [t: "dynamic", d: true],
		'$response': [t: "dynamic", d: true],
		'$hsmStatus': [t: "string", d: true],
		'$nfl': [t: "dynamic", d: true],
		'$weather': [t: "dynamic", d: true],
		'$incidents': [t: "dynamic", d: true],
		'$httpContentType': [t: "string", v: null],
		'$httpStatusCode': [t: "integer", v: null],
		'$httpStatusOk': [t: "boolean", v: null],
		'$currentEventAttribute': [t: "string", v: null],
		'$currentEventDescription': [t: "string", v: null],
		'$currentEventDate': [t: "datetime", v: null],
		'$currentEventDelay': [t: "integer", v: null],
		'$currentEventDevice': [t: "device", v: null],
		'$currentEventDeviceIndex': [t: "integer", v: null],
		'$currentEventDevicePhysical': [t: "boolean", v: null],
//		'$currentEventReceived': [t: "datetime", v: null],
		'$currentEventValue': [t: "dynamic", v: null],
		'$currentEventUnit': [t: "string", v: null],
//		'$currentState': [t: "string", v: null],
//		'$currentStateDuration': [t: "string", v: null],
//		'$currentStateSince': [t: "datetime", v: null],
//		'$nextScheduledTime': [t: "datetime", v: null],
		'$name': [t: "string", d: true],
		'$state': [t: "string", v: ''],
		'$device': [t: 'device', v: null],
		'$devices': [t: 'device', v: null],
		'$index': [t: "decimal", v: null],
		'$iftttStatusCode': [t: "integer", v: null],
		'$iftttStatusOk': [t: "boolean", v: null],
		'$location': [t: 'device', v: null],
		'$locationMode': [t: "string", d: true],
		'$localNow': [t: "datetime", d: true],
		'$now': [t: "datetime", d: true],
		'$hour': [t: "integer", d: true],
		'$hour24': [t: "integer", d: true],
		'$minute': [t: "integer", d: true],
		'$second': [t: "integer", d: true],
		'$meridian': [t: "string", d: true],
		'$meridianWithDots': [t: "string", d: true],
		'$day': [t: "integer", d: true],
		'$dayOfWeek': [t: "integer", d: true],
		'$dayOfWeekName': [t: "string", d: true],
		'$month': [t: "integer", d: true],
		'$monthName': [t: "string", d: true],
		'$year': [t: "integer", d: true],
		'$midnight': [t: "datetime", d: true],
		'$noon': [t: "datetime", d: true],
		'$sunrise': [t: "datetime", d: true],
		'$sunset': [t: "datetime", d: true],
		'$nextMidnight': [t: "datetime", d: true],
		'$nextNoon': [t: "datetime", d: true],
		'$nextSunrise': [t: "datetime", d: true],
		'$nextSunset': [t: "datetime", d: true],
		'$time': [t: "string", d: true],
		'$time24': [t: "string", d: true],
		'$utc': [t: "datetime", d: true],
		'$mediaId': [t: "string", d: true],
		'$mediaUrl': [t: "string", d: true],
		'$mediaType': [t: "string", d: true],
		'$mediaSize': [t: "integer", d: true],
		'$previousEventAttribute': [t: "string", v: null],
		'$previousEventDescription': [t: "string", v: null],
		'$previousEventDate': [t: "datetime", v: null],
		'$previousEventDelay': [t: "integer", v: null],
		'$previousEventDevice': [t: "device", v: null],
		'$previousEventDeviceIndex': [t: "integer", v: null],
		'$previousEventDevicePhysical': [t: "boolean", v: null],
//		'$previousEventExecutionTime': [t: "integer", v: null],
//		'$previousEventReceived': [t: "datetime", v: null],
		'$previousEventValue': [t: "dynamic", v: null],
		'$previousEventUnit': [t: "string", v: null],
//		'$previousState': [t: "string", v: null],
//		'$previousStateDuration': [t: "string", v: null],
//		'$previousStateSince': [t: "datetime", v: null],
		'$random': [t: "decimal", d: true],
		'$randomColor': [t: "string", d: true],
		'$randomColorName': [t: "string", d: true],
		'$randomLevel': [t: "integer", d: true],
		'$randomSaturation': [t: "integer", d: true],
		'$randomHue': [t: "integer", d: true],
		'$temperatureScale': [t: "string", d: true],
		'$version': [t: "string", d: true],
		'$versionH': [t: "string", d: true]
	]

private getSystemVariableValue(Map rtData, String name) {
	switch (name) {
	case '$args': return "${rtData.args}".toString()
	case '$json': return "${rtData.json}".toString()
	case '$places': return "${rtData.settings?.places}".toString()
	case '$response': return "${rtData.response}".toString()
	case '$weather': return "${rtData.weather}".toString()
	case '$nfl': return "${rtData.nfl}".toString()
	case '$incidents': return "${rtData.incidents}".toString()
	case '$mediaId': return rtData.mediaId
	case '$mediaUrl': return (String)rtData.mediaUrl
	case '$mediaType': return (String)rtData.mediaType
	case '$mediaSize': return (rtData.mediaData ? rtData.mediaData.size() : 0)
	case '$name': return (String)app.label
	case '$version': return version()
	case '$versionH': return HEversion()
	case '$now': return (long)now()
	case '$utc': return (long)now()
	case '$localNow': return (long)localTime()
	case '$hour': int h = (int)localDate().hours; return (h == 0 ? 12 : (h > 12 ? h - 12 : h))
	case '$hour24': return (int)localDate().hours
	case '$minute': return (int)localDate().minutes
	case '$second': return (int)localDate().seconds
	case '$meridian': int h = (int)localDate().hours; return ( h < 12 ? "AM" : "PM")
	case '$meridianWithDots': int h = (int)localDate().hours; return ( h < 12 ? "A.M." : "P.M.")
	case '$day': return localDate().date
	case '$dayOfWeek': return (int)localDate().day
	case '$dayOfWeekName': return (String)weekDays()[(int)localDate().day]
	case '$month': return (int)localDate().month + 1
	case '$monthName': return (String)yearMonths()[(int)localDate().month + 1]
	case '$year': return (int)localDate().year + 1900
	case '$midnight': return getMidnightTime()
	case '$noon': return getNoonTime()
	case '$sunrise': return getSunriseTime(rtData)
	case '$sunset': return getSunsetTime(rtData)
	case '$nextMidnight': return getNextMidnightTime()
	case '$nextNoon': return getNextNoonTime()
	case '$nextSunrise': return getNextSunriseTime(rtData)
	case '$nextSunset': return getNextSunsetTime(rtData)
	case '$time': def t = localDate(); int h = (int)t.hours; int m = (int)t.minutes; return (h == 0 ? 12 : (h > 12 ? h - 12 : h)) + ":" + (m < 10 ? "0$m" : "$m") + " " + (h <12 ? "A.M." : "P.M.")
	case '$time24': def t = localDate(); int h = (int)t.hours; int m = (int)t.minutes; return h + ":" + (m < 10 ? "0$m" : "$m")
	case '$random': double result = getRandomValue(rtData, "\$random") ?: (double)Math.random(); setRandomValue(rtData, "\$random", result); return result
	case '$randomColor': String result = getRandomValue(rtData, "\$randomColor") ?: (getRandomColor(rtData))?.rgb; setRandomValue(rtData, "\$randomColor", result); return result
	case '$randomColorName': String result = getRandomValue(rtData, "\$randomColorName") ?: (getRandomColor(rtData))?.name; setRandomValue(rtData, "\$randomColorName", result); return result
	case '$randomLevel': int result = getRandomValue(rtData, "\$randomLevel") ?: (int)Math.round(100 * Math.random()); setRandomValue(rtData, "\$randomLevel", result); return result
	case '$randomSaturation': int result = getRandomValue(rtData, "\$randomSaturation") ?: (int)Math.round(50 + 50 * Math.random()); setRandomValue(rtData, "\$randomSaturation", result); return result
	case '$randomHue': int result = getRandomValue(rtData, "\$randomHue") ?: (int)Math.round(360 * Math.random()); setRandomValue(rtData, "\$randomHue", result); return result
	case '$locationMode': return location.getMode()
	case '$temperatureScale': return location.getTemperatureScale()
	case '$hsmStatus': return location.hsmStatus
	}
}

private void setSystemVariableValue(Map rtData, String name, value) {
	if(!name || !(name.startsWith('$'))) return
	def var = rtData.systemVars[name]
	if(!var || var.d) return
	rtData.systemVars[name].v = value
}

private getRandomValue(Map rtData, String name) {
	rtData.temp = rtData.temp ?: [:]
	rtData.temp.randoms = rtData.temp.randoms ?: [:]
	return rtData.temp?.randoms[name]
}

private void setRandomValue(Map rtData, String name, value) {
	rtData.temp = rtData.temp ?: [:]
	rtData.temp.randoms = rtData.temp.randoms ?: [:]
	rtData.temp.randoms[name] = value
}

private void resetRandomValues(Map rtData) {
	rtData.temp = rtData.temp ?: [:]
	rtData.temp.randoms = [:]
}

private Map getColorByName(Map rtData, String name) {
	Map t1 = getColors().find{ (String)it.name == name }
	Map t2
	if(t1) { t2 = [:] + t1; return t2 }
	return t1
}

private Map getRandomColor(Map rtData) {
	int random = (int)(Math.random() * getColors().size())
	Map t1 = getColors()[random]
	Map t2
	if(t1) { t2 = [:] + t1; return t2 }
	return t1
}

private static Class HubActionClass() {
		return 'hubitat.device.HubAction' as Class
}

private static Class HubProtocolClass() {
		return 'hubitat.device.Protocol' as Class
}

private boolean isHubitat() {
	return hubUID != null
}

@Field static Map theAttributesFLD

//uses i, p, t, m
private Map Attributes() {
	Map result = [:]
	if(theAttributesFLD) {
		result = theAttributesFLD
	} else {
		result = parent.getChildAttributes()
		if(eric()) log.warn "getting attributes from parent"
		theAttributesFLD = result
	}
	if(!result) {
		if(eric()) log.error "no result getAttributes"
	}
	return result
}

@Field static Map theComparisonsFLD

//uses p, t
private Map Comparisons() {
	Map result = [:]
	if(theComparisonsFLD) {
		result = theComparisonsFLD
	} else {
		result = parent.getChildComparisons()
		if(eric()) log.warn "getting comparisons from parent"
		theComparisonsFLD = result
	}
	if(!result) {
		if(eric()) log.error "no result getComparisons"
	}
	return result
}

@Field static Map theVirtCommandsFLD

//uses o (override phys command), a (aggregate commands)
private Map VirtualCommands() {
	Map result = [:]
	if(theVirtCommandsFLD) {
		result = theVirtCommandsFLD
	} else {
		result = parent.getChildVirtCommands()
		if(eric()) log.warn "getting virt commands from parent"
		theVirtCommandsFLD = result
	}
	if(!result) {
		if(eric()) log.error "no result getVirtualCommands"
	}
	return result
}

//uses c and r
// the physical command r: is replaced with command c.   If the VirtualCommand c exists and has o: true we will use that virtual command;  otherwise it will be replaced with a physical command
@Field final Map CommandsOverrides = [
		push: [c: "push",	s: null, r: "pushMomentary"],
		flash: [c: "flash",	s: null, r: "flashNative"] //flash native command conflicts with flash emulated command. Also needs "o" option on command described later
]

@Field static Map theVirtDevicesFLD

//uses ac, o
private Map VirtualDevices() {
	Map result = [:]
	if(theVirtDevicesFLD) {
		result = theVirtDevicesFLD
	} else {
		result = parent.getChildVirtDevices()
		if(eric()) log.warn "getting virt devices from parent"
		theVirtDevicesFLD = result
	}
	if(!result) {
		if(eric()) log.error "no result getVirtualDevices"
	}
	return result
}

@Field static Map thePhysCommandsFLD

//uses a, v
private Map PhysicalCommands() {
	Map result = [:]
	if(thePhysCommandsFLD) {
		result = thePhysCommandsFLD
	} else {
		result = parent.getChildCommands()
		if(eric()) log.warn "getting commands from parent"
		thePhysCommandsFLD = result
	}
	if(!result) {
		if(eric()) log.error "no result getPhysicalCommands"
	}
	return result
}

@Field static List theColorsFLD

private List getColors() {
	List result = []
	if(theColorsFLD) {
		result = theColorsFLD
	} else {
		result = parent.getColors()
		if(eric()) log.warn "getting colors from parent"
		theColorsFLD = result
	}
	if(!result) {
		if(eric()) log.error "no result getColors"
	}
	return result
}
