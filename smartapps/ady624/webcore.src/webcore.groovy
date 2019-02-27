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
 *  Version history
*/
public String version() { return "v0.3.10a.20190223" }

/******************************************************************************/
/*** webCoRE DEFINITION														***/
/******************************************************************************/
private static String handle() { return "webCoRE" }
private static String domain() { return "webcore.co" }
//if(!isHubitat()) include 'asynchttp_v1'
definition(
	name: "${handle()}",
	namespace: "ady624",
	author: "Adrian Caramaliu",
	description: "Tap here to install ${handle()} ${version()}",
	category: "Convenience",
	singleInstance: false,
	/* icons courtesy of @chauger - thank you */
	iconUrl: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE.png",
	iconX2Url: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE@2x.png",
	iconX3Url: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE@3x.png"
)


preferences {
	//UI pages
	page(name: "pageMain")
	page(name: "pageDisclaimer")
	page(name: "pageEngineBlock")
	page(name: "pageInitializeDashboard")
	page(name: "pageFinishInstall")
	page(name: "pageSelectDevices")
	page(name: "pageFuelStreams")
	page(name: "pageSettings")
	page(name: "pageChangePassword")
	page(name: "pageSavePassword")
	page(name: "pageRebuildCache")
	page(name: "pageRemove")
}


/******************************************************************************/
/*** webCoRE CONSTANTS														***/
/******************************************************************************/


/******************************************************************************/
/*** 																		***/
/*** CONFIGURATION PAGES													***/
/*** 																		***/
/******************************************************************************/

/******************************************************************************/
/*** COMMON PAGES															***/
/******************************************************************************/
def pageMain() {
	//webCoRE Dashboard initialization
	def success = initializeWebCoREEndpoint()
	if (!state.installed) {
		return dynamicPage(name: "pageMain", title: "", install: false, uninstall: false, nextPage: "pageInitializeDashboard") {
			section() {
				paragraph "Welcome to ${handle()}"
				paragraph "You will be guided through a few installation steps that should only take a minute."
			}
			if (success) {
				if (!state.oAuthRequired) {
					section('Note') {
						paragraph "If you have previously installed ${handle()} and are trying to open it, please go back to the Automations tab and access ${handle()} from the SmartApps section.\r\n\r\nIf you are trying to install another instance of ${handle()} then please continue with the steps.", required: true
					}
				}
			   	if (location.getTimeZone()) {
		 			section() {
						paragraph "It looks like you are ready to go, please tap Next"
				   	}
				} else {
 					section() {
						paragraph "Your location is not correctly setup."
			   		}
					pageSectionTimeZoneInstructions()
				}
			} else {
				section() {
					paragraph "We'll start by configuring the dashboard. You need to setup OAuth in the SmartThings IDE for the ${handle()} SmartApp."
				}
				pageSectionInstructions()
				section () {
					paragraph "Once you have finished the steps above, tap Next", required: true
				}
			}
		}
	}
	//webCoRE main page
	dynamicPage(name: "pageMain", title: "", install: true, uninstall: false) {
		if (settings.agreement == undefined) {
			pageSectionDisclaimer()
		}

		if (settings.agreement) {
			section("Engine block") {
				href "pageEngineBlock", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/app-CoRE.png", inputTitleStr("Cast iron")), description: app.version(), required: false
			}
		}

		section("Dashboard") {
			if (!state.endpoint) {
				href "pageInitializeDashboard", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/dashboard.png", inputTitleStr("Dashboard")), description: "Tap to initialize", required: false
			} else {
				//trace "*** DO NOT SHARE THIS LINK WITH ANYONE *** Dashboard URL: ${getDashboardInitUrl()}"
				href "", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/dashboard.png", inputTitleStr("Dashboard")), style: "external", url: getDashboardInitUrl(), description: "Tap to open", required: false
				href "", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/browser-reg.png", inputTitleStr("Register a browser")), style: "embedded", url: getDashboardInitUrl(true), description: "Tap to open", required: false
			}
		}

		section(title:"Settings") {
			href "pageSettings", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/settings.png", inputTitleStr("Settings")), required: false
		}

	}
}

def sectionTitleStr(title)      { return "<h3>$title</h3>" }
def inputTitleStr(title)	{ return "<u>$title</u>" }
def pageTitleStr(title)		{ return "<h1>$title</h1>" }
def paraTitleStr(title)		{ return "<b>$title</b>" }

def imgTitle(imgSrc, titleStr, color=null, imgWidth=30, imgHeight=null) {
	def imgStyle = ""
	imgStyle += imgWidth ? "width: ${imgWidth}px !important;" : ""
	imgStyle += imgHeight ? "${imgWidth ? " " : ""}height: ${imgHeight}px !important;" : ""
	if(color) { return """<div style="color: ${color}; font-weight: bold;"><img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img></div>""" }
	else { return """<img style="${imgStyle}" src="${imgSrc}"> ${titleStr}</img>""" }
}


private pageSectionDisclaimer() {
	section('Disclaimer') {
		paragraph "Please read the following information carefully", required: true
		paragraph "webCoRE is a web-enabled product, which means data travels across the internet. webCoRE is using TLS for encryption of data and NEVER provides real object IDs to any system outside the WebCoRE server. The IDs are hashed into a string of letters and numbers that cannot be 'decoded' back to their original value. These hashed IDs are stored by your browser and can be cleaned up by using the Logout action under the dashboard."
		paragraph "Access to a webCoRE App is done through the browser using a security password provided during the installation of webCoRE. The browser never stores this password and it is only used during the initial registration and authentication of your browser. A security token is generated for each browser and is used for any subsequent communication. This token expires at a preset life length, or when the password is changed, or when the tokens are manually revoked from the webCoRE SmartApp's Settings menu."
	}
	section('Server-side features') {
		paragraph "Some features require that a webcore.co server processes your data. Such features include emails (sending emails out, or triggering pistons with emails), inter-location communication for superglobal variables, fuel streams, backup bins."
		paragraph "At no time does the server receive any real IDs of SmartThings objects, the instance security password, nor the instance security token that your browser uses to communicate with the SmartApp. The server is therefore unable to access any information that only an authenticated browser can."
	}
	section('Information collected by the server') {
		paragraph "The webcore.co server(s) collect ANONYMIZED hashes of 1) your unique account identifier, 2) your locations, and 3) installed webCoRE instances. It also collects an encrypted version of your SmartApp instances' endpoints that allow the server to trigger pistons on emails (if you use that feature), proxy IFTTT requests to your pistons, or provide inter-location communication between your webCoRE instances, as well as data points provided by you when using the Fuel Stream feature. It also allows for automatic browser registration when you use another browser, by providing that browser basic information about your existing instances. You will still need to enter the password to access each of those instances, the server does not have the password, nor the security tokens."
	}
	section('Information NOT collected by the server') {
		paragraph "The webcore.co server(s) do NOT intentionally collect any real object IDs from SmartThings, any names, phone numbers, email addresses, physical location information, addresses, or any other personally identifiable information."
	}
	section('Fuel Streams') {
		paragraph "The information you provide while using the Fuel Stream feature is not encrypted and is not filtered in any way. Please avoid providing personally identifiable information in either the canister name, the fuel stream name, or the data point."
	}
	section('Local webCoRE servers') {
		paragraph "Advanced users may enable a local webcore server.   Some features may not be available if you choose to do this."
	}
	section('Agreement') {
		paragraph "Certain advanced features may not work if you do not agree to the webcore.co servers collecting the anonymized information described above."
		input "agreement", "bool", title: "Allow webcore.co to collect basic, anonymized, non-personally identifiable information", defaultValue: true
	}
}

private pageDisclaimer() {
	dynamicPage(name: "pageDisclaimer", title: "") {
		pageSectionDisclaimer()
	}
}

private pageSectionInstructions() {
	state.oAuthRequired = true
	section () {
		paragraph "Please follow these steps:", required: true
		paragraph "1. Go to your SmartThings IDE and log in", required: true
		paragraph "2. Click on 'My SmartApps' and locate the 'ady624 : ${handle()}' SmartApp in the list", required: true
		paragraph "3. Click the 'Edit Properties' button to the left of the SmartApp name (a notepad and pencil icon)", required: true
		paragraph "4. Click on 'OAuth'", required: true
		paragraph "5. Click the 'Enable OAuth in Smart App' button", required: true
		paragraph "6. Click the 'Update' button", required: true
	}
}

private pageSectionTimeZoneInstructions() {
	section () {
		paragraph "Please follow these steps to setup your location timezone:", required: true
		paragraph "1. Using your SmartThings mobile app, abort this installation and go to More section of the app (three horizontal bars)", required: true
		paragraph "2. Click on the gear icon on the top right", required: true
		paragraph "3. Click on the map to edit your location", required: true
		paragraph "4. Find your location on the map and place the pin there, adjusting the desired radius", required: true
		paragraph "5. Tap the Save button, then tap Done", required: true
		paragraph "6. Try installing ${handle()} again", required: true
	}
}

private pageInitializeDashboard() {
	//webCoRE Dashboard initialization
	def success = initializeWebCoREEndpoint()
	def hasTZ = !!location.getTimeZone()
	dynamicPage(name: "pageInitializeDashboard", title: "", nextPage: success && hasTZ ? "pageSelectDevices" : null) {
		if (!state.installed) {
			if (success) {
			   	if (hasTZ) {
					section() {
						paragraph "Great, the dashboard is ready to go."
					}
					section() {
						paragraph "Now, please choose a name for this ${handle()} instance"
							//label name: "name", title: "Name", defaultValue: "webCoRE", required: false
							label name: "name", title: "Name", state: (name ? "complete" : null), defaultValue: app.name, required: false
					}

					pageSectionDisclaimer()

					section() {
						paragraph "${state.installed ? "Tap Done to continue." : "Next, choose a security password for your dashboard. You will need to enter this password when accessing your dashboard for the first time, and possibly from time to time, depending on your settings."}", required: false
					}
				} else {
	 				section() {
						paragraph "Your location is not correctly setup."
					}
					pageSectionTimeZoneInstructions()
					section () {
						paragraph "Once you have finished the steps above, go back and try again", required: true
					}
					return
				}
			} else {
				section() {
					paragraph "Sorry, it looks like OAuth is not properly enabled."
				}
				pageSectionInstructions()
				section () {
					paragraph "Once you have finished the steps above, go back and try again", required: true
				}
				return
			}
		}
		pageSectionPIN()
	}
}

private pageEngineBlock() {
	dynamicPage(name: "pageEngineBlock", title: "") {
		section() {
			paragraph "Under construction. This will help you upgrade your engine block to get access to extra features such as email triggers, fuel streams, and more."
		}
	}
}


private pageSelectDevices() {
	state.deviceVersion = now().toString()
	dynamicPage(name: "pageSelectDevices", title: "", nextPage: state.installed ? null : "pageFinishInstall") {
		section() {
			paragraph "${state.installed ? "Select the devices you want ${handle()} to have access to." : "Great, now let's select some devices."}"
			paragraph "It is a good idea to only select the devices you plan on using with ${handle()} pistons. Pistons will only have access to the devices you selected."
		}
		if (!state.installed) {
			section (Note) {
				paragraph "Remember, you can always come back to ${handle()} and add or remove devices as needed.", required: true
			}
			section() {
				paragraph "So go ahead, select a few devices, then tap Next"
			}
		}

		section (sectionTitleStr('Select devices by type')) {
			paragraph "Most devices should fall into one of these categories"
			if(isHubitat()) input "dev:all", "capability.*", multiple: true, title: "Devices", required: false
			input "dev:actuator", "capability.actuator", multiple: true, title: "Actuators", required: false
			input "dev:sensor", "capability.sensor", multiple: true, title: "Sensors", required: false
		}

		section (sectionTitleStr('Select devices by capability')) {
			paragraph "If you cannot find a device by type, you may try looking for it by category below"
			def d
			for (capability in capabilities().findAll{ (!(it.value.d in [null, 'actuators', 'sensors'])) }.sort{ it.value.d }) {
				if (capability.value.d != d) input "dev:${capability.key}", "capability.${capability.key}", multiple: true, title: "Which ${capability.value.d}", required: false
				d = capability.value.d
			}
		}
	}
}

private pageFinishInstall() {
	initTokens()
	dynamicPage(name: "pageFinishInstall", title: "", install: true) {
		section() {
			paragraph "Excellent! You are now ready to use ${handle()}"
		}
		section("Note") {
			paragraph "After you tap Done, go to the Automation tab, select the SmartApps section, and open the '${app.label}' SmartApp to access the dashboard.", required: true
			paragraph "You can also access the dashboard on any another device by entering ${domain()} in the address bar of your browser.", required: true
		}
		section() {
			paragraph "Now tap Done and enjoy ${handle()}!"
		}
	}
}

def pageSettings() {
	//clear devices cache
	dynamicPage(name: "pageSettings", title: "", install: false, uninstall: false) {
		section("General") {
			label name: "name", title: "Name", state: (name ? "complete" : null), defaultValue: app.name, required: false
		}

/*
		def storageApp = getStorageApp()
		if (storageApp) {
			section("Storage Application") {
				app([title: isHubitat() ? 'Do not click - App Launchs automatically' : 'Available Devices', multiple: false, install: true, uninstall: false], 'storage', 'ady624', "${handle()} Storage")
			}
		} else {*/
			section("Available devices") {
				href "pageSelectDevices", title: "Available devices", description: "Tap here to select which devices are available to pistons"
			}
		//}

		section(sectionTitleStr("Fuel Streams")){
			input "localFuelStreams", "bool", title: "Use local fuel streams?", defaultValue: (settings.localFueStreams != null) ? settings.localFueStreams : isHubitat() ? true : false, submitOnChange: true
			if(settings.localFuelStreams){
				href "pageFuelStreams", title: "Fuel Streams", description: "Tap here to manage fuel streams"		
			}	 	
		}
	
/*		section("Integrations") {
			href "pageIntegrations", title: "Integrations with other services", description: "Tap here to configure your integrations"
		}*/

		section(sectionTitleStr("Security")) {
			href "pageChangePassword", title: "Security", description: "Tap here to change your dashboard security settings"
		}

		section(sectionTitleStr("Custom Endpoints - Advanced")) {
			paragraph "Custom Endpoints allows use of a local hub address and optionally a local WebCoRE server"
			input "customEndpoints", "bool", submitOnChange: true, title: "Use custom endpoints?", default: false, required: true
			if(customEndpoints){
				if(isHubitat()) input "customHubUrl", "string", title: "Custom hub (local) url different from https://cloud.hubitat.com", submitOnChange: true,default: null, required: false
				def req = false
				if(customEndPoints && customHubUrl) req = true
				input "customWebcoreInstanceUrl", "string", title: "Custom webcore server instance url different from dashboard.webcore.co", default: null, required: req
				if(customHubUrl && isHubitat()) paragraph "If you enter a custom hub url you MUST use a custom webcore server instance as dashboard.webcore.co site is restricted to hubitat and smartthing's cloud access only" 
			}
		}

		section(sectionTitleStr("Logging")) {
			input "logging", "enum", title: "Logging level", options: ["None", "Minimal", "Medium", "Full"], description: "Enable Logs in platform logs", defaultValue: "None", required: false
		}

		section(title:"Privacy") {
			href "pageDisclaimer", title: imgTitle("https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/settings.png", inputTitleStr("Data Collection")), required: false
//			href "pageDisclaimer", title: "Data Collection", image: "https://cdn.rawgit.com/ady624/${handle()}/master/resources/icons/settings.png", required: false
		}

		section(title: "Maintenance") {
			paragraph "Memory usage is at ${mem()}", required: false
			if(!isHubitat()) input "redirectContactBook", "bool", title: "Redirect all Contact Book requests as PUSH notifications", description: "SmartThings has removed the Contact Book feature and as a result, all uses of Contact Book are by default ignored. By enabling this option, you will get all the existing Contact Book uses fall back onto the PUSH notification system, possibly allowing other people to receive these notifications.", defaultValue: false, required: true
			input "disabled", "bool", title: "Disable all pistons", description: "Disable all pistons belonging to this instance", defaultValue: false, required: false
			input "logPistonExecutions", "bool", title: "Log piston executions?", description: "Tap here to change logging pistons as hub location events", defaultValue: isHubitat() ? false : true, required: false
			if(isHubitat()) input "enableDashNotifications", "bool", title: "Enable Dashboard Notifications for device state changes?", description: "Tap here to change enable dashboard notifications of device state changes (more overhead)", defaultValue: false, required: false
			href "pageRebuildCache", title: "Clean up and rebuild data cache", description: "Tap here to change your clean up and rebuild your data cache"
		}

		section(title: "Recovery") {
			paragraph "webCoRE can run a recovery procedure every so often. This augments the built-in automatic recovery procedures that allows webCoRE to rely on all healthy pistons to keep the failed ones running."
			input "recovery", "enum", title: "Run recovery", options: ["Never", "Every 5 minutes", "Every 10 minutes", "Every 15 minutes", "Every 30 minutes", "Every 1 hour", "Every 3 hours"], description: "Allows recovery procedures to run every so often", defaultValue: "Every 30 minutes", required: true
		}

		section("Uninstall") {
			href "pageRemove", title: "Uninstall ${handle()}", description: "Tap here to uninstall ${handle()}"
		}

	}
}

private pageFuelStreams(){
	dynamicPage(name: "pageFuelStreams", title: "", uninstall: false, install: false){
		section(){
			app([title: isHubitat() ? 'Do not click - Launches automatically' : 'Fuel Streams', multiple: true, install: true, uninstall: false], 'fuelStreams', 'ady624', "${handle()} Fuel Stream")
		}
	}
}

private pageChangePassword() {
	dynamicPage(name: "pageChangePassword", title: "", nextPage: "pageSavePassword") {
		section() {
			paragraph "Choose a security password for your dashboard. You will need to enter this password when accessing your dashboard for the first time and possibly from time to time.", required: false
		}
		pageSectionPIN()
	}
}

private pageSectionPIN() {
	section() {
	input "PIN", "password", title: "Choose a security password for your dashboard", required: true
	input "expiry", "enum", options: ["Every hour", "Every day", "Every week", "Every month (recommended)", "Every three months", "Never (not recommended)"], defaultValue: "Every month (recommended)", title: "Choose how often the dashboard login expires", required: true
	}

}

private pageSavePassword() {
	initTokens()
	dynamicPage(name: "pageSavePassword", title: "") {
		section() {
			paragraph "Your password has been changed. Please note you may need to reauthenticate when opening the dashboard.", required: false
		}
	}
}

def pageRebuildCache() {
	cleanUp()
	dynamicPage(name: "pageRebuildCache", title: "", install: false, uninstall: false) {
		section() {
			paragraph "Success! Data cache has been cleaned up and rebuilt."
		}
	}
}

/*
def pageIntegrations() {
	//clear devices cache
	dynamicPage(name: "pageIntegrations", title: "", install: false, uninstall: false) {
	def twilio = settings.twilio_sid && settings.twilio_token && settings.twilio_number
		section() {
			paragraph "Integrate other services into webCoRE to extend its capabilities."
		}
		section("Available integrations") {
			href "pageIntegrationAskAlexa", title: "Ask Alexa", description: "Allow interactions with AskAlexa"
			href "pageIntegrationIFTTT", title: "IFTTT", description: "Allow IFTTT interactions with external services"
			href "pageIntegrationTwilio", title: "Twilio", description: "Allows two-way SMS interactions", state: twilio ? 'complete' : null, required: twilio
		}
	}
}


def pageIntegrationIFTTT() {
	return dynamicPage(name: "pageIntegrationIFTTT", title: "IFTTT Integration", nextPage: settings.iftttEnabled ? "pageIntegrationIFTTTConfirm" : null) {
		section() {
			paragraph "CoRE can optionally integrate with IFTTT (IF This Then That) via the Maker channel, triggering immediate events to IFTTT. To enable IFTTT, please login to your IFTTT account and connect the Maker channel. You will be provided with a key that needs to be entered below", required: false
			input "iftttEnabled", "bool", title: "Enable IFTTT", submitOnChange: true, required: false
			if (settings.iftttEnabled) href name: "", title: "IFTTT Maker channel", required: false, style: "external", url: "https://www.ifttt.com/maker", description: "tap to go to IFTTT and connect the Maker channel"
		}
		if (settings.iftttEnabled) {
			section("IFTTT Maker key"){
				input("iftttKey", "string", title: "Key", description: "Your IFTTT Maker key", required: false)
			}
		}
	}
}

def pageIntegrationIFTTTConfirm() {
	if (testIFTTT()) {
		return dynamicPage(name: "pageIntegrationIFTTTConfirm", title: "IFTTT Integration") {
			section(){
				paragraph "Congratulations! You have successfully connected CoRE to IFTTT."
			}
		}
	} else {
		return dynamicPage(name: "pageIntegrateIFTTTConfirm",  title: "IFTTT Integration") {
			section(){
				paragraph "Sorry, the credentials you provided for IFTTT are invalid. Please go back and try again."
			}
		}
	}
}

def pageIntegrationTwilio() {
	//clear devices cache
	dynamicPage(name: "pageIntegrationTwilio", title: "Twilio", install: false, uninstall: false) {
		section() {
			paragraph "Twilio allows two-way messaging between you and webCoRE, bringing interactivity to your automations."
			paragraph "NOTE: Usage charges apply to your Twilio account and possibly your mobile phone bill.", required: true
		}
	section() {
		paragraph "You will need to setup a Twilio account, purchase a number, and configure a Messaging Service to get this intergration working."
			href "", title: "How to configure your Twilio account", style: "external", url: "${getWikiUrl()}Twilio", description: "Tap to open", required: false
	}
		section("Twilio settings") {
		paragraph "Login to your Twilio and go to your console. Find the Account SID and the Auth Token and copy and paste them below:"
		input "twilio_sid", "password", title: "Twilio account SID", required: true
		input "twilio_token", "password", title: "Twilio authorization token", required: true
		input "twilio_number", "text", title: "Twilio phone number (+E.164 format)", required: true, defaultValue: "+"
		}

	section("Test your settings") {
		paragraph "Once you have provided all details, test your integration here"
		input "twilio_test_number", "text", title: "Your mobile phone number (+E.164 format)", defaultValue: "+"
		input "twilio_test_message", "text", title: "A test message", defaultValue: "This is a test message from webCoRE"
			href "pageIntegrationTwilioTest", title: "Test your Twilio account"
	}
	}
}

def pageIntegrationTwilioTest() {
	def data = [
		s: settings.twilio_sid,
		t: settings.twilio_token,
		n: settings.twilio_number,
		p: settings.twilio_test_number,
		m: settings.twilio_test_message
	]
	def requestParams = [
		uri:  "https://api.webcore.co/sms/send/",
		query: null,
		requestContentType: "application/json",
		body: data
	]
	def success = false
	httpPost(requestParams) { response ->
		if (response.status == 200) {
			def jsonData = response.data instanceof Map ? response.data : (LinkedHashMap) new groovy.json.JsonSlurper().parseText(response.data)
			if (jsonData && (jsonData.result == 'OK')) {
				success = true
			}
		}
	}
	dynamicPage(name: "pageIntegrationTwilioTest", title: "Twilio Test", install: false, uninstall: false) {
		section("Test result") {
			if (success) {
				paragraph "Congratulations! Your Twilio account is correctly setup."
			} else {
				paragraph "Oh-oh, something unexpected happened. Please check your settings and try again.", required: true
			}
		}
	}
}
*/

def pageRemove() {
	dynamicPage(name: "pageRemove", title: "", install: false, uninstall: true) {
		section('CAUTION') {
			paragraph "You are about to completely remove ${handle()} and all of its pistons.", required: true
			paragraph "This action is irreversible.", required: true
			paragraph "If you are sure you want to do this, please tap on the Remove button below.", required: true
		}
	}
}






/******************************************************************************/
/*** 																		***/
/*** INITIALIZATION ROUTINES												***/
/*** 																		***/
/******************************************************************************/


def installed() {
	state.installed = true
	initialize()
	return true
}

def updated() {
	warn "Updating webCoRE ${version()}"
	unsubscribe()
	unschedule()
	initialize()

	def chg1 = false
	def chg2 = false
	def chg3 = false

	if(state.disabled != disabled) {
		state.disabled = disabled
		chg1 = true
	}
	if(state.lPE != logPistonExecutions) {
		state.lPE = logPistonExecutions
		chg2 = true
	}
	if(state.cV != version()) {
		state.cV = version()
		chg3 = true
	}
	if(chg1 || chg2 || chg3) {
		def name = handle() + ' Piston'
		def t0 = getChildApps().findAll{ it.name == name }
		t0.each {
			updatePistonsW(it, chg1, chg2, chg3)
		}
	}

	return true
}

public updatePistonsW(piston, ch1=true, ch2=true, ch3=true) {
	if(ch1) piston.settingsToState('disabled', disabled)
	if(ch2) piston.settingsToState('logPistonExecutions', logPistonExecutions)
	if(ch3) piston.settingsToState('cVersion', version())
}

private initialize() {
	subscribeAll()
	state.vars = state.vars ?: [:]
	state.version = version()
	if (state.installed && settings.agreement) {
		registerInstance()
	}
	def recoveryMethod = (settings.recovery ?: 'Every 30 minutes').replace('Every ', 'Every').replace(' minute', 'Minute').replace(' hour', 'Hour')
	if (recoveryMethod != 'Never') {
		try {
			"run$recoveryMethod"(recoveryHandler)
		} catch (all) { }
	}

	//move lifx
/*
	if (state.settings && state.settings.lifx_scenes) {
		state.lifx = [
			scenes: state.settings.lifx_scenes,
			lights: state.settings.lifx_lights,
			groups: state.settings.lifx_groups,
			locations: state.settings.lifx_locations
		]
		state.settings.remove('lifx_scenes')
		state.settings.remove('lifx_lights')
		state.settings.remove('lifx_groups')
		state.settings.remove('lifx_locations')
	}
*/
	
	if(state.accessToken){
		updateEndpoint(state.accessToken)
	}
}

private updateEndpoint(accessToken){
	if(isCustomEndpoint()){
		state.endpoint = customServerUrl("?access_token=${accessToken}")
	}
	else {
		state.endpoint = isHubitat() ? apiServerUrl("$hubUID/apps/${app.id}/?access_token=${accessToken}") : apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")
	}
}

private initializeWebCoREEndpoint() {
	try {
		if (!state.endpoint) {
			try {
				def accessToken = createAccessToken()
				if (accessToken) {
					updateEndpoint(accessToken)
				}
				else {
					enableOauth()
					return initializeWebCoREEndpoint()
				}
			} catch(e) {
				state.endpoint = null
			}
		}
		return state.endpoint
	} catch (all) {
		error "An error has occurred during endpoint initialization: ", all
	}
	return false
}

private enableOauth() {
	def params = [
		uri: "http://localhost:8080/app/edit/update?_action_update=Update&oauthEnabled=true&id=${app.appTypeId}",
		headers: ['Content-Type':'text/html;charset=utf-8']
	]
	try {
		httpPost(params) { resp ->
			//LogTrace("response data: ${resp.data}")
		}
	} catch (e) {
		log.debug "enableOauth something went wrong: ${e}"
	}
}

private getHub() {
	return location.getHubs().find{ it.getType().toString() == 'PHYSICAL' }
}

private subscribeAll() {
	subscribe(location, "${handle()}.poll", webCoREHandler)
	subscribe(location, "${'@@' + handle()}", webCoREHandler)
//	subscribe(location, "askAlexa", askAlexaHandler)
//	subscribe(location, "echoSistant", echoSistantHandler)
	subscribe(location, "HubUpdated", hubUpdatedHandler, [filterEvents: false])
	subscribe(location, "summary", summaryHandler, [filterEvents: false])
	if(isHubitat()) subscribe(location, "hsmStatus", hsmHandler, [filterEvents: false])
	setPowerSource(getHub()?.isBatteryInUse() ? 'battery' : 'mains')
}

/******************************************************************************/
/*** 																		***/
/*** DASHBOARD MAPPINGS														***/
/*** 																		***/
/******************************************************************************/

mappings {
	//path("/dashboard") {action: [GET: "api_dashboard"]}
	path("/intf/dashboard/load") {action: [GET: "api_intf_dashboard_load"]}
	path("/intf/dashboard/refresh") {action: [GET: "api_intf_dashboard_refresh"]}
	path("/intf/dashboard/piston/new") {action: [GET: "api_intf_dashboard_piston_new"]}
	path("/intf/dashboard/piston/create") {action: [GET: "api_intf_dashboard_piston_create"]}
	path("/intf/dashboard/piston/backup") {action: [GET: "api_intf_dashboard_piston_backup"]}
	path("/intf/dashboard/piston/get") {action: [GET: "api_intf_dashboard_piston_get"]}
	path("/intf/dashboard/piston/set") {action: [GET: "api_intf_dashboard_piston_set"]}
	path("/intf/dashboard/piston/set.start") {action: [GET: "api_intf_dashboard_piston_set_start"]}
	path("/intf/dashboard/piston/set.chunk") {action: [GET: "api_intf_dashboard_piston_set_chunk"]}
	path("/intf/dashboard/piston/set.end") {action: [GET: "api_intf_dashboard_piston_set_end"]}
	path("/intf/dashboard/piston/pause") {action: [GET: "api_intf_dashboard_piston_pause"]}
	path("/intf/dashboard/piston/resume") {action: [GET: "api_intf_dashboard_piston_resume"]}
	path("/intf/dashboard/piston/set.bin") {action: [GET: "api_intf_dashboard_piston_set_bin"]}
	path("/intf/dashboard/piston/tile") {action: [GET: "api_intf_dashboard_piston_tile"]}
	path("/intf/dashboard/piston/set.category") {action: [GET: "api_intf_dashboard_piston_set_category"]}
	path("/intf/dashboard/piston/logging") {action: [GET: "api_intf_dashboard_piston_logging"]}
	path("/intf/dashboard/piston/clear.logs") {action: [GET: "api_intf_dashboard_piston_clear_logs"]}
	path("/intf/dashboard/piston/delete") {action: [GET: "api_intf_dashboard_piston_delete"]}
	path("/intf/dashboard/piston/evaluate") {action: [GET: "api_intf_dashboard_piston_evaluate"]}
	path("/intf/dashboard/piston/test") {action: [GET: "api_intf_dashboard_piston_test"]}
	path("/intf/dashboard/piston/activity") {action: [GET: "api_intf_dashboard_piston_activity"]}
	path("/intf/dashboard/presence/create") {action: [GET: "api_intf_dashboard_presence_create"]}
	path("/intf/dashboard/variable/set") {action: [GET: "api_intf_variable_set"]}
	path("/intf/dashboard/settings/set") {action: [GET: "api_intf_settings_set"]}
	path("/intf/fuelstreams/list") {action: [GET: "api_intf_fuelstreams_list"]}
	path("/intf/fuelstreams/get") {action: [GET: "api_intf_fuelstreams_get"]}
	path("/intf/location/entered") {action: [GET: "api_intf_location_entered"]}
	path("/intf/location/exited") {action: [GET: "api_intf_location_exited"]}
	path("/intf/location/updated") {action: [GET: "api_intf_location_updated"]}
	path("/ifttt/:eventName") {action: [GET: "api_ifttt", POST: "api_ifttt"]}
	path("/email/:pistonId") {action: [POST: "api_email"]}
	path("/execute/:pistonIdOrName") {action: [GET: "api_execute", POST: "api_execute"]}
	path("/tap") {action: [POST: "api_tap"]}
	path("/tap/:tapId") {action: [GET: "api_tap"]}
}

private api_get_error_result(error) {
	return [
		name: location.name + ' \\ ' + (app.label ?: app.name),
		error: error,
		now: now()
	]
}

private getHubitatVersion(){
	try {
		return location.getHubs().collectEntries {[it.id, it.getFirmwareVersionString()]}
	}
	catch(e){
	 	return location.getHubs().collectEntries {[it.id, "< 1.1.2.112"]}
	}
}

private api_get_base_result(deviceVersion = 0, updateCache = false) {
	def tz = location.getTimeZone()
	def currentDeviceVersion = state.deviceVersion
	def Boolean sendDevices = (deviceVersion != currentDeviceVersion)
	def name = handle() + ' Piston'
	def incidentThreshold = now() - 604800000
	
	def instanceId = hashId(app.id, updateCache)

//	def t0 = location.getHubs().collect{ [id: hashId(it.id, updateCache), name: it.name, firmware: isHubitat() ? getHubitatVersion()[it.id] : it.getFirmwareVersionString(), physical: it.getType().toString().contains('PHYSICAL'), powerSource: it.isBatteryInUse() ? 'battery' : 'mains' ]}
//	error "api_get_base_result: hubs ${location.getHubs()} t0: ${t0}"
//	error "api_get_base_result: locstatus ${location.hsmStatus} statehsm: ${state.hsmStatus}  shm ${transformHsmStatus(location.hsmStatus ?: state.hsmStatus)}"
	return [
		name: location.name + ' \\ ' + (app.label ?: app.name),
		instance: [
			account: [id: hashId(hubUID ?: app.getAccountId(), updateCache)],
			pistons: getChildApps().findAll{ it.name == name }.sort{ it.label }.collect{ [ id: hashId(it.id, updateCache), 'name': it.label, 'meta': state[hashId(it.id, updateCache)] ] },
			id: instanceId,
			locationId: hashId(location.id + (isHubitat() ? '-L' : ''), updateCache),
			name: app.label ?: app.name,
			uri: state.endpoint,
			deviceVersion: currentDeviceVersion,
			coreVersion: version(),
			enabled: !settings.disabled,
			settings: state.settings ?: [:],
			lifx: state.lifx ?: [:],
			virtualDevices: virtualDevices(updateCache),
			globalVars: listAvailableVariables(),
			fuelStreamUrls: getFuelStreamUrls(instanceId),
		] + (sendDevices ? [contacts: [:], devices: listAvailableDevices(false, updateCache)] : [:]),
		location: [
			contactBookEnabled: location.getContactBookEnabled(),
			hubs: location.getHubs().collect{ [id: hashId(it.id, updateCache), name: it.name, firmware: isHubitat() ? getHubitatVersion()[it.id] : it.getFirmwareVersionString(), physical: it.getType().toString().contains('PHYSICAL'), powerSource: it.isBatteryInUse() ? 'battery' : 'mains' ]},
			incidents: isHubitat() ? [] : location.activeIncidents.collect{[date: it.date.time, title: it.getTitle(), message: it.getMessage(), args: it.getMessageArgs(), sourceType: it.getSourceType()]}.findAll{ it.date >= incidentThreshold },
			id: hashId(location.id + (isHubitat() ? '-L' : ''), updateCache),
			mode: hashId(location.getCurrentMode().id, updateCache),
			modes: location.getModes().collect{ [id: hashId(it.id, updateCache), name: it.name ]},
			shm: isHubitat() ? transformHsmStatus(location.hsmStatus) : location.currentState("alarmSystemStatus")?.value,
			name: location.name,
			temperatureScale: location.getTemperatureScale(),
			timeZone: tz ? [
				id: tz.ID,
				name: tz.displayName,
				offset: tz.rawOffset
			] : null,
			zipCode: location.getZipCode(),
		],
		now: now(),
	]
}

private getFuelStreamUrls(iid){
	if(!useLocalFuelStreams()){
		def region = state.endpoint.contains('graph-eu') ? 'eu' : 'us'
		def baseUrl = 'https://api-' + region + '-' + iid[32] + '.webcore.co:9287/fuelStreams'
		def headers = [ 'Auth-Token' : iid ]

		return [
			list : [l: false, m: 'POST', h: headers, u: baseUrl + '/list', d: [i : iid]],
			get  : [l: false, m: 'POST', h: headers, u: baseUrl + '/get',  d: [ i: iid ], p: 'f']
		]
	}	
	
	def baseUrl = isCustomEndpoint() ? customServerUrl("/") :
						isHubitat() ? apiServerUrl("$hubUID/apps/${app.id}/")
							: apiServerUrl("/api/token/${state.accessToken}/smartapps/installations/${app.id}/")
	
	def params = baseUrl.contains(state.accessToken) ? "" : "access_token=${state.accessToken}"
	return [
		list : [l: true, u: baseUrl + "intf/fuelstreams/list?${params}"],
		get  : [l: true, u: baseUrl + "intf/fuelstreams/get?id={fuelStreamId}${params ? "&" + params : ""}", p: 'fuelStreamId']
	]
}

private boolean useLocalFuelStreams(){
 	return settings.localFuelStreams != null ? settings.localFuelStreams : (isHubitat() ? true : false)
}

private String transformHsmStatus(status){
	if(status == null) return "unconfigured"
	switch(status){
		case "disarmed":
		case "allDisarmed":
			return "off"
			break;
		case "armedHome":
		case "armedNight":
			return "stay"
			break;
		case "armedAway":
			return "away"
			break;
		default:
			return "Unknown"
	}
}

private api_intf_dashboard_load() {
	def result
	recoveryHandler()
	//install storage app
	def storageApp = getStorageApp(true)
	//debug "Dashboard: Request received to initialize instance"
	if (verifySecurityToken(params.token)) {
		result = api_get_base_result(params.dev, true)
		if (params.dashboard == "1") {
			startDashboard()
		} else {
	 		stopDashboard()
		}
	} else {
		if (params.pin) {
			if (settings.PIN && (md5("pin:${settings.PIN}") == params.pin)) {
				result = api_get_base_result()
				result.instance.token = createSecurityToken()
			} else {
				error "Dashboard: Authentication failed due to an invalid PIN"
			}
		}
		if (!result) result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	//for accuracy, use the time as close as possible to the render
	result.now = now()
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_refresh() {
	//debug "Dashboard: Request received to refresh instance"
	startDashboard()
	def result
	if (verifySecurityToken(params.token)) {
		def storageApp = getStorageApp(true)
		result = storageApp ? storageApp.getDashboardData() : getDashboardData()
	} else {
		if (!result) result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	//for accuracy, use the time as close as possible to the render
	result.now = now()
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

def Map getDashboardData() {
	boolean ok
	def value
	def item
	def start = now()
	return settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id)): dev]}.collectEntries{ id, dev ->
	        [ (id): dev.getSupportedAttributes().collect{ it.name }.unique().collectEntries {
	                try { value = dev.currentValue(it); } catch (all) { value = null};
                        return [ (it) : value]
		}]
	}
}

private api_intf_dashboard_piston_new() {
	def result
	debug "Dashboard: Request received to generate a new piston name"
	if (verifySecurityToken(params.token)) {
		result = [status: "ST_SUCCESS", name: generatePistonName()]
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_create() {
	def result
	debug "Dashboard: Request received to generate a new piston name"
	if (verifySecurityToken(params.token)) {
		def piston = addChildApp("ady624", "${handle()} Piston", params.name?:generatePistonName())
		if (params.author || params.bin) {
			piston.config([bin: params.bin, author: params.author, initialVersion: version()])
			updatePistonsW(piston)
			def settings = atomicState.settings
			piston.settingsToState('settings', settings)
		}
		if (isHubitat() && !piston.isInstalled()) piston.installed()
			result = [status: "ST_SUCCESS", id: hashId(piston.id)]
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_get() {
	def result
	debug "Dashboard: Request received to get piston ${params?.id}"
	if (verifySecurityToken(params.token)) {
		def pistonId = params.id
		def serverDbVersion = version()
		def clientDbVersion = params.db
		def requireDb = serverDbVersion != clientDbVersion
		if (pistonId) {
			result = api_get_base_result(requireDb ? 0 : params.dev, true)
			def piston = getChildApps().find{ hashId(it.id) == pistonId };
			if (piston) {
				result.data = piston.get() ?: [:]
			}
			if (requireDb) {
				result.dbVersion = serverDbVersion
			result.db = [
				capabilities: capabilities().sort{ it.value.d },
				commands: [
					physical: commands().sort{ it.value.d ?: it.value.n },
					virtual: virtualCommands().sort{ it.value.d ?: it.value.n }
				],
				attributes: attributes().sort{ it.key },
				comparisons: comparisons(),
				functions: functions(),
				colors: [
					standard: colorUtil?.ALL ?: getColors()
				],
			]
			}
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	//for accuracy, use the time as close as possible to the render
	result.now = now()
	def jsonData = groovy.json.JsonOutput.toJson(result)
	
	if(isHubitat() && (!isCustomEndpoint() || customHubUrl.contains(hubUID))){
		//data saver for hubitat ~100K limit	
		def responseLength = jsonData.getBytes("UTF-8").length
		if(responseLength > 100 * 1024){ //these are loaded anyway right after loading the piston
			log.warn "Trimming ${ (int)(responseLength/1024) }KB response to smaller size"
			result.instance = null
			result.data?.logs = []
			result.data?.stats?.timing = []
			//for accuracy, use the time as close as possible to the render
			result.now = now()
			jsonData = groovy.json.JsonOutput.toJson(result)
		}
	}
	
	//log.debug "Trimmed resonse length: ${jsonData.getBytes("UTF-8").length}"	
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${jsonData})"
}


private api_intf_dashboard_piston_backup() {
	def result = [pistons: []]
	debug "Dashboard: Request received to backup pistons ${params?.id}"
	if (verifySecurityToken(params.token)) {
		def pistonIds = (params.ids ?: '').tokenize(',')
		for(pistonId in pistonIds) {
			if (pistonId) {
				def piston = getChildApps().find{ hashId(it.id) == pistonId };
				if (piston) {
					def pd = piston.get(true)
					pd.instance = [id: hashId(app.id), name: app.label]
					if (pd) result.pistons.push(pd)
				}
			}
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	//for accuracy, use the time as close as possible to the render
	result.now = now()
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private decodeEmoji(value) {
	if (!value) return ''
	return value.replaceAll(/(\:%[0-9A-F]{2}%[0-9A-F]{2}%[0-9A-F]{2}%[0-9A-F]{2}\:)/, { m -> URLDecoder.decode(m[0].substring(1, 13), 'UTF-8') })
};


private api_intf_dashboard_piston_set_save(id, data, chunks) {
	def piston = getChildApps().find{ hashId(it.id) == id };
	if (piston) {
	/*
		def s = decodeEmoji(new String(data.decodeBase64(), "UTF-8"))
		int cs = 512
		for (int a = 0; a <= Math.floor(s.size() / cs); a++) {
			int x = a * cs + cs - 1;
		if (x >= s.size()) x = s.size() - 1
			log.trace s.substring(a * cs, x)
		}
	*/
		def p = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(decodeEmoji(new String(data.decodeBase64(), "UTF-8")))
		def result = piston.setup(p, chunks);
		broadcastPistonList()
		return result
	}
	return false;
}

//set is used for small pistons, for large data, using set.start, set.chunk, and set.end
private api_intf_dashboard_piston_set() {
	def result
	debug "Dashboard: Request received to set a piston"
	if (verifySecurityToken(params.token)) {
		def data = params?.data
		//save the piston here
		def saved = api_intf_dashboard_piston_set_save(params?.id, data, ['chunk:0' : data])
		if (saved) {
			if (saved.rtData) {
				updateRunTimeData(saved.rtData)
				saved.rtData = null
			}
			result = [status: "ST_SUCCESS"] + saved
		} else {
			result = [status: "ST_ERROR", error: "ERR_UNKNOWN"]
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_set_start() {
	def result
	debug "Dashboard: Request received to set a piston (chunked start)"
	if (verifySecurityToken(params.token)) {
		def chunks = "${params?.chunks}";
		chunks = chunks.isInteger() ? chunks.toInteger() : 0;
		if ((chunks > 0) && (chunks < 100)) {
			atomicState.hash = [:]
			atomicState.chunks = [id: params?.id, count: chunks];
			result = [status: "ST_READY"]
		} else {
			result = [status: "ST_ERROR", error: "ERR_INVALID_CHUNK_COUNT"]
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_set_chunk() {
	def result
	def chunk = "${params?.chunk}"
	chunk = chunk.isInteger() ? chunk.toInteger() : -1
	debug "Dashboard: Request received to set a piston chunk (#${1 + chunk}/${state.chunks?.count})"
	if (verifySecurityToken(params.token)) {
		def data = params?.data
		def chunks = state.chunks
		if (chunks && chunks.count && (chunk >= 0) && (chunk < chunks.count)) {
			chunks["chunk:$chunk"] = data;
			atomicState.chunks = chunks;
			result = [status: "ST_READY"]
		} else {
			result = [status: "ST_ERROR", error: "ERR_INVALID_CHUNK"]
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_set_end() {
	def result
	debug "Dashboard: Request received to set a piston (chunked end)"
	if (verifySecurityToken(params.token)) {
		def chunks = state.chunks
		if (chunks && chunks.count) {
			def ok = true
			def data = ""
			def i = 0;
			def count = chunks.count;
			while(i<count) {
				def s = chunks["chunk:$i"]
				if (s) {
					data += s
				} else {
					data = ""
					ok = false;
					break;
				}
				i++
			}
			atomicState.chunks = null
			if (ok) {
				//save the piston here
				def saved = api_intf_dashboard_piston_set_save(chunks.id, data, chunks.findAll{ it.key.startsWith('chunk:') })
				if (saved) {
					if (saved.rtData) {
						updateRunTimeData(saved.rtData)
						saved.rtData = null
					}
					result = [status: "ST_SUCCESS"] + saved
				} else {
					result = [status: "ST_ERROR", error: "ERR_UNKNOWN"]
				}
			} else {
				result = [status: "ST_ERROR", error: "ERR_INVALID_CHUNK"]
			}
		} else {
			result = [status: "ST_ERROR", error: "ERR_INVALID_CHUNK"]
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_pause() {
	def result
	debug "Dashboard: Request received to pause a piston"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			def rtData = piston.pausePiston()
			updateRunTimeData(rtData)
			//update the state because it will overwrite the atomicState
			//state[piston.id] = state[piston.id]
			result = [status: "ST_SUCCESS", active: false]
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_resume() {
	def result
	debug "Dashboard: Request received to resume a piston"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			def rtData = piston.resume()
			result = rtData.result
			updateRunTimeData(rtData)
			//update the state because it will overwrite the atomicState
			//state[piston.id] = state[piston.id]
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_test() {
	def result
	debug "Dashboard: Request received to test a piston"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			result = piston.test()
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_presence_create() {
	def result
	if (verifySecurityToken(params.token)) {
		def dni = params.dni
		def sensor = (dni ? getChildDevices().find{ it.getDeviceNetworkId() == dni } : null) ?: addChildDevice("ady624", handle() + " Presence Sensor", dni ?: hashId("${now()}"), null, [label: params.name])
		sensor.updateLabel(params.name);
		if (sensor) {
			result = [
				status: "ST_SUCCESS",
				deviceId: hashId(sensor.id)
			]
			refreshDevices()
		} else {
			result = api_get_error_result("ERR_COULD_NOT_CREATE_DEVICE")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_tile() {
	def result
	debug "Dashboard: Clicked a piston tile"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			result = piston.clickTile(params.tile)
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_set_bin() {
	def result
	debug "Dashboard: Request received to set piston bin"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			result = piston.setBin(params.bin)
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}



private api_intf_dashboard_piston_set_category() {
	def result
	debug "Dashboard: Request received to set piston category"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			result = piston.setCategory(params.category)
			def st = state[params.id]
			if (st) {
				st.c = params.category
				state[params.id] = st
			}
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_logging() {
	def result
	debug "Dashboard: Request received to set piston logging level"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			result = piston.setLoggingLevel(params.level)
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_clear_logs() {
	def result
	debug "Dashboard: Request received to clear piston logs"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
		result = piston.clearLogs()
			result.status = "ST_SUCCESS"
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_delete() {
	def result
	debug "Dashboard: Request received to delete a piston"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			app.deleteChildApp(isHubitat() ? piston.id : piston)
			result = [status: "ST_SUCCESS"]
			state.remove(params.id)
			state.remove('sph${params.id}')
			broadcastPistonList()
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_location_entered() {
	def deviceId = params.device
	def dni = params.dni
	def device = getChildDevices().find{ (it.getDeviceNetworkId() == dni) || (hashId(it.id) == deviceId) }
   	if (device && params.place) device.processEvent([name: 'entered', place: params.place, places: state.settings.places])
}

private api_intf_location_exited() {
	def deviceId = params.device
	def dni = params.dni
	def device = getChildDevices().find{ (it.getDeviceNetworkId() == dni) || (hashId(it.id) == deviceId) }
   	if (device && params.place) device.processEvent([name: 'exited', place: params.place, places: state.settings.places])
}

private api_intf_location_updated() {
	def deviceId = params.device
	def dni = params.dni
	def device = getChildDevices().find{ (it.getDeviceNetworkId() == dni) || (hashId(it.id) == deviceId) }
	Map location = params.location ? (LinkedHashMap) new groovy.json.JsonSlurper().parseText(params.location) : [error: "Invalid data"]
	if (device) device.processEvent([name: 'updated', location: location, places: state.settings.places])
}

private api_intf_variable_set() {
	def result
	debug "Dashboard: Request received to set a variable"
	if (verifySecurityToken(params.token)) {
		def pid = params.id;
		def name = params.name;
		def value = params.value ? (LinkedHashMap) new groovy.json.JsonSlurper().parseText(new String(params.value.decodeBase64(), "UTF-8")) : null
		Map globalVars
		Map localVars
		if (!pid) {
			globalVars = atomicState.vars ?: [:]
			if (name && !value) {
				//deleting a variable
				globalVars.remove(name);
			} else if (value && value.n) {
				if (!name || (name != value.n)) {
					//add a new variable
					if (name) globalVars.remove(name);
					globalVars[value.n] = [t: value.t, v: value.v]
				} else {
					//update a variable
					globalVars[name] = [t: value.t, v: value.v]
				}
				sendVariableEvent([name: value.n, value: value.v, type: value.t])
			}
			atomicState.vars = globalVars
			result = [status: "ST_SUCCESS"] + [globalVars: globalVars]
		} else {
			def piston = getChildApps().find{ hashId(it.id) == pid };
			if (piston) {
				localVars = piston.setLocalVariable(name, value.v)
			}
			result = [status: "ST_SUCCESS"] + [id: pid, localVars: localVars]
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

public resetFuelStreamList(){
	state.fuelStreams = []
	state.remove("fuelStreams")
}

public writeToFuelStream(req){
	def name = "${handle()} Fuel Stream"
	def streamName = "${(req.c ?: "")}||${req.n}"
	
	def result = getChildApps().find{ it.name == name && it.label.contains(streamName)}
	def fuelStreams = isHubitat() ? [] : atomicState.fuelStreams ?: []
	
	if(!result){
		if(fuelStreams.find{ it.contains(streamName) } ?: false){ //bug in smartthings doesn't remember state,childapps between multiple calls in the same piston
			error "Found duplicate stream, not adding point"
			return
		}
		def id =  (getChildApps().findAll{ it.name == name }.collect{ it.label.split(' - ')[0].toInteger()}.max() ?: 0) + 1
		try {
			result = addChildApp('ady624', name, "$id - $streamName")
			if(!isHubitat()){
				fuelStreams = getChildApps().find{ it.name == name }.collect { it.label }
				fuelStreams << result.label
				atomicState.fuelStreams = fuelStreams
			}		
	   		result.createStream([id: id, name: req.n, canister: req.c ?: ""])
		}
		catch(e){
			error "Please install the webCoRE Fuel Streams app for local Fuel Streams"
			return
		}	 	
	}
	result.updateFuelStream(req)
}

private api_intf_fuelstreams_list() {
	def result = []
	def name = "${handle()} Fuel Stream"
	result = getChildApps().findAll{ it.name == name }*.getFuelStream()
	
   	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(["fuelStreams" : result])})"
}

private api_intf_fuelstreams_get() {
	def result = []
	def id = params.id
	
	def name = "${handle()} Fuel Stream"
	def stream = getChildApps().find { it.name == name && it.label.startsWith("$id -")}
	result = stream.listFuelStreamData()
	
   	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(["points" : result])})"
}

private api_intf_settings_set() {
	def result
	debug "Dashboard: Request received to set settings"
	if (verifySecurityToken(params.token)) {
		def settings = params.settings ? (LinkedHashMap) new groovy.json.JsonSlurper().parseText(new String(params.settings.decodeBase64(), "UTF-8")) : null
		atomicState.settings = settings

		def name = handle() + ' Piston'
		def t0 = getChildApps().findAll{ it.name == name }
		t0.each {
			it.settingsToState('settings', settings)
		}

		//testLifx()
		result = [status: "ST_SUCCESS"]
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_evaluate() {
	def result
	debug "Dashboard: Request received to evaluate an expression"
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			def expression = (LinkedHashMap) new groovy.json.JsonSlurper().parseText(new String(params.expression.decodeBase64(), "UTF-8"))
			def msg = timer "Evaluating expression"
			result = [status: "ST_SUCCESS", value: piston.proxyEvaluateExpression(getRunTimeData(), expression, params.dataType)]
			trace msg
		} else {
			result = api_get_error_result("ERR_INVALID_ID")
		}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

private api_intf_dashboard_piston_activity() {
	def result
	if (verifySecurityToken(params.token)) {
		def piston = getChildApps().find{ hashId(it.id) == params.id };
		if (piston) {
			result = [status: "ST_SUCCESS", activity: (piston.activity(params.log) ?: [:]) + [globalVars: listAvailableVariables()/*, mode: hashId(location.getCurrentMode().id), shm: location.currentState("alarmSystemStatus")?.value, hubs: location.getHubs().collect{ [id: hashId(it.id, updateCache), name: it.name, firmware: it.getFirmwareVersionString(), physical: it.getType().toString().contains('PHYSICAL'), powerSource: it.isBatteryInUse() ? 'battery' : 'mains' ]}*/]]
	} else {
			result = api_get_error_result("ERR_INVALID_ID")
	}
	} else {
		result = api_get_error_result("ERR_INVALID_TOKEN")
	}
	render contentType: "application/javascript;charset=utf-8", data: "${params.callback}(${groovy.json.JsonOutput.toJson(result)})"
}

def api_ifttt() {
	def data = [:]
	def remoteAddr = isHubitat() ? "UNKNOWN" : request.getHeader("X-FORWARDED-FOR") ?: request.getRemoteAddr()
	if (params) {
		data.params = [:]
	for(param in params) {
		if (!(param.key in ['theAccessToken', 'appId', 'action', 'controller'])) {
			data[param.key] = param.value
		}
	}
	}
	data = data + (request?.JSON ?: [:])
	data.remoteAddr = remoteAddr
	def eventName = params?.eventName
	if (eventName) {
		sendLocationEvent([name: "ifttt", value: eventName, isStateChange: true, linkText: "IFTTT event", descriptionText: "${handle()} has received an IFTTT event: $eventName", data: data])
	}
	render contentType: "text/html", data: "<!DOCTYPE html><html lang=\"en\">Received event $eventName.<body></body></html>"
}


def api_email() {
	def data = request?.JSON ?: [:]
	def from = data.from ?: ''
	def pistonId = params?.pistonId
	if (pistonId) {
		sendLocationEvent([name: "email", value: pistonId, isStateChange: true, linkText: "Email event", descriptionText: "${handle()} has received an email from $from", data: data])
	}
	render contentType: "text/plain", data: "OK"
}

private api_execute() {
	def result = [:]
	def data = [:]
	def remoteAddr = isHubitat() ? "UNKNOWN" : request.getHeader("X-FORWARDED-FOR") ?: request.getRemoteAddr()
	debug "Dashboard: Request received to execute a piston from IP $remoteAddr"
	if (params) {
		data = [:]
	for(param in params) {
		if (!(param.key in ['theAccessToken', 'appId', 'action', 'controller', 'pistonIdOrName'])) {
			data[param.key] = param.value
		}
	}
	}
	data = data + (request?.JSON ?: [:])
	data.remoteAddr = remoteAddr
	def pistonIdOrName = params?.pistonIdOrName
	def piston = getChildApps().find{ (it.label == pistonIdOrName) || (hashId(it.id) == pistonIdOrName) };
	if (piston) {
		sendLocationEvent(name: hashId(piston.id), value: remoteAddr, isStateChange: true, displayed: false, linkText: "Execute event", descriptionText: "External piston execute request from IP $remoteAddr", data: data)
	result.result = 'OK'
	} else {
		result.result = 'ERROR'
	}
	result.timestamp = (new Date()).time
	render contentType: "application/json", data: "${groovy.json.JsonOutput.toJson(result)}"
}

def recoveryHandler() {
	def t = now()
	def lastRecovered = state.lastRecovered
	def recTime = 120000
	if (lastRecovered && (now() - lastRecovered < recTime)) return
	atomicState.lastRecovered = now()
	def name = handle() + ' Piston'
	long threshold = now() - recTime
	def updateCache = true
	def failedPistons = getChildApps().findAll{ it.name == name }.collect{ [ id: hashId(it.id, updateCache), 'name': it.label, 'meta': state[hashId(it.id, updateCache)] ] }.findAll{ it.meta && it.meta.a && it.meta.n && (it.meta.n < threshold) }
	if (failedPistons.size()) {
		for (piston in failedPistons) {
			warn "Piston $piston.name was sent a recovery signal because it was ${now() - piston.meta.n}ms late"
			sendLocationEvent(name: piston.id, value: 'recovery', isStateChange: true, displayed: false, linkText: "Recovery event", descriptionText: "Recovery event for piston $piston.name")
		}
	}
	if (state.version != version()) {
		//updated
		atomicState.version = version()
		updated()
	}
	//log.trace "RECOVERY took ${now() - t}ms"
}






/******************************************************************************/
/*** PRIVATE METHODS								***/
/******************************************************************************/

private cleanUp() {
	try {
	List pistons = getChildApps().collect{ hashId(it.id) }
	for (item in state.findAll{ (it.key.startsWith('sph') && (it.value == 0)) || it.key.contains('-') || (it.key.startsWith(':') && !(it.key in pistons)) }) {
		state.remove(item.key)
	}
	state.remove('chunks')
	state.remove('hash')
	state.remove('virtualDevices')
	state.remove('updateDevices')
	state.remove('semaphore')
	state.remove('pong')
	state.remove('modules')
	state.remove('globalVars')
	state.remove('devices')
	api_get_base_result(1, true)
	} catch (all) {
	}
}

private getStorageApp(install = false) {
	def name = handle() + ' Storage'
	def storageApp = getChildApps().find{ it.name == name }

	if(storageApp) { // Hubitat does not use storage app for performance reasons;  Someone could have created it elsewhere in UI
		if(storageApp.getStorageSettings() != null){ //migrate settings off of storage app
			storageApp.getStorageSettings().findAll { it.key.startsWith('dev:') }.each {
				app.updateSetting(it.key, [type: 'capability', value: it.value.collect { it.id }])
			}
		}
		app.deleteChildApp(storageApp.id)
		return null
	}

/*
	def label = "${app.label}"
	if (storageApp) {
		if (label != storageApp.label) {
			storageApp.updateLabel(label)
		}
		return storageApp
	}

	try {
		storageApp = addChildApp("ady624", name, label)
	} catch (all) {
		error "Please install the webCoRE Storage SmartApp for better scale performance"
		return null
	}
	try {
		storageApp.initData(settings.collect{ it.key.startsWith('dev:') ? it : null }, settings.contacts)
		for (item in settings.collect{ it.key.startsWith('dev:') ? it : null }) {
			if (item && item.key) {
				app.updateSetting(item.key, [type: 'string', value: null])
			}
		}
		app.updateSetting('contacts', [type: 'string', value: null])
	} catch (all) {
	}
*/
	return storageApp
}

private getDashboardApp(install = false) {
	if(isHubitat()) {
		if(!enableDashNotifications) return null
	}
	//if(isHubitat()) return null
	def name = handle() + ' Dashboard'
	def label = app.label + ' (dashboard)'
	def dashboardApp = getChildApps().find{ it.name == name }
	if (dashboardApp) {
		if(isHubitat() && !enableDashNotifications) {
			app.deleteChildApp(dashboardApp.id)
			return null
		}
		if (label != dashboardApp.label) {
			dashboardApp.updateLabel(label)
		}
		return dashboardApp
	}
	try {
		dashboardApp = addChildApp("ady624", name, app.label)
	} catch (all) {
		return null
	}
	return dashboardApp
}

def customServerUrl(path){
	path ?: ""
	if(!path.startsWith("/")){
		path = "/" + path
	}
	
	if(customHubUrl.contains(hubUID)){
	 	return customHubUrl + "/" + app.id + path
	}
	return customHubUrl + "/apps/api/" + app.id + path
}


private String getDashboardInitUrl(register = false) {
	def url = register ? getDashboardRegistrationUrl() : getDashboardUrl()
	if (!url) return null
	def t0
	if(isCustomEndpoint()){
		//return url + (register ? "register/" : "init/") +	(
		t0 =  url + (register ? "register/" : "init/") +	(
			customServerUrl('/?access_token=' + state.accessToken)    ).bytes.encodeBase64()
	}
	else {
		//return url + (register ? "register/" : "init/") + 
		t0 = url + (register ? "register/" : "init/") + 
			 (apiServerUrl("").replace("https://", '').replace(".api.smartthings.com", "").replace(":443", "").replace("/", "") + 
	  		((hubUID ?: state.accessToken) + app.id).replace("-", "") + (isHubitat() ? '/?access_token=' + state.accessToken : '')).bytes.encodeBase64()
	}
	//log.debug "Url: $t0"
	return t0
//						isHubitat() ? apiServerUrl("$hubUID/apps/${app.id}/")
//							: apiServerUrl("/api/token/${state.accessToken}/smartapps/installations/${app.id}/")
//		state.endpoint = isHubitat() ? apiServerUrl("$hubUID/apps/${app.id}/?access_token=${accessToken}") : apiServerUrl("/api/token/${accessToken}/smartapps/installations/${app.id}/")
}

private String getDashboardRegistrationUrl() {
	if (!state.endpoint) return null
	return "https://api.${domain()}/dashboard/"
}

public Map listAvailableDevices(raw = false, updateCache = false) {
	def storageApp = getStorageApp()
	Map result = [:]
	if (storageApp) {
		result = storageApp.listAvailableDevices(raw)
	} else {
		def overrides = commandOverrides()
		if (raw) {
			result = settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id, updateCache)): dev]}
		} else {
			result = settings.findAll{ it.key.startsWith("dev:") }.collect{ it.value }.flatten().collectEntries{ dev -> [(hashId(dev.id, updateCache)): dev]}.collectEntries{ id, dev -> [ (id): [ n: dev.getDisplayName(), cn: dev.getCapabilities()*.name, a: dev.getSupportedAttributes().unique{ it.name }.collect{def x = [n: it.name, t: it.getDataType(), o: it.getValues()]; try {x.v = dev.currentValue(x.n);} catch(all) {}; x}, c: dev.getSupportedCommands().unique{ transformCommand(it, overrides) }.collect{[n: transformCommand(it, overrides), p: it.getArguments()]} ]]}
		}
	}
	List presenceDevices = getChildDevices()
	if (presenceDevices && presenceDevices.size()) {
		if (raw) {
			result << presenceDevices.collectEntries{ dev -> [(hashId(dev.id, updateCache)): dev]}
		} else {
			result << presenceDevices.collectEntries{ dev -> [(hashId(dev.id, updateCache)): dev]}.collectEntries{ id, dev -> [ (id): [ n: dev.getDisplayName(), cn: dev.getCapabilities()*.name, a: dev.getSupportedAttributes().unique{ it.name }.collect{def x = [n: it.name, t: it.getDataType(), o: it.getValues()]; try {x.v = dev.currentValue(x.n);} catch(all) {}; x}, c: dev.getSupportedCommands().unique{ it.getName() }.collect{[n: it.getName(), p: it.getArguments()]} ]]}
		}
	}
	return result
}

private def transformCommand(command, overrides){
	def override = overrides[command.getName()]
	if(override && override.s == command.getArguments()?.toString()){
		return override.r
	}
	return command.getName()
}


private setPowerSource(powerSource, atomic = true) {
	if (state.powerSource == powerSource) return
	if (atomic) {
		atomicState.powerSource = powerSource
	} else {
		state.powerSource = powerSource
	}
	sendLocationEvent([name: 'powerSource', value: powerSource, isStateChange: true, linkText: "webCoRE power source event", descriptionText: "${handle()} has detected a new power source: $powerSource"])
}

public Map listAvailableVariables() {
	return (state.vars ?: [:]).sort{ it.key }
}

private void initTokens() {
	debug "Dashboard: Initializing security tokens"
	state.securityTokens = [:]
}

private Boolean verifySecurityToken(tokenId) {
	def tokens = state.securityTokens
	if (!tokens) return false
	def threshold = now()
	def modified = false
	//remove all expired tokens
	for (token in tokens.findAll{ it.value < threshold }) {
		tokens.remove(token.key)
		modified = true
	}
	if (modified) {
		atomicState.securityTokens = tokens
	}
	def token = tokens[tokenId]
	if (!token || token < now()) {
		error "Dashboard: Authentication failed due to an invalid token"
		return false
	}
	return true
}

private String createSecurityToken() {
	trace "Dashboard: Generating new security token after a successful PIN authentication"
	def token = UUID.randomUUID().toString()
	def tokens = state.securityTokens ?: [:]
	long expiry = 0
	def eo = "$settings.expiry".toLowerCase().replace("every ", "").replace("(recommended)", "").replace("(not recommended)", "").trim()
	switch (eo) {
		case "hour": expiry = 3600; break;
		case "day": expiry = 86400; break;
		case "week": expiry = 604800; break;
		case "month": expiry = 2592000; break;
		case "three months": expiry = 7776000; break;
		case "never": expiry = 3110400000; break; //never means 100 years, okay?
	}
	tokens[token] = now() + (expiry * 1000)
	state.securityTokens = tokens
	//state.securityTokens = tokens
	return token
}

private String generatePistonName() {
	def apps = getChildApps()
	def i = 1
	while (true) {
		def name = "${handle()} Piston #$i"
		def found = false
		for (app in apps) {
			if (app.label == name) {
				found = true
				break
			}
		}
		if (found) {
			i++
			continue
		}
		return name
	}
}

private ping() {
	sendLocationEvent( [name: handle(), value: 'ping', isStateChange: true, displayed: false, linkText: "${handle()} ping reply", descriptionText: "${handle()} has received a ping reply and is replying with a pong", data: [id: hashId(app.id), name: app.label]] )
}

private getLogging() {
	def logging = settings.logging
	return [
		error: true,
		warn: true,
		info: (logging != 'None'),
		trace: (logging == 'Medium') || (logging == 'Full'),
		debug: (logging == 'Full')
	]
}

private boolean startDashboard() {
	//debug "startDashboard"
	def storageApp = getStorageApp()
	//if (!storageApp) return false
	def dashboardApp = getDashboardApp()
	if (!dashboardApp) return false
	def t0 = (storageApp ? storageApp.listAvailableDevices(true) : listAvailableDevices(true))
	dashboardApp.start(t0.collect{ it.value }, hashId(app.id))
	if (state.dashboard != 'active') atomicState.dashboard = 'active'
}

private boolean stopDashboard() {
	//debug "stopDashboard"
	def dashboardApp = getDashboardApp()
	if (!dashboardApp) return false
	dashboardApp.stop()
	if (state.dashboard != 'inactive') atomicState.dashboard = 'inactive'
}

private testIFTTT() {
	//setup our security descriptor
	state.modules = state.modules ?: [:]
	state.modules["IFTTT"] = [
		key: settings.iftttKey,
		connected: false
	]
	if (settings.iftttKey) {
		//verify the key
		return httpGet("https://maker.ifttt.com/trigger/test/with/key/" + settings.iftttKey) { response ->
			if (response.status == 200) {
				if (response.data == "Congratulations! You've fired the test event")
					state.modules["IFTTT"].connected = true
				return true;
			}
			return false;
		}
	}
	return false
}

/*
private testLifx() {
	def token = state.settings?.lifx_token
	if (!token) return false
	def requestParams = [
		uri:  "https://api.lifx.com",
		path: "/v1/scenes",
		headers: [
			"Authorization": "Bearer ${token}"
		],
		requestContentType: "application/json"
	]
	if (asynchttp_v1) asynchttp_v1.get(lifxHandler, requestParams, [request: 'scenes'])
	pause(250)
	requestParams.path = "/v1/lights/all"
	if (asynchttp_v1) asynchttp_v1.get(lifxHandler, requestParams, [request: 'lights'])
	return true
}
*/

private registerInstance() {
	def accountId = hashId(hubUID ?: app.getAccountId())
	def locationId = hashId(location.id + (isHubitat() ? '-L' : ''))
	def instanceId = hashId(app.id)
	def endpoint = state.endpoint
	def region = endpoint.contains('graph-eu') ? 'eu' : 'us';
	def name = handle() + ' Piston'
	def pistons = getChildApps().findAll{ it.name == name }.collect{ [ a: state[hashId(it.id, false)]?.a ] }
//log.debug "pistons: ${pistons}"
	List lpa = pistons.findAll{ it.a }.collect{ it.id }
	def pa = lpa.size()
	List lpd = pistons.findAll{ !it.a }.collect{ it.id }
	def pd = pistons.size() - pa
	
	def params = [
		uri: "https://api-${region}-${instanceId[32]}.webcore.co:9247",
		path: '/instance/register',
		headers: ['ST' : instanceId],
		body: [
			a: accountId,
			l: locationId,
			i: instanceId,
			e: endpoint,
			v: version(),
			r: region,
			pa: pa,
			lpa: lpa.join(','),
			pd: pd,
			lpd: lpd.join(',')
		]
	]
//log.debug "params ${params}"
/*
	if (asynchttp_v1) {
		asynchttp_v1.put(instanceRegistrationHandler, params)
	}
	else { */
		params << [contentType: 'application/json', requestContentType: 'application/json']
		asynchttpPut('myDone', params, [bbb:0])
/*
		try{
			httpPut(params) { res -> }	
		}
		catch(e) {}
*/
	//}
}

def myDone(resp, data) {
	log.debug "register resp: ${resp?.status}"
}

private initSunriseAndSunset() {
	def sunTimes = app.getSunriseAndSunset()
	if (!sunTimes.sunrise) {
		warn "Actual sunrise and sunset times are unavailable; please reset the location for your hub", rtData
		sunTimes.sunrise = new Date(getMidnightTime() + 7 * 3600000)
		sunTimes.sunset = new Date(getMidnightTime() + 19 * 3600000)
	}
	state.sunTimes = [
		sunrise: sunTimes.sunrise.time,
		sunset: sunTimes.sunset.time,
		updated: now()
	]
	return state.sunTimes
}

private getSunTimes() {
	def updated = state.sunTimes?.updated ?: 0
	//we require an update every 8 hours
	if (!updated || (now() - updated < 28800000)) return state.sunTimes
	return initSunriseAndSunset()
}

private getMidnightTime(rtData) {
	def rightNow = localTime()
	return localToUtcTime(rightNow - rightNow.mod(86400000))
}

/******************************************************************************/
/*** 																		***/
/*** PUBLIC METHODS															***/
/*** 																		***/
/******************************************************************************/
public Boolean isInstalled() {
	return !!state.installed
}

public String getDashboardUrl() {
	if (!state.endpoint) return null

	if(customEndpoints && (customWebcoreInstanceUrl ?: "") != ""){
		return customWebcoreInstanceUrl + "/"
	}
	else {
		return "https://dashboard.${domain()}/"
	}
}

public refreshDevices() {
	state.deviceVersion = now().toString()
	//testLifx()
}

public String getWikiUrl() {
	return "https://wiki.${domain()}/"
}
public String mem(showBytes = true) {
	def bytes = state.toString().length()
	return Math.round(100.00 * (bytes/ 100000.00)) + "%${showBytes ? " ($bytes bytes)" : ""}"
}

public Map getRunTimeData(semaphore = null, fetchWrappers = false) {
	def startTime = now()
	semaphore = semaphore ?: 0
	def semaphoreDelay = 0
	def semaphoreName = semaphore ? "sph$semaphore" : ''
	def waited = false
	if (semaphore) {		
		//if we need to wait for a semaphore, we do it here
		def lastSemaphore
		while (semaphore) {
			lastSemaphore = lastSemaphore ?: (atomicState[semaphoreName] ?: 0)
			if (!lastSemaphore || (now() - lastSemaphore > 100000)) {
				semaphoreDelay = waited ? now() - startTime : 0
				semaphore = now()
				atomicState[semaphoreName] = semaphore
				break
			}
			waited = true
			pause(250)
		}
	}
	def storageApp = !!fetchWrappers ? getStorageApp() : null
	return [
		enabled: !settings.disabled,
		//attributes: attributes(),
		semaphore: semaphore,
		semaphoreName: semaphoreName,
		semaphoreDelay: semaphoreDelay,
		commands: [
			//physical: commands(),
			//virtual: virtualCommands(),
			//overrides: commandOverrides()
		],
		//comparisons: comparisons(),
		coreVersion: version(),
		contacts: [:],
		devices: (!!fetchWrappers ? (storageApp ? storageApp.listAvailableDevices(true) : listAvailableDevices(true)) : [:]),
		//virtualDevices: virtualDevices(),
		globalVars: listAvailableVariables(),
		globalStore: state.store ?: [:],
		settings: state.settings ?: [:],
		lifx: state.lifx ?: [:],
		powerSource: state.powerSource ?: 'mains',
		region: state.endpoint.contains('graph-eu') ? 'eu' : 'us',
		instanceId: hashId(app.id),
		sunTimes: getSunTimes(),
		started: startTime,
		ended: now(),
		generatedIn: now() - startTime,
		redirectContactBook: settings.redirectContactBook,
		logPistonExecutions: settings.logPistonExecutions,
		useLocalFuelStreams : useLocalFuelStreams(),
		waitedAtSemaphore : waited
	] + (isHubitat() ? [	
		hsmStatus: location.hsmStatus //,
//		colors: getColors()
	] : [:])
}

public void updateRunTimeData(data) {
	if (!data || !data.id) return
	List variableEvents = []
	if (data && data.gvCache) {
		Map vars = atomicState.vars ?: [:]
		def modified = false
		for(var in data.gvCache) {
			if (var.key && var.key.startsWith('@') && (vars[var.key]) && (var.value.v != vars[var.key].v)) {
				variableEvents.push([name: var.key, oldValue: vars[var.key].v, value: var.value.v, type: var.value.t])
				vars[var.key].v = var.value.v
				modified = true
			}
		}
		if (modified) {
			atomicState.vars = vars
		}
	}
	if (data && data.gvStoreCache) {
		Map store = atomicState.store ?: [:]
		def modified = false
		for(var in data.gvStoreCache) {
			if (var.value == null) {
				store.remove(var.key)
			} else {
				store[var.key] = var.value
			}
			modified = true
		}
		if (modified) {
			atomicState.store = store
		}
	}
	def id = data.id
	//remove the old state as we don't need it
	def st = [:] + data.state
	st.remove('old')
	Map piston = [
		a: data.active,
		c: data.category,
		t: now(), //last run
		n: data.stats.nextSchedule,
		z: data.piston.z, //description
		s: st, //state
	]
	atomicState[id] = piston
	//broadcast variable change events
	for (variable in variableEvents) {
		sendVariableEvent(variable)
	}
	//release semaphores
	if (data.semaphoreName && (atomicState[data.semaphoreName] <= data.semaphore)) {
		//release the semaphore
		atomicState[data.semaphoreName] = 0
		//atomicState.remove(data.semaphoreName)
	}
	//broadcast to dashboard
	if (state.dashboard == 'active') {
		def dashboardApp = getDashboardApp()
		if (dashboardApp) dashboardApp.updatePiston(id, piston)
	}
	recoveryHandler()
}

public pausePiston(pistonId) {
	def piston = getChildApps().find{ hashId(it.id) == pistonId };
	if (piston) {
		def rtData = piston.pausePiston()
		updateRunTimeData(rtData)
	}
}

public resumePiston(pistonId) {
	def piston = getChildApps().find{ hashId(it.id) == pistonId };
	if (piston) {
		def rtData = piston.resume()
		updateRunTimeData(rtData)
	}
}

public executePiston(pistonId, data, source) {
	def piston = getChildApps().find{ hashId(it.id) == pistonId };
	if (piston) {
		piston.execute(data, source)
		return true
	}
	return false
}

private sendVariableEvent(variable) {
	sendLocationEvent([name: variable.name.startsWith('@@') ? '@@' + handle() : hashId(app.id), value: variable.name, isStateChange: true, displayed: false, linkText: "${handle()} global variable ${variable.name} changed", descriptionText: "${handle()} global variable ${variable.name} changed", data: [id: hashId(app.id), name: app.label, event: 'variable', variable: variable]])
}

private broadcastPistonList() {
	sendLocationEvent([name: handle(), value: 'pistonList', isStateChange: true, displayed: false, data: [id: hashId(app.id), name: app.label, pistons: getChildApps().findAll{ it.name == "${handle()} Piston" }.collect{[id: hashId(it.id), name: it.label]}]])
}

def webCoREHandler(event) {
	if (!event || (!event.name.endsWith(handle()))) return;
	def data = event.jsonData ?: null
	log.error "GOT EVENT WITH DATA $data"
	if (data && data.variable && (data.event == 'variable') && event.value && event.value.startsWith('@')) {
		Map vars = atomicState.vars ?: [:]
		Map variable = data.variable
		def oldVar = vars[variable.name] ?: [t:'', v:'']
		if ((oldVar.t != variable.type) || (oldVar.v != variable.value)) {
			vars[variable.name] = [t: variable.type ? variable.type : 'dynamic', v: variable.value]
			atomicState.vars = vars
		}
		return;
	}
	switch (event.value) {
		case 'poll':
			int delay = (int) Math.round(2000 * Math.random())
			pause(delay)
			broadcastPistonList()
			break;
/*		case 'ping':
		if (data && data.id && data.name && (data.id != hashId(app.id))) {
			sendLocationEvent( [name: handle(), value: 'pong', isStateChange: true, displayed: false, linkText: "${handle()} ping reply", descriptionText: "${handle()} has received a ping reply and is replying with a pong", data: [id: hashId(app.id), name: app.label]] )
		} else {
			break;
		}
			//fall through to pong
		case 'pong':
		/*if (data && data.id && data.name && (data.id != hashId(app.id))) {
			def pong = atomicState.pong ?: [:]
			pong[data.id] = data.name
		atomicState.pong = pong
			}*/
	}
}

def instanceRegistrationHandler(response, callbackData) {
}

/*
def askAlexaHandler(evt) {
	if (!evt) return
	switch (evt.value) {
		case "refresh":
			Map macros = [:]
			for(macro in (evt.jsonData && evt.jsonData?.macros ? evt.jsonData.macros : [])) {
				if (macro instanceof Map) {
					macros[hashId(macro.id)] = macro.name
				} else {
					macros[hashId(macro)] = macro;
				}
			}
			atomicState.askAlexaMacros = macros
			break
	}
}

def echoSistantHandler(evt) {
	if (!evt) return
	switch (evt.value) {
		case "refresh":
			Map profiles = [:]
			for(profile in (evt.jsonData && evt.jsonData?.profiles ? evt.jsonData.profiles : [])) {
				if (profile instanceof Map) {
					profiles[hashId(profile.id)] = profile.name
				} else {
					profiles[hashId(profile)] = profile;
				}
			}
			atomicState.echoSistantProfiles = profiles
			break
	}
}
*/

def hubUpdatedHandler(evt) {
	if (evt.jsonData && (evt.jsonData.hubType == 'PHYSICAL') && evt.jsonData.data && evt.jsonData.data.batteryInUse) {
		setPowerSource(evt.jsonData.data.batteryInUse ? 'battery' : 'mains')
	}
}

def summaryHandler(evt) {
	//log.error "$evt.name >>> ${evt.jsonData}"
}

def NewIncidentHandler(evt) {
	//log.error "$evt.name >>> ${evt.jsonData}"
}

def hsmHandler(evt){
	state.hsmStatus = evt.value
}

/*
def lifxHandler(response, cbkData) {
	if ((response.status == 200)) {
		def data = response.data instanceof List ? response.data : new groovy.json.JsonSlurper().parseText(response.data)
		cbkData = cbkData instanceof Map ? cbkData : (LinkedHashMap) new groovy.json.JsonSlurper().parseText(cbkData)
		if (data instanceof List) {
			state.lifx = state.lifx ?: [:]
			switch (cbkData.request) {
				case 'scenes':
					state.lifx.scenes = data.collectEntries{[(it.uuid): it.name]}
					break
				case 'lights':
					state.lifx.lights = data.collectEntries{[(it.id): it.label]}
					state.lifx.groups = data.collectEntries{[(it.group.id): it.group.name]}
					state.lifx.locations = data.collectEntries{[(it.location.id): it.location.name]}
					break
			}
		}
	}
}
*/


/******************************************************************************/
/***																		***/
/*** SECURITY METHODS														***/
/***																		***/
/******************************************************************************/
def String md5(String md5) {
    try {
	java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5")
	byte[] array = md.digest(md5.getBytes())
	def result = ""
	for (int i = 0; i < array.length; ++i) {
		result += Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3)
   	}
	return result
    } catch (java.security.NoSuchAlgorithmException e) {
    }
	return null;
}

def String hashId(id, updateCache = false) {
	//enabled hash caching for faster processing
	def result = state.hash ? state.hash[id] : null
	if (!result) {
		result = ":${md5("core." + id)}:"
		if (updateCache) {
			def hash = state.hash ?: [:]
			hash[id] = result
			state.hash = hash
		}
	}
	return result
}

def String temperatureUnit() {
	return "°" + location.temperatureScale;
}

/******************************************************************************/
/*** DEBUG FUNCTIONS														***/
/******************************************************************************/
private debug(message, shift = null, err = null, cmd = null) {
	if (cmd == "timer") {
		return [m: message, t: now(), s: shift, e: err]
	}
	if (message instanceof Map) {
		shift = message.s
		err = message.e
		message = message.m + " (${now() - message.t}ms)"
	}
	if (!settings.logging && (cmd != "error")) {
		return
	}
	cmd = cmd ? cmd : "debug"
	//mode is
	// 0 - initialize level, level set to 1
	// 1 - start of routine, level up
	// -1 - end of routine, level down
	// anything else - nothing happens
	def maxLevel = 4
	def level = state.debugLevel ? state.debugLevel : 0
	def levelDelta = 0
	def prefix = "║"
	def pad = "░"
	switch (shift) {
		case 0:
			level = 0
			prefix = ""
			break
		case 1:
			level += 1
			prefix = "╚"
			pad = "═"
			break
		case -1:
			levelDelta = -(level > 0 ? 1 : 0)
			pad = "═"
			prefix = "╔"
		break
	}

	if (level > 0) {
		prefix = prefix.padLeft(level, "║").padRight(maxLevel, pad)
	}

	level += levelDelta
	state.debugLevel = level

	if (debugging) {
		prefix += " "
	} else {
		prefix = ""
	}
	if (isHubitat()) {
		if(err){
			log."$cmd" "$prefix$message $err"
		} else {
			log."$cmd" "$prefix$message"
		}
	} else {
		log."$cmd" "$prefix$message", err
	}
}

private info  (message, shift=null, err=null)	{ debug message, shift, err, 'info' }
private trace (message, shift=null, err=null)	{ debug message, shift, err, 'trace' }
private warn  (message, shift=null, err=null)	{ debug message, shift, err, 'warn' }
private error (message, shift=null, err=null)	{ debug message, shift, err, 'error' }
private timer (message, shift=null, err=null)	{ debug message, shift, err, 'timer' }

private isCustomEndpoint(){
	customEndpoints && (customHubUrl ?: "") != ""
}

/******************************************************************************/
/*** DATABASE																***/
/******************************************************************************/

private Map capabilities() {
	//n = name
	//d = friendly devices name
	//a = default attribute
	//c = accepted commands
	//m = momentary
	//s = number of subdevices
	//i = subdevice index in event data
	def capabilities = [
		accelerationSensor		: [ n: "Acceleration Sensor",		d: "acceleration sensors",		a: "acceleration",								],
		actuator			: [ n: "Actuator", 			d: "actuators",														],
		alarm				: [ n: "Alarm",				d: "alarms and sirens",			a: "alarm",		c: ["off", "strobe", "siren", "both"],			],
		audioNotification		: [ n: "Audio Notification",		d: "audio notification devices",				c: ["playText", "playTextAndResume", "playTextAndRestore", "playTrack", "playTrackAndResume", "playTrackAndRestore"],			],
		battery				: [ n: "Battery",			d: "battery powered devices",		a: "battery",									],
		beacon				: [ n: "Beacon",			d: "beacons",				a: "presence",									],
		bulb				: [ n: "Bulb",				d: "bulbs",				a: "switch",		c: ["off", "on"],					],
//		button				: [ n: "Button",			d: "buttons",				a: "button",		m: true,	s: "numberOfButtons,numButtons", i: "buttonNumber",		],
		carbonDioxideMeasurement	: [ n: "Carbon Dioxide Measurement",	d: "carbon dioxide sensors",		a: "carbonDioxide",								],
		carbonMonoxideDetector		: [ n: "Carbon Monoxide Detector",	d: "carbon monoxide detectors",		a: "carbonMonoxide",								],
		colorControl			: [ n: "Color Control",			d: "adjustable color lights",		a: "color",		c: ["setColor", "setHue", "setSaturation"],		],
		colorTemperature		: [ n: "Color Temperature",		d: "adjustable white lights",		a: "colorTemperature",	c: ["setColorTemperature"],				],
		configuration			: [ n: "Configuration",			d: "configurable devices",					c: ["configure"],					],
		consumable			: [ n: "Consumable",			d: "consumables",			a: "consumableStatus",	c: ["setConsumableStatus"],				],
		contactSensor			: [ n: "Contact Sensor",		d: "contact sensors",			a: "contact",									],
		doorControl			: [ n: "Door Control",			d: "automatic doors",			a: "door",		c: ["close", "open"],					],
		energyMeter			: [ n: "Energy Meter",			d: "energy meters",			a: "energy",									],
		estimatedTimeOfArrival		: [ n: "Estimated Time of Arrival", 	d: "moving devices (ETA)",		a: "eta",									],
		garageDoorControl		: [ n: "Garage Door Control",		d: "automatic garage doors",		a: "door",		c: ["close", "open"],					],
		holdableButton			: [ n: "Holdable Button",		d: "holdable buttons",			a: "button",		m: true,	s: "numberOfButtons,numButtons", i: "buttonNumber",		],
		illuminanceMeasurement		: [ n: "Illuminance Measurement",	d: "illuminance sensors",		a: "illuminance",										],
		imageCapture			: [ n: "Image Capture",			d: "cameras, imaging devices",		a: "image",		c: ["take"],						],
		indicator			: [ n: "Indicator",			d: "indicator devices",			a: "indicatorStatus",	c: ["indicatorNever", "indicatorWhenOn", "indicatorWhenOff"],		],
		infraredLevel			: [ n: "Infrared Level",		d: "adjustable infrared lights",	a: "infraredLevel",	c: ["setInfraredLevel"],						],
		light				: [ n: "Light",				d: "lights",				a: "switch",		c: ["off", "on"],							],
		lock				: [ n: "Lock",				d: "electronic locks",			a: "lock",		c: ["lock", "unlock"],	s:"numberOfCodes,numCodes", i: "usedCode", 	],
		lockOnly			: [ n: "Lock Only",			d: "electronic locks (lock only)",	a: "lock",		c: ["lock"],								],
		mediaController			: [ n: "Media Controller",		d: "media controllers",			a: "currentActivity",	c: ["startActivity", "getAllActivities", "getCurrentActivity"],		],
		momentary			: [ n: "Momentary",			d: "momentary switches",					c: ["push"],								],
		motionSensor			: [ n: "Motion Sensor",			d: "motion sensors",			a: "motion",											],
		musicPlayer			: [ n: "Music Player",			d: "music players",			a: "status",		c: ["mute", "nextTrack", "pause", "play", "playTrack", "previousTrack", "restoreTrack", "resumeTrack", "setLevel", "setTrack", "stop", "unmute"],		],
		notification			: [ n: "Notification",			d: "notification devices",					c: ["deviceNotification"],						],
		outlet				: [ n: "Outlet",			d: "lights",				a: "switch",		c: ["off", "on"],							],
		pHMeasurement			: [ n: "pH Measurement",		d: "pH sensors",			a: "pH",											],
		polling				: [ n: "Polling",			d: "pollable devices",						c: ["poll"],								],
		powerMeter			: [ n: "Power Meter",			d: "power meters",			a: "power",											],
		powerSource			: [ n: "Power Source",			d: "multisource powered devices",	a: "powerSource",										],
		presenceSensor			: [ n: "Presence Sensor",		d: "presence sensors",			a: "presence",											],
		refresh				: [ n: "Refresh",			d: "refreshable devices",					c: ["refresh"],								],
		relativeHumidityMeasurement	: [ n: "Relative Humidity Measurement",	d: "humidity sensors",			a: "humidity",											],
		relaySwitch			: [ n: "Relay Switch",			d: "relay switches",			a: "switch",		c: ["off", "on"],							],
		sensor				: [ n: "Sensor",			d: "sensors",				a: "sensor",											],
		shockSensor			: [ n: "Shock Sensor",			d: "shock sensors",			a: "shock",											],
		signalStrength			: [ n: "Signal Strength",		d: "wireless devices",			a: "rssi",											],
		sleepSensor			: [ n: "Sleep Sensor",			d: "sleep sensors",			a: "sleeping",											],
		smokeDetector			: [ n: "Smoke Detector",		d: "smoke detectors",			a: "smoke",											],
		soundPressureLevel		: [ n: "Sound Pressure Level",		d: "sound pressure sensors",		a: "soundPressureLevel",									],
		soundSensor			: [ n: "Sound Sensor",			d: "sound sensors",			a: "sound",											],
		speechRecognition		: [ n: "Speech Recognition",		d: "speech recognition devices",	a: "phraseSpoken",				m: true,					],
		speechSynthesis			: [ n: "Speech Synthesis",		d: "speech synthesizers",					c: ["speak"],								],
		stepSensor			: [ n: "Step Sensor",			d: "step counters",			a: "steps",											],
		switch				: [ n: "Switch",			d: "switches",				a: "switch",		c: ["off", "on"],							],
		switchLevel			: [ n: "Switch Level",			d: "dimmers and dimmable lights",	a: "level",		c: ["setLevel"],							],
		tamperAlert			: [ n: "Tamper Alert",			d: "tamper sensors",			a: "tamper",											],
		temperatureMeasurement		: [ n: "Temperature Measurement",	d: "temperature sensors",		a: "temperature",										],
		thermostat			: [ n: "Thermostat",			d: "thermostats",			a: "thermostatMode",	c: ["auto", "cool", "eco", "emergencyHeat", "fanAuto", "fanCirculate", "fanOn", "heat", "off", "setCoolingSetpoint", "setHeatingSetpoint", "setSchedule", "setThermostatFanMode", "setThermostatMode"],	],
		thermostatCoolingSetpoint	: [ n: "Thermostat Cooling Setpoint",	d: "thermostats (cooling)",		a: "coolingSetpoint",	c: ["setCoolingSetpoint"],						],
		thermostatFanMode		: [ n: "Thermostat Fan Mode",		d: "fans",				a: "thermostatFanMode",	c: ["fanAuto", "fanCirculate", "fanOn", "setThermostatFanMode"],	],
		thermostatHeatingSetpoint	: [ n: "Thermostat Heating Setpoint",	d: "thermostats (heating)",		a: "heatingSetpoint",	c: ["setHeatingSetpoint"],						],
		thermostatMode			: [ n: "Thermostat Mode",							a: "thermostatMode",	c: ["auto", "cool", "eco", "emergencyHeat", "heat", "off", "setThermostatMode"],	],
		thermostatOperatingState	: [ n: "Thermostat Operating State",						a: "thermostatOperatingState",									],
		thermostatSetpoint		: [ n: "Thermostat Setpoint",							a: "thermostatSetpoint",									],
		threeAxis			: [ n: "Three Axis Sensor",		d: "three axis sensors",		a: "orientation",										],
		timedSession			: [ n: "Timed Session",			d: "timers",				a: "sessionStatus",	c: ["cancel", "pause", "setTimeRemaining", "start", "stop", ],		],
		tone				: [ n: "Tone",				d: "tone generators",						c: ["beep"],								],
		touchSensor			: [ n: "Touch Sensor",			d: "touch sensors",			a: "touch",											],
		ultravioletIndex		: [ n: "Ultraviolet Index",		d: "ultraviolet sensors",		a: "ultravioletIndex",										],
		valve				: [ n: "Valve",				d: "valves",				a: "valve",		c: ["close", "open"],							],
		voltageMeasurement		: [ n: "Voltage Measurement",		d: "voltmeters",			a: "voltage",											],
		waterSensor			: [ n: "Water Sensor",			d: "water and leak sensors",		a: "water",											],
		windowShade			: [ n: "Window Shade",			d: "automatic window shades",		a: "windowShade",	c: ["close", "open", "presetPosition"],					],
	//]  + (isHubitat() ? [
		doubleTapableButton		: [ n: "Double Tapable Button",		d: "double tapable buttons",		a: "doubleTapped",	c: ["doubleTap"],							],
		holdableButton			: [ n: "Holdable Button",		d: "holdable buttons",			a: "held",		c: ["hold"]								],
		momentary			: [ n: "Momentary",			d: "momentary switches",					c: ["pushMomentary"],							],
		pushableButton			: [ n: "Pushable Button",		d: "pushable buttons",			a: "pushed",		c: ["push"],								]
	
	]// : [:])
	
/*
	if(isHubitat()){
	 	capabilities.remove('button')
	}
*/
	
	return capabilities
}

private Map attributes() {
	def attrs = [
		acceleration			: [ n: "acceleration",			t: "enum",		o: ["active", "inactive"],						],
		activities			: [ n: "activities", 			t: "object",											],
		alarm				: [ n: "alarm", 			t: "enum",		o: ["both", "off", "siren", "strobe"],					],
		axisX				: [ n: "X axis",			t: "integer",	r: [-1024, 1024],	s: "threeAxis",						],
		axisY				: [ n: "Y axis",			t: "integer",	r: [-1024, 1024],	s: "threeAxis",						],
		axisZ				: [ n: "Z axis",			t: "integer",	r: [-1024, 1024],	s: "threeAxis",						],
		battery				: [ n: "battery", 			t: "integer",	r: [0, 100],		u: "%",							],
//		button				: [ n: "button", 			t: "enum",		o: ["pushed", "held"],									c: "button",					m: true, s: "numberOfButtons,numButtons", i: "buttonNumber"		],
		carbonDioxide			: [ n: "carbon dioxide",		t: "decimal",	r: [0, null],									],
		carbonMonoxide			: [ n: "carbon monoxide",		t: "enum",		o: ["clear", "detected", "tested"],					],
		color				: [ n: "color",				t: "color",											],
		colorTemperature		: [ n: "color temperature",		t: "integer",	r: [1000, 30000],	u: "°K",						],
		consumableStatus		: [ n: "consumable status",		t: "enum",		o: ["good", "maintenance_required", "missing", "order", "replace"],	],
		contact				: [ n: "contact",			t: "enum",		o: ["closed", "open"],							],
		coolingSetpoint			: [ n: "cooling setpoint",		t: "decimal",	r: [-127, 127],		u: '°?',						],
		currentActivity			: [ n: "current activity",		t: "string",											],
		door				: [ n: "door",				t: "enum",		o: ["closed", "closing", "open", "opening", "unknown"],					p: true,					],
		energy				: [ n: "energy",			t: "decimal",	r: [0, null],		u: "kWh",						],
		eta				: [ n: "ETA",				t: "datetime",											],
		goal				: [ n: "goal",				t: "integer",	r: [0, null],									],
		heatingSetpoint			: [ n: "heating setpoint",		t: "decimal",	r: [-127, 127],		u: '°?',						],
		hex				: [ n: "hexadecimal code",		t: "hexcolor",											],
//		holdableButton			: [ n: "holdable button",		t: "enum",		o: ["held", "pushed"],								c: "holdableButton",			m: true,		],
		hue				: [ n: "hue",				t: "integer",	r: [0, 360],		u: "°",							],
		humidity			: [ n: "relative humidity",		t: "integer",	r: [0, 100],		u: "%",							],
		illuminance			: [ n: "illuminance",			t: "integer",	r: [0, null],		u: "lux",						],
		image				: [ n: "image",				t: "image",											],
		indicatorStatus			: [ n: "indicator status",		t: "enum",		o: ["never", "when off", "when on"],					],
		infraredLevel			: [ n: "infrared level",		t: "integer",	r: [0, 100],		u: "%",							],
		level				: [ n: "level",				t: "integer",	r: [0, 100],		u: "%",							],
		lock				: [ n: "lock",				t: "enum",		o: ["locked", "unknown", "unlocked", "unlocked with timeout"],	c: "lock",			s:"numberOfCodes,numCodes", i:"usedCode", sd: "user code"		],
		lqi				: [ n: "link quality",			t: "integer",	r: [0, 255],									],
		motion				: [ n: "motion",			t: "enum",		o: ["active", "inactive"],						],
		mute				: [ n: "mute",				t: "enum",		o: ["muted", "unmuted"],						],
		orientation			: [ n: "orientation",			t: "enum",		o: ["rear side up", "down side up", "left side up", "front side up", "up side up", "right side up"],	],
		axisX				: [ n: "axis X",			t: "decimal",	],
		axisY				: [ n: "axis Y",			t: "decimal",	],
		axisZ				: [ n: "axis Z",			t: "decimal",	],
		pH				: [ n: "pH level",			t: "decimal",	r: [0, 14],									],
		phraseSpoken			: [ n: "phrase",			t: "string",											],
		power				: [ n: "power",				t: "decimal",		u: "W",									],
		powerSource			: [ n: "power source",			t: "enum",		o: ["battery", "dc", "mains", "unknown"],				],
		presence			: [ n: "presence",			t: "enum",		o: ["not present", "present"],						],
		rssi				: [ n: "signal strength",		t: "integer",	r: [0, 100],		u: "%",							],
		saturation			: [ n: "saturation",			t: "integer",	r: [0, 100],		u: "%",							],
		schedule			: [ n: "schedule",			t: "object",											],
		sessionStatus			: [ n: "session status",		t: "enum",		o: ["canceled", "paused", "running", "stopped"],			],
		shock				: [ n: "shock",				t: "enum",		o: ["clear", "detected"],						],
		sleeping			: [ n: "sleeping",			t: "enum",		o: ["not sleeping", "sleeping"],					],
		smoke				: [ n: "smoke",				t: "enum",		o: ["clear", "detected", "tested"],					],
		sound				: [ n: "sound",				t: "enum",		o: ["detected", "not detected"],					],
		soundPressureLevel		: [ n: "sound pressure level",		t: "integer",	r: [0, null],		u: "dB",						],
		status				: [ n: "status",			t: "string",											],
		steps				: [ n: "steps",				t: "integer",	r: [0, null],									],
		switch				: [ n: "switch",			t: "enum",		o: ["off", "on"],		p: true,					],
		tamper				: [ n: "tamper",			t: "enum",		o: ["clear", "detected"],						],
		temperature			: [ n: "temperature",			t: "decimal",	r: [-460, 10000],	u: '°?',						],
		thermostatFanMode		: [ n: "fan mode",			t: "enum",		o: ["auto", "circulate", "on"],						],
		thermostatMode			: [ n: "thermostat mode",		t: "enum",		o: ["auto", "cool", "eco", "emergency heat", "heat", "off"],		],
		thermostatOperatingState	: [ n: "operating state",		t: "enum",		o: ["cooling", "fan only", "heating", "idle", "pending cool", "pending heat", "vent economizer"],	],
		thermostatSetpoint		: [ n: "setpoint",			t: "decimal",	r: [-127, 127],		u: '°?',							],
		threeAxis			: [ n: "vector",			t: "vector3",											],
		timeRemaining			: [ n: "time remaining",		t: "integer",	r: [0, null],		u: "s",							],
		touch				: [ n: "touch",				t: "enum",		o: ["touched"],								],
		trackData			: [ n: "track data",			t: "object",											],
		trackDescription		: [ n: "track description",		t: "string",											],
		ultravioletIndex		: [ n: "UV index",			t: "integer",	r: [0, null],									],
		valve				: [ n: "valve",				t: "enum",		o: ["closed", "open"],							],
		voltage				: [ n: "voltage",			t: "decimal",	r: [null, null],	u: "V",							],
		water				: [ n: "water",				t: "enum",		o: ["dry", "wet"],							],
		windowShade			: [ n: "window shade",			t: "enum",		o: ["closed", "closing", "open", "opening", "partially open", "unknown"],	],
	//webCoRE Presence Sensor
		altitude			: [ n: "altitude",			t: "decimal",	r: [null, null],	u: "ft",						],
		altitudeMetric			: [ n: "altitude (metric)",		t: "decimal",	r: [null, null],	u: "m",							],
		floor				: [ n: "floor",				t: "integer",	r: [null, null],								],
		distance			: [ n: "distance",			t: "decimal",	r: [null, null],	u: "mi",						],
		distanceMetric			: [ n: "distance (metric)",		t: "decimal",	r: [null, null],	u: "km",						],
		currentPlace			: [ n: "current place",			t: "string",											],
		previousPlace			: [ n: "previous place",		t: "string",											],
		closestPlace			: [ n: "closest place",			t: "string",											],
		arrivingAtPlace			: [ n: "arriving at place",		t: "string",											],
		leavingPlace			: [ n: "leaving place",			t: "string",											],
		places				: [ n: "places",			t: "string",											],
		horizontalAccuracyMetric	: [ n: "horizontal accuracy (metric)",	t: "decimal",	r: [null, null],	u: "m",							],
		horizontalAccuracy		: [ n: "horizontal accuracy",		t: "decimal",	r: [null, null],	u: "ft",						],
		verticalAccuracy		: [ n: "vertical accuracy",		t: "decimal",	r: [null, null],	u: "ft",						],
		verticalAccuracyMetric		: [ n: "vertical accuracy (metric)",	t: "decimal",	r: [null, null],	u: "m",							],
		latitude			: [ n: "latitude",			t: "decimal",	r: [null, null],	u: "°",							],
		longitude			: [ n: "longitude",			t: "decimal",	r: [null, null],	u: "°",							],
		closestPlaceDistance		: [ n: "distance to closest place",	t: "decimal",	r: [null, null],	u: "mi",						],
		closestPlaceDistanceMetric	: [ n: "distance to closest place (metric)",t: "decimal",	r: [null, null],	u: "km",					],
		speed				: [ n: "speed",				t: "decimal",	r: [null, null],	u: "ft/s",						],
		speedMetric			: [ n: "speed (metric)",		t: "decimal",	r: [null, null],	u: "m/s",						],
		bearing				: [ n: "bearing",			t: "decimal",	r: [0, 360],		u: "°",							],
	//]  + (isHubitat() ? [
		doubleTapped			: [ n: "double tapped button", 	t: "integer",	c: "doubleTapableButton"								],
		held				: [ n: "held button", 			t: "integer",	c: "holdableButton"								],
		pushed				: [ n: "pushed button", 		t: "integer",	c: "pushableButton"								]
	]// : [:])
	
/*
	if(isHubitat()){
		attrs.remove('button')
		attrs.remove('holdableButton')
	}
*/
	
	return attrs
}

/* Push command has multiple overloads in hubitat */
public Map commandOverrides(){
	return (isHubitat() ? [ //s: command signature
	 	push	: [c: "push",	s: null , r: "pushMomentary"],
		flash	: [c: "flash",	s: null , r: "flashNative"] //flash native command conflicts with flash emulated command. Also needs "o" option on command described later
	] : [:])
}

private Map commands() {
	return [
		auto				: [ n: "Set to Auto",				a: "thermostatMode",				v: "auto",						],
		beep				: [ n: "Beep",																		],
		both				: [ n: "Strobe and Siren",			a: "alarm",					v: "both",						],
		cancel				: [ n: "Cancel",																	],
		close				: [ n: "Close",					a: "door",					v: "close",						],
		configure			: [ n: "Configure",		i: 'cog',														],
		cool				: [ n: "Set to Cool",		i: 'snowflake', is: 'l',	a: "thermostatMode",		v: "cool",						],
		deviceNotification		: [ n: "Send device notification...",	d: "Send device notification \"{0}\"",			p: [[n:"Message",t:"string"]],  			],
		eco				: [ n: "Set to Eco",		i: 'leaf', 	a: "thermostatMode",				v: "eco",						],
		emergencyHeat			: [ n: "Set to Emergency Heat",															a: "thermostatMode",				v: "emergency heat",																																	],
		fanAuto				: [ n: "Set fan to Auto",																a: "thermostatFanMode",				v: "auto",																																			],
		fanCirculate			: [ n: "Set fan to Circulate",															a: "thermostatFanMode",				v: "circulate",																																		],
		fanOn				: [ n: "Set fan to On",																	a: "thermostatFanMode",				v: "on",																																			],
		getAllActivities		: [ n: "Get all activities",																																																													],
		getCurrentActivity		: [ n: "Get current activity",																																																													],
		heat				: [ n: "Set to Heat",					i: 'fire',										a: "thermostatMode",				v: "heat",																																			],
		indicatorNever			: [ n: "Disable indicator",																																																														],
		indicatorWhenOff		: [ n: "Enable indicator when off",																																																												],
		indicatorWhenOn			: [ n: "Enable indicator when on",																																																												],
		lock				: [ n: "Lock",							i: "lock",										a: "lock",							v: "locked",																																		],
		mute				: [ n: "Mute",							i: 'volume-off',								a: "mute",							v: "muted",																																			],
		nextTrack			: [ n: "Next track",																																																															],
		off				: [ n: "Turn off",						i: 'circle-notch',							a: "switch",						v: "off",																																			],
		on				: [ n: "Turn on",						i: "power-off",									a: "switch",						v: "on",																																			],
		open				: [ n: "Open",																			a: "door",							v: "open",																																			],
		pause				: [ n: "Pause",																																																																	],
		play				: [ n: "Play",																																																																	],
		playText			: [ n: "Speak text...",					d: "Speak text \"{0}\"",																					p: [[n:"Text",t:"string"], [n:"Volume", t:"level", d:" at volume {v}"]],  													],
		playTextAndRestore		: [ n: "Speak text and restore...",		d: "Speak text \"{0}\" and restore",																		p: [[n:"Text",t:"string"], [n:"Volume", t:"level", d:" at volume {v}"]],  													],
		playTextAndResume		: [ n: "Speak text and resume...",		d: "Speak text \"{0}\" and resume",																		p: [[n:"Text",t:"string"], [n:"Volume", t:"level", d:" at volume {v}"]],  													],
		playTrack			: [ n: "Play track...",					d: "Play track {0}{1}",																			p: [[n:"Track URL",t:"uri"], [n:"Volume", t:"level", d:" at volume {v}"]],  												],
		playTrackAndRestore		: [ n: "Play track and restore...",		d: "Play track {0}{1} and restore",																p: [[n:"Track URL",t:"uri"], [n:"Volume", t:"level", d:" at volume {v}"]],  												],
		playTrackAndResume		: [ n: "Play track and resume...",		d: "Play track {0}{1} and resume",																p: [[n:"Track URL",t:"uri"], [n:"Volume", t:"level", d:" at volume {v}"]],  												],
		poll				: [ n: "Poll",						i: 'question',																																																											],
		presetPosition			: [ n: "Move to preset position",														a: "windowShade",					v: "partially open",																																],
		previousTrack			: [ n: "Previous track",																																																														],
		push				: [ n: "Push",																																																																	],
		refresh				: [ n: "Refresh",					i: 'sync',																																																											],
		restoreTrack			: [ n: "Restore track...",				d: "Restore track <uri>{0}</uri>",																			p: [[n:"Track URL",t:"url"]],  																								],
		resumeTrack			: [ n: "Resume track...",				d: "Resume track <uri>{0}</uri>",																			p: [[n:"Track URL",t:"url"]],  																								],
		setColor			: [ n: "Set color...",				i: 'palette', is: "l",	d: "Set color to {0}{1}",						a: "color",													p: [[n:"Color",t:"color"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],  							],
		setColorTemperature		: [ n: "Set color temperature...",		d: "Set color temperature to {0}°K{1}",			a: "colorTemperature",										p: [[n:"Color Temperature", t:"colorTemperature"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],	],
		setConsumableStatus		: [ n: "Set consumable status...",		d: "Set consumable status to {0}",																			p: [[n:"Status", t:"consumable"]],																							],
		setCoolingSetpoint		: [ n: "Set cooling point...",			d: "Set cooling point at {0}{T}",				a: "thermostatCoolingSetpoint",								p: [[n:"Desired temperature", t:"thermostatSetpoint"]], 																	],
		setHeatingSetpoint		: [ n: "Set heating point...",			d: "Set heating point at {0}{T}",				a: "thermostatHeatingSetpoint",								p: [[n:"Desired temperature", t:"thermostatSetpoint"]], 																	],
		setHue				: [ n: "Set hue...",				i: 'palette', is: "l",	d: "Set hue to {0}°{1}",			a: "hue",													p: [[n:"Hue", t:"hue"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]], 								],
		setInfraredLevel		: [ n: "Set infrared level...",		i: 'signal',	d: "Set infrared level to {0}%{1}",	a: "infraredLevel",											p: [[n:"Level",t:"infraredLevel"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]], 					],
		setLevel				: [ n: "Set level...",				i: 'signal',	d: "Set level to {0}%{1}",			a: "level",													p: [[n:"Level",t:"level"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]], 							],
		setSaturation			: [ n: "Set saturation...",				d: "Set saturation to {0}{1}",					a: "saturation",											p: [[n:"Saturation", t:"saturation"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],					],
		setSchedule			: [ n: "Set thermostat schedule...",	d: "Set schedule to {0}",						a: "schedule",												p: [[n:"Schedule", t:"object"]],																							],
		setThermostatFanMode		: [ n: "Set fan mode...",				d: "Set fan mode to {0}",						a: "thermostatFanMode",										p: [[n:"Fan mode", t:"thermostatFanMode"]],																					],
		setThermostatMode		: [ n: "Set thermostat mode...",		d: "Set thermostat mode to {0}",				a: "thermostatMode",										p: [[n:"Thermostat mode",t:"thermostatMode"]],																				],
		setTimeRemaining		: [ n: "Set remaining time...",			d: "Set remaining time to {0}s",				a: "timeRemaining",											p: [[n:"Remaining time [seconds]", t:"number"]],																						],
		setTrack			: [ n: "Set track...",					d: "Set track to <uri>{0}</uri>",																			p: [[n:"Track URL",t:"url"]], 																								],
		siren				: [ n: "Siren",																			a: "alarm",							v: "siren",																																			],
		speak				: [ n: "Speak...",						d: "Speak \"{0}\"",																							p: [[n:"Message", t:"string"]],																								],
		start				: [ n: "Start",																																																																	],
		startActivity			: [ n: "Start activity...",				d: "Start activity \"{0}\"",																				p: [[n:"Activity", t:"string"]],																							],
		stop				: [ n: "Stop",																																																																	],
		strobe				: [ n: "Strobe",																		a: "alarm",							v: "strobe",																																		],
		take				: [ n: "Take a picture",																																																														],
		unlock				: [ n: "Unlock",					i: 'unlock-alt',									a: "lock",							v: "unlocked",																																		],
		unmute				: [ n: "Unmute",					i: 'volume-up',										a: "mute",							v: "unmuted",																																		],
		/* predfined commands below */
		//general
		quickSetCool			: [ n: "Quick set cooling point...",	d: "Set quick cooling point at {0}{T}",				p: [[n:"Desired temperature",t:"thermostatSetpoint"]],		],
		quickSetHeat			: [ n: "Quick set heating point...",	d: "Set quick heating point at {0}{T}",				p: [[n:"Desired temperature",t:"thermostatSetpoint"]],		],
		toggle				: [ n: "Toggle",																																																																],
		reset				: [ n: "Reset",																																																																	],
		//hue
		startLoop			: [ n: "Start color loop",																																																														],
		stopLoop			: [ n: "Stop color loop",																																																														],
		setLoopTime			: [ n: "Set loop duration...",			d: "Set loop duration to {0}",																				p: [[n:"Duration", t:"duration"]]																							],
		setDirection			: [ n: "Switch loop direction",																																																													],
		alert				: [ n: "Alert with lights...",			d: "Alert \"{0}\" with lights",																				p: [[n:"Alert type", t:"enum", o:["Blink","Breathe","Okay","Stop"]]], 														],
		setAdjustedColor		: [ n: "Transition to color...",		d: "Transition to color {0} in {1}{2}",																		p: [[n:"Color", t:"color"], [n:"Duration",t:"duration"],[n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																	],
		setAdjustedHSLColor		: [ n: "Transition to HSL color...",		d: "Transition to color H:{0}° / S:{1}% / L:{2}% in {3}{4}",												p: [[n:"Hue", t:"hue"],[n:"Saturation", t:"saturation"],[n:"Level", t:"level"],[n:"Duration",t:"duration"],[n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																	],
		//harmony
		allOn				: [ n: "Turn all on",																																																															],
		allOff				: [ n: "Turn all off",																																																															],
		hubOn				: [ n: "Turn hub on",																																																															],
		hubOff				: [ n: "Turn hub off",																																																															],
		//blink camera
		enableCamera			: [ n: "Enable camera",																																																															],
		disableCamera			: [ n: "Disable camera",																																																														],
		monitorOn			: [ n: "Turn monitor on",																																																														],
		monitorOff			: [ n: "Turn monitor off",																																																														],
		ledOn				: [ n: "Turn LED on",																																																															],
		ledOff				: [ n: "Turn LED off",																																																															],
		ledAuto				: [ n: "Set LED to Auto",																																																														],
		setVideoLength			: [ n: "Set video length...",			d: "Set video length to {0}", 																				p: [[n:"Duration", t:"duration"]], 																							],
		//dlink camera
		pirOn				: [ n: "Enable PIR motion detection",																																																											],
		pirOff				: [ n: "Disable PIR motion detection",																																																											],
		nvOn				: [ n: "Set Night Vision to On",																																																												],
		nvOff				: [ n: "Set Night Vision to Off",																																																												],
		nvAuto				: [ n: "Set Night Vision to Auto",																																																												],
		vrOn				: [ n: "Enable local video recording",																																																											],
		vrOff				: [ n: "Disable local video recording",																																																											],
		left				: [ n: "Pan camera left",																																																														],
		right				: [ n: "Pan camera right",																																																														],
		up				: [ n: "Pan camera up",																																																															],
		down				: [ n: "Pan camera down",																																																														],
		home				: [ n: "Pan camera to the Home",																																																												],
		presetOne			: [ n: "Pan camera to preset #1",																																																												],
		presetTwo			: [ n: "Pan camera to preset #2",																																																												],
		presetThree			: [ n: "Pan camera to preset #3",																																																												],
		presetFour			: [ n: "Pan camera to preset #4",																																																												],
		presetFive			: [ n: "Pan camera to preset #5",																																																												],
		presetSix			: [ n: "Pan camera to preset #6",																																																												],
		presetSeven			: [ n: "Pan camera to preset #7",																																																												],
		presetEight			: [ n: "Pan camera to preset #8",																																																												],
		presetCommand			: [ n: "Pan camera to preset...",		d: "Pan camera to preset #{0}",																				p: [[n:"Preset #", t:"integer",r:[1,99]]], 																					],
		//zwave fan speed control by @pmjoen
		low				: [ n: "Set to Low",																																																															],
		med				: [ n: "Set to Medium",																																																															],
		high				: [ n: "Set to High",																																																															],
//	] + (isHubitat() ? [
		doubleTap			: [ n: "Double Tap",					d: "Double tap button {0}",						a: "doubleTapped",										p:[[n: "Button #", t: "integer"]]																																																										],
		flashNative			: [ n: "Flash",																		   																																															],
		hold				: [ n: "Hold",							d: "Hold Button {0}",							a: "held",													p: [[n:"Button #", t: "integer"]]																																						],
		push				: [ n: "Push",							d: "Push button {0}",							a: "pushed",												p:[[n: "Button #", t: "integer"]]																																																										],
		pushMomentary			: [ n: "Push"																																																																						]
	]// : [:])
}

private Map virtualCommands() {
	//a = aggregate
	//d = display
	//n = name
	//t = type
	List tileIndexes = ['1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16']
	def commands = [
		noop					: [	n: "No operation",				a: true,	i: "circle",				d: "No operation",																										],
		wait					: [	n: "Wait...", 					a: true,	i: "clock", is: "r",				d: "Wait {0}",															p: [[n:"Duration", t:"duration"]],				],
		waitRandom				: [ n: "Wait randomly...",			a: true,	i: "clock", is: "r",				d: "Wait randomly between {0} and {1}",									p: [[n:"At least", t:"duration"],[n:"At most", t:"duration"]],	],
		waitForTime				: [ n: "Wait for time...",			a: true,	i: "clock", is: "r",				d: "Wait until {0}",													p: [[n:"Time", t:"time"]],	],
		waitForDateTime				: [ n: "Wait for date & time...",	a: true,	i: "clock", is: "r",				d: "Wait until {0}",													p: [[n:"Date & Time", t:"datetime"]],	],
		executePiston				: [ n: "Execute piston...",			a: true,	i: "clock", is: "r",				d: "Execute piston \"{0}\"{1}",											p: [[n:"Piston", t:"piston"], [n:"Arguments", t:"variables", d:" with arguments {v}"],[n:"Wait for execution",t:"boolean",d:" and wait for execution to finish",w:"webCoRE can only wait on piston executions of pistons within the same instance as the caller. Please note that global variables updated in the callee piston do NOT get reflected immediately in the caller piston, the new values will be available on the next run."]],	],
		pausePiston				: [ n: "Pause piston...",			a: true,	i: "clock", is: "r",				d: "Pause piston \"{0}\"",												p: [[n:"Piston", t:"piston"]],	],
		resumePiston				: [ n: "Resume piston...",			a: true,	i: "clock", is: "r",				d: "Resume piston \"{0}\"",												p: [[n:"Piston", t:"piston"]],	],
//		executeRoutine				: [ n: "Execute routine...",		a: true,	i: "clock", is: "r",				d: "Execute routine \"{0}\"",											p: [[n:"Routine", t:"routine"]],	],
		toggle					: [ n: "Toggle", r: ["on", "off"], 				i: "toggle-on"																				],
		toggleRandom				: [ n: "Random toggle", r: ["on", "off"], 		i: "toggle-on",				d: "Random toggle{0}",													p: [[n:"Probability for on", t:"level", d:" with a {v}% probability for on"]],	],
		setSwitch				: [ n: "Set switch...", r: ["on", "off"], 			i: "toggle-on",			d: "Set switch to {0}",													p: [[n:"Switch value", t:"switch"]],																],
		setHSLColor				: [ n: "Set color... (hsl)", 					i: "palette", is: "l",				d: "Set color to H:{0}° / S:{1}% / L%:{2}{3}",				r: ["setColor"],				p: [[n:"Hue",t:"hue"], [n:"Saturation",t:"saturation"], [n:"Level",t:"level"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],  							],
		toggleLevel				: [ n: "Toggle level...", 						i: "toggle-off",			d: "Toggle level between 0% and {0}%",	r: ["on", "off", "setLevel"],	p: [[n:"Level", t:"level"]],																																	],
		sendNotification			: [ n: "Send notification...",		a: true,	i: "comment-alt", is: "r",			d: "Send notification \"{0}\"",											p: [[n:"Message", t:"string"]],												],
		sendPushNotification		: [ n: "Send PUSH notification...",	a: true,	i: "comment-alt", is: "r",			d: "Send PUSH notification \"{0}\"{1}",									p: [[n:"Message", t:"string"],[n:"Store in Messages", t:"boolean", d:" and store in Messages", s:1]],	],
		sendSMSNotification			: [ n: "Send SMS notification...",	a: true,	i: "comment-alt", is: "r",			d: "Send SMS notification \"{0}\" to {1}{2}",							p: [[n:"Message", t:"string"],[n:"Phone number",t:"phone"],[n:"Store in Messages", t:"boolean", d:" and store in Messages", s:1]],	],
//		sendNotificationToContacts	: [ n: "Send notification to contacts...",a: true,i: "comment-alt", is: "r",		d: "Send notification \"{0}\" to {1}{2}",								p: [[n:"Message", t:"string"],[n:"Contacts",t:"contacts"],[n:"Store in Messages", t:"boolean", d:" and store in Messages", s:1]],	],
		log					: [ n: "Log to console...",			a: true,	i: "bug",					d: "Log {0} \"{1}\"{2}",												p: [[n:"Log type", t:"enum", o:["info","trace","debug","warn","error"]],[n:"Message",t:"string"],[n:"Store in Messages", t:"boolean", d:" and store in Messages", s:1]],	],
		httpRequest				: [ n: "Make a web request",		a: true, 	i: "anchor", is: "r",				d: "Make a {1} request to {0}",					p: [[n:"URL", t:"uri"],[n:"Method", t:"enum", o:["GET","POST","PUT","DELETE","HEAD"]],[n:"Request body type", t:"enum", o:["JSON","FORM","CUSTOM"]],[n:"Send variables", t:"variables", d:"data {v}"],[n:"Request body", t:"string", d:"data {v}"],[n:"Request content type", t:"enum", o:["text/plain","text/html","application/json","application/x-www-form-urlencoded","application/xml"]],[n:"Authorization header", t:"string", d:"{v}"]],	],
		setVariable				: [ n: "Set variable...",			a: true,	i: "superscript", is:"r",			d: "Set variable {0} = {1}",											p: [[n:"Variable",t:"variable"],[n:"Value", t:"dynamic"]],	],
		setState				: [ n: "Set piston state...",		a: true,	i: "align-left", is:"l",			d: "Set piston state to \"{0}\"",										p: [[n:"State",t:"string"]],	],
		setTileColor				: [ n: "Set piston tile colors...",	a: true,	i: "info-square", is:"l",			d: "Set piston tile #{0} colors to {1} over {2}{3}",					p: [[n:"Tile Index",t:"enum",o:tileIndexes],[n:"Text Color",t:"color"],[n:"Background Color",t:"color"],[n:"Flash mode",t:"boolean",d:" (flashing)"]],	],
		setTileTitle				: [ n: "Set piston tile title...",	a: true,	i: "info-square", is:"l",			d: "Set piston tile #{0} title to \"{1}\"",								p: [[n:"Tile Index",t:"enum",o:tileIndexes],[n:"Title",t:"string"]],	],
		setTileText				: [ n: "Set piston tile text...",	a: true,	i: "info-square", is:"l",			d: "Set piston tile #{0} text to \"{1}\"",								p: [[n:"Tile Index",t:"enum",o:tileIndexes],[n:"Text",t:"string"]],	],
		setTileFooter				: [ n: "Set piston tile footer...",	a: true,	i: "info-square", is:"l",			d: "Set piston tile #{0} footer to \"{1}\"",							p: [[n:"Tile Index",t:"enum",o:tileIndexes],[n:"Footer",t:"string"]],	],
		setTile					: [ n: "Set piston tile...",		a: true,	i: "info-square", is:"l",			d: "Set piston tile #{0} title  to \"{1}\", text to \"{2}\", footer to \"{3}\", and colors to {4} over {5}{6}",		p: [[n:"Tile Index",t:"enum",o:tileIndexes],[n:"Title",t:"string"],[n:"Text",t:"string"],[n:"Footer",t:"string"],[n:"Text Color",t:"color"],[n:"Background Color",t:"color"],[n:"Flash mode",t:"boolean",d:" (flashing)"]],	],
		clearTile				: [ n: "Clear piston tile...",		a: true,	i: "info-square", is:"l",			d: "Clear piston tile #{0}",											p: [[n:"Tile Index",t:"enum",o:tileIndexes]],	],
		setLocationMode				: [ n: "Set location mode...",		a: true,	i: "", 						d: "Set location mode to {0}", 											p: [[n:"Mode",t:"mode"]],																														],
		//setAlarmSystemStatus			: [ n: "Set Smart Home Monitor status...",	a: true, i: "",					d: "Set Smart Home Monitor status to {0}",								p: [[n:"Status", t:"alarmSystemStatus"]],																										],
		sendEmail				: [ n: "Send email...",				a: true,	i: "envelope", 				d: "Send email with subject \"{1}\" to {0}", 							p: [[n:"Recipient",t:"email"],[n:"Subject",t:"string"],[n:"Message body",t:"string"]],																							],
		wolRequest				: [ n: "Wake a LAN device", 		a: true,	i: "", 						d: "Wake LAN device at address {0}{1}",									p: [[n:"MAC address",t:"string"],[n:"Secure code",t:"string",d:" with secure code {v}"]],	],
		adjustLevel				: [ n: "Adjust level...",	 r: ["setLevel"], 	i: "toggle-on",				d: "Adjust level by {0}%{1}",											p: [[n:"Adjustment",t:"integer",r:[-100,100]], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		adjustInfraredLevel			: [ n: "Adjust infrared level...",	 r: ["setInfraredLevel"], 	i: "toggle-on",	d: "Adjust infrared level by {0}%{1}",								p: [[n:"Adjustment",t:"integer",r:[-100,100]], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		adjustSaturation			: [ n: "Adjust saturation...",	 r: ["setSaturation"], 	i: "toggle-on",		d: "Adjust saturation by {0}%{1}",										p: [[n:"Adjustment",t:"integer",r:[-100,100]], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		adjustHue				: [ n: "Adjust hue...",	 r: ["setHue"], 	i: "toggle-on",					d: "Adjust hue by {0}°{1}",												p: [[n:"Adjustment",t:"integer",r:[-360,360]], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		adjustColorTemperature			: [ n: "Adjust color temperature...",	 r: ["setColorTemperature"], 	i: "toggle-on",				d: "Adjust color temperature by {0}°K%{1}",		p: [[n:"Adjustment",t:"integer",r:[-29000,29000]], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		fadeLevel				: [ n: "Fade level...",	 r: ["setLevel"], 		i: "toggle-on",				d: "Fade level{0} to {1}% in {2}{3}",									p: [[n:"Starting level",t:"level",d:" from {v}%"],[n:"Final level",t:"level"],[n:"Duration",t:"duration"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		fadeInfraredLevel			: [ n: "Fade infrared level...",	 r: ["setInfraredLevel"], 		i: "toggle-on",				d: "Fade infrared level{0} to {1}% in {2}{3}",		p: [[n:"Starting infrared level",t:"level",d:" from {v}%"],[n:"Final infrared level",t:"level"],[n:"Duration",t:"duration"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		fadeSaturation				: [ n: "Fade saturation...",	 r: ["setSaturation"], 		i: "toggle-on",				d: "Fade saturation{0} to {1}% in {2}{3}",					p: [[n:"Starting saturation",t:"level",d:" from {v}%"],[n:"Final saturation",t:"level"],[n:"Duration",t:"duration"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		fadeHue					: [ n: "Fade hue...",			 r: ["setHue"], 		i: "toggle-on",				d: "Fade hue{0} to {1}° in {2}{3}",								p: [[n:"Starting hue",t:"hue",d:" from {v}°"],[n:"Final hue",t:"hue"],[n:"Duration",t:"duration"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		fadeColorTemperature			: [ n: "Fade color temperature...",		 r: ["setColorTemperature"], 		i: "toggle-on",				d: "Fade color temperature{0} to {1}°K in {2}{3}",									p: [[n:"Starting color temperature",t:"colorTemperature",d:" from {v}°K"],[n:"Final color temperature",t:"colorTemperature"],[n:"Duration",t:"duration"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		flash					: [ n: "Flash...",	 r: ["on", "off"], 			i: "toggle-on",				d: "Flash on {0} / off {1} for {2} times{3}",							p: [[n:"On duration",t:"duration"],[n:"Off duration",t:"duration"],[n:"Number of flashes",t:"integer"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		flashLevel				: [ n: "Flash (level)...",	 r: ["setLevel"], 			i: "toggle-on",		d: "Flash {0}% {1} / {2}% {3} for {4} times{5}",						p: [[n:"Level 1", t:"level"],[n:"Duration 1",t:"duration"],[n:"Level 2", t:"level"],[n:"Duration 2",t:"duration"],[n:"Number of flashes",t:"integer"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		flashColor				: [ n: "Flash (color)...",	 r: ["setColor"], 			i: "toggle-on",		d: "Flash {0} {1} / {2} {3} for {4} times{5}",							p: [[n:"Color 1", t:"color"],[n:"Duration 1",t:"duration"],[n:"Color 2", t:"color"],[n:"Duration 2",t:"duration"],[n:"Number of flashes",t:"integer"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
//		iftttMaker				: [ n: "Send an IFTTT Maker event...",	a: true,							d: "Send the {0} IFTTT Maker event{1}{2}{3}",							p: [[n:"Event", t:"text"], [n:"Value 1", t:"string", d:", passing value1 = '{v}'"], [n:"Value 2", t:"string", d:", passing value2 = '{v}'"], [n:"Value 3", t:"string", d:", passing value3 = '{v}'"]],				],
//		lifxScene					: [ n: "LIFX - Activate scene...",	  	a: true, 							d: "Activate LIFX Scene '{0}'{1}", 										p: [[n: "Scene", t:"lifxScene"],[n: "Duration", t:"duration", d:" for {v}"]],					],
		writeToFuelStream			: [ n: "Write to fuel stream...",  		a: true, 							d: "Write data point '{2}' to fuel stream {0}{1}{3}", 					p: [[n: "Canister", t:"text", d:"{v} \\ "], [n:"Fuel stream name", t:"text"], [n: "Data", t:"dynamic"], [n: "Data source", t:"text", d:" from source '{v}'"]],					],
		storeMedia				: [ n: "Store media...",		 		a: true, 							d: "Store media", 														p: [],					],
		saveStateLocally			: [ n: "Capture attributes to local store...", 								d: "Capture attributes {0} to local state{1}{2}",						p: [[n: "Attributes", t:"attributes"],[n:'State container name',t:'string',d:' "{v}"'],[n:'Prevent overwriting existing state', t:'enum', o:['true','false'], d:' only if store is empty']], ],
//		saveStateGlobally			: [ n: "Capture attributes to global store...", 							d: "Capture attributes {0} to global state{1}{2}",						p: [[n: "Attributes", t:"attributes"],[n:'State container name',t:'string',d:' "{v}"'],[n:'Prevent overwriting existing state', t:'enum', o:['true','false'],, d:' only if store is empty']], ],
		loadStateLocally			: [ n: "Restore attributes from local store...", 							d: "Restore attributes {0} from local state{1}{2}",						p: [[n: "Attributes", t:"attributes"],[n:'State container name',t:'string',d:' "{v}"'],[n:'Empty state after restore', t:'enum', o:['true','false'], d:' and empty the store']], ],
//		loadStateGlobally			: [ n: "Restore attributes from global store...", 							d: "Restore attributes {0} from global state{1}{2}",					p: [[n: "Attributes", t:"attributes"],[n:'State container name',t:'string',d:' "{v}"'],[n:'Empty state after restore', t:'enum', o:['true','false'], d:' and empty the store']], ],
		parseJson				: [ n: "Parse JSON data...",			a: true,							d: "Parse JSON data {0}",												p: [[n: "JSON string", t:"string"]],																											],
		cancelTasks				: [ n: "Cancel all pending tasks",		a: true,							d: "Cancel all pending tasks",											p: [],																											],
//		lifxState				: [ n: "LIFX - Set State...",			a: true,							d: "Set LIFX lights matching {0} to {1}{2}{3}{4}{5}",					p: [[n: "Selector", t:"lifxSelector"],[n: "Switch (power)",t:"enum",o:["on","off"],d:" switch '{v}'"],[n: "Color",t:"color",d:" color '{v}'"],[n: "Level (brightness)",t:"level",d:" level {v}%"],[n: "Infrared level",t:"infraredLevel",d:" infrared {v}%"],[n: "Duration",t:"duration",d:" in {v}"]], ],
//		lifxToggle				: [ n: "LIFX - Toggle...",				a: true,							d: "Toggle LIFX lights matching {0}{1}",								p: [[n: "Selector", t:"lifxSelector"],[n: "Duration",t:"duration",d:" in {v}"]], ],
//		lifxBreathe				: [ n: "LIFX - Breathe...",				a: true,							d: "Breathe LIFX lights matching {0} to color {1}{2}{3}{4}{5}{6}{7}",	p: [[n: "Selector", t:"lifxSelector"],[n: "Color",t:"color"],[n: "From color",t:"color",d:" from color '{v}'"],[n: "Period", t:"duration", d:" with a period of {v}"],[n: "Cycles", t:"integer", d:" for {v} cycles"],[n:"Peak",t:"level",d:" with a peak at {v}% of the period"],[n:"Power on",t:"boolean",d:" and power on at start"],[n:"Persist",t:"boolean",d:" and persist"] ], ],
//		lifxPulse				: [ n: "LIFX - Pulse...",				a: true,							d: "Pulse LIFX lights matching {0} to color {1}{2}{3}{4}{5}{6}",		p: [[n: "Selector", t:"lifxSelector"],[n: "Color",t:"color"],[n: "From color",t:"color",d:" from color '{v}'"],[n: "Period", t:"duration", d:" with a period of {v}"],[n: "Cycles", t:"integer", d:" for {v} cycles"],[n:"Power on",t:"boolean",d:" and power on at start"],[n:"Persist",t:"boolean",d:" and persist"] ], ],
	//lifxCycle					: [ n: "LIFX - Cycle...",				a: true,							d: "Cycle LIFX lights matching {0}",									p: [[n: "Selector", t:"lifxSelector"]], ],
/*		[ n: "waitState",											d: "Wait for piston state change",	p: ["Change to:enum[any,false,true]"],															i: true,	l: true,						dd: "Wait for {0} state"],
		[ n: "flash",				r: ["on", "off"], 				d: "Flash",							p: ["On interval (milliseconds):number[250..5000]","Off interval (milliseconds):number[250..5000]","Number of flashes:number[1..10]"],					dd: "Flash {0}ms/{1}ms for {2} time(s)",		],
		[ n: "saveState",		d: "Save state to variable",			p: ["Attributes:attributes","Aggregation:aggregation","?Convert to data t:dataType","Save to state variable:string"],			stateVarEntry: 3,	dd: "Save state of attributes {0} to variable |[{3}]|'",	aggregated: true,	],
		[ n: "saveStateLocally",	d: "Capture state to local store",	p: ["Attributes:attributes","?Only if state is empty:bool"],																															dd: "Capture state of attributes {0} to local store",		],
		[ n: "saveStateGlobally",d: "Capture state to global store",	p: ["Attributes:attributes","?Only if state is empty:bool"],																															dd: "Capture state of attributes {0} to global store",	],
		[ n: "loadState",		d: "Load state from variable",		p: ["Attributes:attributes","Load from state variable:stateVariable","Allow translations:bool","Negate translation:bool"],								dd: "Load state of attributes {0} from variable |[{1}]|"				],
		[ n: "loadStateLocally",	d: "Restore state from local store",	p: ["Attributes:attributes","?Empty the state:bool"],																															dd: "Restore state of attributes {0} from local store",			],
		[ n: "loadStateGlobally",d: "Restore state from global store",	p: ["Attributes:attributes","?Empty the state:bool"],																															dd: "Restore state of attributes {0} from global store",			],
		[ n: "queueAskAlexaMessage",d: "Queue AskAlexa message",			p: ["Message:text", "?Unit:text", "?Application:text"],																		l: true, dd: "Queue AskAlexa message '{0}' in unit {1}",aggregated: true,	],
		[ n: "deleteAskAlexaMessages",d: "Delete AskAlexa messages",			p: ["Unit:text", "?Application:text"],																	l: true, dd: "Delete AskAlexa messages in unit {1}",aggregated: true,	],
		[ n: "cancelPendingTasks",d: "Cancel pending tasks",			p: ["Scope:enum[Local,Global]"],																														dd: "Cancel all pending {0} tasks",		],
*/

//	]

/*	+ (location.contactBookEnabled ? [
		sendNotificationToContacts : [n: "Send notification to contacts", p: ["Message:text","Contacts:contacts","Save notification:bool"], l: true, dd: "Send notification '{0}' to {1}", aggregated: true],
	] : [:])
	+ (getIftttKey() ? [
		iftttMaker : [n: "Send IFTTT Maker event", p: ["Event:text", "?Value1:string", "?Value2:string", "?Value3:string"], l: true, dd: "Send IFTTT Maker event '{0}' with parameters '{1}', '{2}', and '{3}'", aggregated: true],
	] : [:])
	+ (getLifxToken() ? [
		lifxScene: [n: "Activate LIFX scene", p: ["Scene:lifxScenes"], l: true, dd: "Activate LIFX Scene '{0}'", aggregated: true],
	] : [:])*/	
	
//	if(isHubitat()){
//	commands += [
		setAlarmSystemStatus		: [ n: "Set Hubitat Safety Monitor status...",	a: true, i: "",				d: "Set Hubitat Safety Monitor status to {0}",							p: [[n:"Status", t:"enum", o: getAlarmSystemStatusActions().collect {[n: it.value, v: it.key]}]],																										],
		//keep emulated flash to not break old pistons
		emulatedFlash				: [ n: "(Old do not use) Emulated Flash",	 r: ["on", "off"], 			i: "toggle-on",				d: "(Old do not use)Flash on {0} / off {1} for {2} times{3}",							p: [[n:"On duration",t:"duration"],[n:"Off duration",t:"duration"],[n:"Number of flashes",t:"integer"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],																],
		//add back emulated flash with "o" option so that it overrides the native flash command
		flash						: [ n: "Flash...",	 r: ["on", "off"], 			i: "toggle-on",				d: "Flash on {0} / off {1} for {2} times{3}",							p: [[n:"On duration",t:"duration"],[n:"Off duration",t:"duration"],[n:"Number of flashes",t:"integer"], [n:"Only if switch is...", t:"enum",o:["on","off"], d:" if already {v}"]],		o: true /*override physical command*/													]
	]
	//}
	
	return commands
}




private static Map comparisons() {
	return [
		conditions: [
			changed				: [ d: "changed",									g:"bdfis",				t: 1,	],
			did_not_change			: [ d: "did not change",								g:"bdfis",				t: 1,	],
			is 				: [ d: "is",				dd: "are",					g:"bs",		p: 1					],
			is_not	 			: [ d: "is not",			dd: "are not",					g:"bs",		p: 1					],
			is_any_of 			: [ d: "is any of",			dd: "are any of",				g:"s",		p: 1,	m: true,			],
			is_not_any_of 			: [ d: "is not any of",			dd: "are not any of",				g:"s",		p: 1,	m: true,			],
			is_equal_to			: [ d: "is equal to",			dd: "are equal to",				g:"di",		p: 1					],
			is_different_than		: [ d: "is different than",		dd: "are different than",			g:"di",		p: 1					],
			is_less_than			: [ d: "is less than",			dd: "are less than",				g:"di",		p: 1					],
			is_less_than_or_equal_to	: [ d: "is less than or equal to",	dd: "are less than or equal to",		g:"di",		p: 1					],
			is_greater_than			: [ d: "is greater than",		dd: "are greater than",				g:"di",		p: 1					],
			is_greater_than_or_equal_to	: [ d: "is greater than or equal to",	dd: "are greater than or equal to",		g:"di",		p: 1					],
			is_inside_of_range		: [ d: "is inside of range",		dd: "are inside of range",			g:"di",		p: 2					],
			is_outside_of_range		: [ d: "is outside of range",		dd: "are outside of range",			g:"di",		p: 2					],
			is_even				: [ d: "is even",			dd: "are even",					g:"di",							],
			is_odd				: [ d: "is odd",			dd: "are odd",					g:"di",							],
			was 				: [ d: "was",				dd: "were",					g:"bs",		p: 1,			t: 2,	],
			was_not 			: [ d: "was not",			dd: "were not",					g:"bs",		p: 1,			t: 2,	],
			was_any_of 			: [ d: "was any of",			dd: "were any of",				g:"s",		p: 1,	m: true,	t: 2,	],
			was_not_any_of 			: [ d: "was not any of",		dd: "were not any of",				g:"s",		p: 1,	m: true,	t: 2,	],
			was_equal_to 			: [ d: "was equal to",			dd: "were equal to",				g:"di",		p: 1,			t: 2,	],
			was_different_than 		: [ d: "was different than",		dd: "were different than",			g:"di",		p: 1,			t: 2,	],
			was_less_than 			: [ d: "was less than",			dd: "were less than",				g:"di",		p: 1,			t: 2,	],
			was_less_than_or_equal_to 	: [ d: "was less than or equal to",	dd: "were less than or equal to",		g:"di",		p: 1,			t: 2,	],
			was_greater_than 		: [ d: "was greater than",		dd: "were greater than",			g:"di",		p: 1,			t: 2,	],
			was_greater_than_or_equal_to 	: [ d: "was greater than or equal to",	dd: "were greater than or equal to",		g:"di",		p: 1,			t: 2,	],
			was_inside_of_range 		: [ d: "was inside of range",		dd: "were inside of range",			g:"di",		p: 2,			t: 2,	],
			was_outside_of_range 		: [ d: "was outside of range",		dd: "were outside of range",			g:"di",		p: 2,			t: 2,	],
			was_even			: [ d: "was even",			dd: "were even",						g:"di",			t: 2,	],
			was_odd				: [ d: "was odd",			dd: "were odd",							g:"di",			t: 2,	],
			is_any				: [ d: "is any",									g:"t",		p: 0					],
			is_before			: [ d: "is before",									g:"t",		p: 1					],
			is_after			: [ d: "is after",									g:"t",		p: 1					],
			is_between			: [ d: "is between",									g:"t",		p: 2					],
			is_not_between			: [ d: "is not between",								g:"t",		p: 2					],
		],
		triggers: [
			gets				: [ d: "gets",										g:"m",		p: 1					],
			happens_daily_at		: [ d: "happens daily at",								g:"t",		p: 1					],
			arrives				: [ d: "arrives",									g:"e",		p: 2					],
			executes			: [ d: "executes",									g:"v",		p: 1					],
			changes 			: [ d: "changes",			dd: "change",					g:"bdfis",						],
			changes_to 			: [ d: "changes to",			dd: "change to",				g:"bdis",	p: 1,					],
			changes_away_from 		: [ d: "changes away from",		dd: "change away from",				g:"bdis",	p: 1,					],
			changes_to_any_of 		: [ d: "changes to any of",		dd: "change to any of",				g:"dis",	p: 1,	m: true,			],
			changes_away_from_any_of 	: [ d: "changes away from any of",	dd: "change away from any of",			g:"dis",	p: 1,	m: true,			],
			drops				: [ d: "drops",				dd: "drop",					g:"di",							],
			does_not_drop			: [ d: "does not drop",			dd: "do not drop",				g:"di",							],
			drops_below			: [ d: "drops below",			dd: "drop below",				g:"di",		p: 1,					],
			drops_to_or_below		: [ d: "drops to or below",		dd: "drop to or below",				g:"di",		p: 1,					],
			remains_below			: [ d: "remains below",			dd: "remains below",				g:"di",		p: 1,					],
			remains_below_or_equal_to	: [ d: "remains below or equal to",	dd: "remains below or equal to",		g:"di",		p: 1,					],
			rises				: [ d: "rises",				dd: "rise",					g:"di",							],
			does_not_rise			: [ d: "does not rise",			dd: "do not rise",				g:"di",							],
			rises_above			: [ d: "rises above",			dd: "rise above",				g:"di",		p: 1,					],
			rises_to_or_above		: [ d: "rises to or above",		dd: "rise to or above",				g:"di",		p: 1,					],
			remains_above			: [ d: "remains above",			dd: "remains above",				g:"di",		p: 1,					],
			remains_above_or_equal_to	: [ d: "remains above or equal to",	dd: "remains above or equal to",		g:"di",		p: 1,					],
			enters_range			: [ d: "enters range",			dd: "enter range",				g:"di",		p: 2,					],
			remains_outside_of_range	: [ d: "remains outside of range",	dd: "remain outside of range",			g:"di",		p: 2,					],
			exits_range			: [ d: "exits range",			dd: "exit range",				g:"di",		p: 2,					],
			remains_inside_of_range		: [ d: "remains inside of range",	dd: "remain inside of range",			g:"di",		p: 2,					],
			becomes_even			: [ d: "becomes even",			dd: "become even",				g:"di",							],
			remains_even			: [ d: "remains even",			dd: "remain even",				g:"di",							],
			becomes_odd			: [ d: "becomes odd",			dd: "become odd",				g:"di",							],
			remains_odd			: [ d: "remains odd",			dd: "remain odd",				g:"di",							],
			stays_unchanged			: [ d: "stays unchanged",		dd: "stay unchanged",				g:"bdfis",				t: 1,	],
			stays	 			: [ d: "stays",				dd: "stay",					g:"bdis",	p: 1,			t: 1,	],
			stays_away_from			: [ d: "stays away from",		dd: "stay away from",				g:"bdis",	p: 1,			t: 1,	],
			stays_any_of			: [ d: "stays any of",			dd: "stay any of",				g:"dis",	p: 1,	m: true,	t: 1,	],
			stays_away_from_any_of		: [ d: "stays away from any of",	dd: "stay away from any of",			g:"bdis",	p: 1,	m: true,	t: 1,	],
			stays_equal_to 			: [ d: "stays equal to",		dd: "stay equal to",				g:"di",		p: 1,			t: 1,	],
			stays_different_than		: [ d: "stays different than",		dd: "stay different than",			g:"di",		p: 1,			t: 1,	],
			stays_less_than 		: [ d: "stays less than",		dd: "stay less than",				g:"di",		p: 1,			t: 1,	],
			stays_less_than_or_equal_to 	: [ d: "stays less than or equal to",	dd: "stay less than or equal to",		g:"di",		p: 1,			t: 1,	],
			stays_greater_than 		: [ d: "stays greater than",		dd: "stay greater than",			g:"di",		p: 1,			t: 1,	],
			stays_greater_than_or_equal_to 	: [ d: "stays greater than or equal to",	dd: "stay greater than or equal to",	g:"di",		p: 1,			t: 1,	],
			stays_inside_of_range 		: [ d: "stays inside of range",		dd: "stay inside of range",			g:"di",		p: 2,			t: 1,	],
			stays_outside_of_range 		: [ d: "stays outside of range",	dd: "stay outside of range",			g:"di",		p: 2,			t: 1,	],
			stays_even			: [ d: "stays even",			dd: "stay even",				g:"di",					t: 1,	],
			stays_odd			: [ d: "stays odd",			dd: "stay odd",					g:"di",					t: 1,	],
	]
	]
}

private static Map functions() {
	return [
	  	age			: [ t: "integer",						],
	  	previousage		: [ t: "integer",	d: "previousAge",	],
	  	previousvalue		: [ t: "dynamic",	d: "previousValue",	],
	  	newer			: [ t: "integer",						],
	  	older			: [ t: "integer",						],
	  	least			: [ t: "dynamic",						],
	  	most			: [ t: "dynamic",						],
	  	avg			: [ t: "decimal",						],
	  	variance		: [ t: "decimal",						],
	  	median			: [ t: "decimal",						],
	  	stdev			: [ t: "decimal",						],
	  	round			: [ t: "decimal",						],
	  	ceil			: [ t: "decimal",						],
	  	ceiling			: [ t: "decimal",						],
	  	floor			: [ t: "decimal",						],
	  	min			: [ t: "decimal",						],
	  	max			: [ t: "decimal",						],
	  	sum			: [ t: "decimal",						],
	  	count			: [ t: "integer",						],
	  	size			: [ t: "integer",						],
	  	left			: [ t: "string",						],
	  	right			: [ t: "string",						],
	  	mid			: [ t: "string",						],
	  	substring		: [ t: "string",						],
	  	sprintf			: [ t: "string",						],
	  	format			: [ t: "string",						],
	  	string			: [ t: "string",						],
	  	replace			: [ t: "string",						],
	  	indexof			: [ t: "integer",	d: "indexOf",		],
	  	lastindexof		: [ t: "integer",	d: "lastIndexOf",	],
	  	concat			: [ t: "string",						],
	  	text			: [ t: "string",						],
	  	lower			: [ t: "string",						],
	  	upper			: [ t: "string",						],
	  	title			: [ t: "string",						],
		int			: [ t: "integer",						],
		integer			: [ t: "integer",						],
		float			: [ t: "decimal",						],
		decimal			: [ t: "decimal",						],
		number			: [ t: "decimal",						],
		bool			: [ t: "boolean",						],
		boolean			: [ t: "boolean",						],
		power			: [ t: "decimal",						],
		sqr			: [ t: "decimal",						],
		sqrt			: [ t: "decimal",						],
		dewpoint		: [ t: "decimal",	d: "dewPoint",		],
		fahrenheit		: [ t: "decimal",						],
		celsius			: [ t: "decimal",						],
		dateAdd			: [ t: "time",		d: "dateAdd",		],
		startswith		: [ t: "boolean",	d: "startsWith",	],
		endswith		: [ t: "boolean",	d: "endsWith",		],
		contains		: [ t: "boolean",						],
		matches			: [ t: "boolean",						],
		eq			: [ t: "boolean",						],
		lt			: [ t: "boolean",						],
		le			: [ t: "boolean",						],
		gt			: [ t: "boolean",						],
		ge			: [ t: "boolean",						],
		not			: [ t: "boolean",						],
		isempty			: [ t: "boolean",	d: "isEmpty",		],
		if			: [ t: "dynamic",						],
		datetime		: [ t: "datetime",						],
		date			: [ t: "date",							],
		time			: [ t: "time",							],
		addseconds		: [ t: "datetime",	d: "addSeconds"		],
		addminutes		: [ t: "datetime",	d: "addMinutes"		],
		addhours		: [ t: "datetime",	d: "addHours"		],
		adddays			: [ t: "datetime",	d: "addDays"		],
		addweeks		: [ t: "datetime",	d: "addWeeks"		],
		isbetween		: [ t: "boolean",	d: "isBetween"		],
		formatduration		: [ t: "string",	d: "formatDuration"	],
		formatdatetime		: [ t: "string",	d: "formatDateTime"	],
		random			: [ t: "dynamic",						],
		strlen			: [ t: "integer",						],
		length			: [ t: "integer",						],
		coalesce		: [ t: "dynamic",						],
		weekdayname		: [ t: "string",	d: "weekDayName"	],
		monthname		: [ t: "string",	d: "monthName"		],
		arrayitem		: [ t: "dynamic",	d: "arrayItem"		],
		trim			: [ t: "string"							],
		trimleft		: [ t: "string", 	d: "trimLeft"		],
		ltrim			: [ t: "string"							],
		trimright		: [ t: "string",	d: "trimRight"		],
		rtrim			: [ t: "string"							],
		hsltohex		: [ t: "string",	d: "hslToHex"		],
		abs			: [ t: "dynamic"						],
		rangevalue		: [ t: "dynamic",	d: "rangeValue"		],
		rainbowvalue		: [ t: "string",	d: "rainbowValue"	],
		distance		: [ t: "decimal"						],
		json			: [ t: "dynamic"						],
		urlencode		: [ t: "string",	d: "urlEncode"					],
		encodeuricomponent	: [ t: "string",	d: "encodeURIComponent"					],
	]
}

/*
def getIftttKey() {
	def module = state.modules?.IFTTT
	return (module && module.connected ? module.key : null)
}

def getLifxToken() {
	def module = state.modules?.LIFX
	return (module && module.connected ? module.token : null)
}
*/
private Map getLocationModeOptions(updateCache = false) {
	def result = [:]
	for (mode in location.modes) {
		if (mode) result[hashId(mode.id, updateCache)] = mode.name;
	}
	return result
}
private static Map getAlarmSystemStatusActions() {
	return [	
		armAway:		"Arm Away",
		armHome: 		"Arm Home",
		armNight: 		"Arm Night",
		disarm:			"Disarm",
		armRules: 		"Arm Monitor Rules",	
		disarmRules: 		"Disarm Monitor Rules",	
		disarmAll:		"Disarm All",
		armAll:			"Arm All",
	   	cancelAlerts:		"Cancel Alerts"	
	]
}

/*
private static Map getAlarmSystemStatusOptions() {
	return [
	off:	"Disarmed",
	stay: 	"Armed/Stay",
	away:	"Armed/Away"
	]
}
*/

private static Map getHubitatAlarmSystemStatusOptions() {
	return [	
		armedAway:		"Armed Away",
		armingAway:		"Arming Pending exit delay",
		armedHome: 		"Armed Home",
		armedNight: 		"Armed Night",
		disarmed: 		"Disarmed",
		allDisarmed:		"All Disarmed"
	]	
}

private static Map getAlarmSystemAlertOptions() {
	return [	
		intrusion:		"Intrusion Away",
		"intrusion-home": 	"Intrusion Home",
		"intrusion-night": 	"Intrusion Night",
		smoke:			"Smoke",
		water:			"Water",
		rule:			"Rule",
		cancel:			"Alerts cancelled",
		arming:			"Arming failure"
	]
}

private static Map getAlarmSystemRuleOptions() {
	return [	
		armedRule: 	"Armed Rule",
		disarmedRule: 	"Disarmed Rule"
	]
}


/*
private Map getRoutineOptions(updateCache = false) {
	def routines = location.helloHome?.getPhrases()
	def result = [:]
	if (routines) {
		routines = routines.sort{ it?.label ?: '' }
		for(routine in routines) {
			if (routine && routine?.label)
				result[hashId(routine.id, updateCache)] = routine.label
		}
	}
	return result
}

private Map getAskAlexaOptions() {
	return state.askAlexaMacros ?: [null:"AskAlexa not installed - please install or open AskAlexa"]
}

private Map getEchoSistantOptions() {
	return state.echoSistantProfiles ?: [null:"EchoSistant not installed - please install or open EchoSistant"]
}
*/

private Map virtualDevices(updateCache = false) {
	return [
		date:			[ n: 'Date',			t: 'date',		],
		datetime:		[ n: 'Date & Time',		t: 'datetime',	],
		time:			[ n: 'Time',			t: 'time',		],
//		askAlexa:		[ n: 'Ask Alexa',		t: 'enum',	o: getAskAlexaOptions(),		m: true	],
//		echoSistant:		[ n: 'EchoSistant',		t: 'enum',	o: getEchoSistantOptions(),		m: true	],
		email:			[ n: 'Email',			t: 'email',						m: true	],
		powerSource:		[ n: 'Hub power source',	t: 'enum',	o: [battery: 'battery', mains: 'mains'],					x: true	],
		ifttt:			[ n: 'IFTTT',			t: 'string',						m: true	],
		mode:			[ n: 'Location mode',		t: 'enum', 	o: getLocationModeOptions(updateCache),	x: true],
		tile:			[ n: 'Piston tile',		t: 'enum',	o: ['1':'1','2':'2','3':'3','4':'4','5':'5','6':'6','7':'7','8':'8','9':'9','10':'10','11':'11','12':'12','13':'13','14':'14','15':'15','16':'16'],		m: true	],
//		routine:		[ n: 'Routine',			t: 'enum',	o: getRoutineOptions(updateCache),	m: true],
//		alarmSystemStatus:	[ n: 'Smart Home Monitor status',	t: 'enum',	o: getAlarmSystemStatusOptions(),	x: true]
//	] + (isHubitat() ? [
		alarmSystemStatus:	[ n: 'Hubitat Safety Monitor status',t: 'enum',		o: getHubitatAlarmSystemStatusOptions(), ac: getAlarmSystemStatusActions(),			x: true], //ac - actions. hubitat doesn't reuse the status for actions
		alarmSystemEvent:	[ n: 'Hubitat Safety Monitor event',t: 'enum',		o: getAlarmSystemStatusActions(),	m: true],
		alarmSystemAlert: 	[ n: 'Hubitat Safety Monitor alert',t: 'enum',		o: getAlarmSystemAlertOptions(),	m: true],
		alarmSystemRule: 	[ n: 'Hubitat Safety Monitor rule',t: 'enum',		o: getAlarmSystemRuleOptions(),		m: true]	
	]// : [:])
}

public List getColors(){
	return [
		[name:"Alice Blue",	 rgb:"#F0F8FF",	 h:208,	 s:100,	 l:97],
		[name:"Antique White",	 rgb:"#FAEBD7",	 h:34,	 s:78,	 l:91],
		[name:"Aqua",	 rgb:"#00FFFF",	 h:180,	 s:100,	 l:50],
		[name:"Aquamarine",	 rgb:"#7FFFD4",	 h:160,	 s:100,	 l:75],
		[name:"Azure",	 rgb:"#F0FFFF",	 h:180,	 s:100,	 l:97],
		[name:"Beige",	 rgb:"#F5F5DC",	 h:60,	 s:56,	 l:91],
		[name:"Bisque",	 rgb:"#FFE4C4",	 h:33,	 s:100,	 l:88],
		[name:"Blanched Almond",	 rgb:"#FFEBCD",	 h:36,	 s:100,	 l:90],
		[name:"Blue",	 rgb:"#0000FF",	 h:240,	 s:100,	 l:50],
		[name:"Blue Violet",	 rgb:"#8A2BE2",	 h:271,	 s:76,	 l:53],
		[name:"Brown",	 rgb:"#A52A2A",	 h:0,	 s:59,	 l:41],
		[name:"Burly Wood",	 rgb:"#DEB887",	 h:34,	 s:57,	 l:70],
		[name:"Cadet Blue",	 rgb:"#5F9EA0",	 h:182,	 s:25,	 l:50],
		[name:"Chartreuse",	 rgb:"#7FFF00",	 h:90,	 s:100,	 l:50],
		[name:"Chocolate",	 rgb:"#D2691E",	 h:25,	 s:75,	 l:47],
		[name:"Cool White",	 rgb:"#F3F6F7",	 h:187,	 s:19,	 l:96],
		[name:"Coral",	 rgb:"#FF7F50",	 h:16,	 s:100,	 l:66],
		[name:"Corn Flower Blue",	 rgb:"#6495ED",	 h:219,	 s:79,	 l:66],
		[name:"Corn Silk",	 rgb:"#FFF8DC",	 h:48,	 s:100,	 l:93],
		[name:"Crimson",	 rgb:"#DC143C",	 h:348,	 s:83,	 l:58],
		[name:"Cyan",	 rgb:"#00FFFF",	 h:180,	 s:100,	 l:50],
		[name:"Dark Blue",	 rgb:"#00008B",	 h:240,	 s:100,	 l:27],
		[name:"Dark Cyan",	 rgb:"#008B8B",	 h:180,	 s:100,	 l:27],
		[name:"Dark Golden Rod",	 rgb:"#B8860B",	 h:43,	 s:89,	 l:38],
		[name:"Dark Gray",	 rgb:"#A9A9A9",	 h:0,	 s:0,	 l:66],
		[name:"Dark Green",	 rgb:"#006400",	 h:120,	 s:100,	 l:20],
		[name:"Dark Khaki",	 rgb:"#BDB76B",	 h:56,	 s:38,	 l:58],
		[name:"Dark Magenta",	 rgb:"#8B008B",	 h:300,	 s:100,	 l:27],
		[name:"Dark Olive Green",	 rgb:"#556B2F",	 h:82,	 s:39,	 l:30],
		[name:"Dark Orange",	 rgb:"#FF8C00",	 h:33,	 s:100,	 l:50],
		[name:"Dark Orchid",	 rgb:"#9932CC",	 h:280,	 s:61,	 l:50],
		[name:"Dark Red",	 rgb:"#8B0000",	 h:0,	 s:100,	 l:27],
		[name:"Dark Salmon",	 rgb:"#E9967A",	 h:15,	 s:72,	 l:70],
		[name:"Dark Sea Green",	 rgb:"#8FBC8F",	 h:120,	 s:25,	 l:65],
		[name:"Dark Slate Blue",	 rgb:"#483D8B",	 h:248,	 s:39,	 l:39],
		[name:"Dark Slate Gray",	 rgb:"#2F4F4F",	 h:180,	 s:25,	 l:25],
		[name:"Dark Turquoise",	 rgb:"#00CED1",	 h:181,	 s:100,	 l:41],
		[name:"Dark Violet",	 rgb:"#9400D3",	 h:282,	 s:100,	 l:41],
		[name:"Daylight White",	 rgb:"#CEF4FD",	 h:191,	 s:9,	 l:90],
		[name:"Deep Pink",	 rgb:"#FF1493",	 h:328,	 s:100,	 l:54],
		[name:"Deep Sky Blue",	 rgb:"#00BFFF",	 h:195,	 s:100,	 l:50],
		[name:"Dim Gray",	 rgb:"#696969",	 h:0,	 s:0,	 l:41],
		[name:"Dodger Blue",	 rgb:"#1E90FF",	 h:210,	 s:100,	 l:56],
		[name:"Fire Brick",	 rgb:"#B22222",	 h:0,	 s:68,	 l:42],
		[name:"Floral White",	 rgb:"#FFFAF0",	 h:40,	 s:100,	 l:97],
		[name:"Forest Green",	 rgb:"#228B22",	 h:120,	 s:61,	 l:34],
		[name:"Fuchsia",	 rgb:"#FF00FF",	 h:300,	 s:100,	 l:50],
		[name:"Gainsboro",	 rgb:"#DCDCDC",	 h:0,	 s:0,	 l:86],
		[name:"Ghost White",	 rgb:"#F8F8FF",	 h:240,	 s:100,	 l:99],
		[name:"Gold",	 rgb:"#FFD700",	 h:51,	 s:100,	 l:50],
		[name:"Golden Rod",	 rgb:"#DAA520",	 h:43,	 s:74,	 l:49],
		[name:"Gray",	 rgb:"#808080",	 h:0,	 s:0,	 l:50],
		[name:"Green",	 rgb:"#008000",	 h:120,	 s:100,	 l:25],
		[name:"Green Yellow",	 rgb:"#ADFF2F",	 h:84,	 s:100,	 l:59],
		[name:"Honeydew",	 rgb:"#F0FFF0",	 h:120,	 s:100,	 l:97],
		[name:"Hot Pink",	 rgb:"#FF69B4",	 h:330,	 s:100,	 l:71],
		[name:"Indian Red",	 rgb:"#CD5C5C",	 h:0,	 s:53,	 l:58],
		[name:"Indigo",	 rgb:"#4B0082",	 h:275,	 s:100,	 l:25],
		[name:"Ivory",	 rgb:"#FFFFF0",	 h:60,	 s:100,	 l:97],
		[name:"Khaki",	 rgb:"#F0E68C",	 h:54,	 s:77,	 l:75],
		[name:"Lavender",	 rgb:"#E6E6FA",	 h:240,	 s:67,	 l:94],
		[name:"Lavender Blush",	 rgb:"#FFF0F5",	 h:340,	 s:100,	 l:97],
		[name:"Lawn Green",	 rgb:"#7CFC00",	 h:90,	 s:100,	 l:49],
		[name:"Lemon Chiffon",	 rgb:"#FFFACD",	 h:54,	 s:100,	 l:90],
		[name:"Light Blue",	 rgb:"#ADD8E6",	 h:195,	 s:53,	 l:79],
		[name:"Light Coral",	 rgb:"#F08080",	 h:0,	 s:79,	 l:72],
		[name:"Light Cyan",	 rgb:"#E0FFFF",	 h:180,	 s:100,	 l:94],
		[name:"Light Golden Rod Yellow",	 rgb:"#FAFAD2",	 h:60,	 s:80,	 l:90],
		[name:"Light Gray",	 rgb:"#D3D3D3",	 h:0,	 s:0,	 l:83],
		[name:"Light Green",	 rgb:"#90EE90",	 h:120,	 s:73,	 l:75],
		[name:"Light Pink",	 rgb:"#FFB6C1",	 h:351,	 s:100,	 l:86],
		[name:"Light Salmon",	 rgb:"#FFA07A",	 h:17,	 s:100,	 l:74],
		[name:"Light Sea Green",	 rgb:"#20B2AA",	 h:177,	 s:70,	 l:41],
		[name:"Light Sky Blue",	 rgb:"#87CEFA",	 h:203,	 s:92,	 l:75],
		[name:"Light Slate Gray",	 rgb:"#778899",	 h:210,	 s:14,	 l:53],
		[name:"Light Steel Blue",	 rgb:"#B0C4DE",	 h:214,	 s:41,	 l:78],
		[name:"Light Yellow",	 rgb:"#FFFFE0",	 h:60,	 s:100,	 l:94],
		[name:"Lime",	 rgb:"#00FF00",	 h:120,	 s:100,	 l:50],
		[name:"Lime Green",	 rgb:"#32CD32",	 h:120,	 s:61,	 l:50],
		[name:"Linen",	 rgb:"#FAF0E6",	 h:30,	 s:67,	 l:94],
		[name:"Maroon",	 rgb:"#800000",	 h:0,	 s:100,	 l:25],
		[name:"Medium Aquamarine",	 rgb:"#66CDAA",	 h:160,	 s:51,	 l:60],
		[name:"Medium Blue",	 rgb:"#0000CD",	 h:240,	 s:100,	 l:40],
		[name:"Medium Orchid",	 rgb:"#BA55D3",	 h:288,	 s:59,	 l:58],
		[name:"Medium Purple",	 rgb:"#9370DB",	 h:260,	 s:60,	 l:65],
		[name:"Medium Sea Green",	 rgb:"#3CB371",	 h:147,	 s:50,	 l:47],
		[name:"Medium Slate Blue",	 rgb:"#7B68EE",	 h:249,	 s:80,	 l:67],
		[name:"Medium Spring Green",	 rgb:"#00FA9A",	 h:157,	 s:100,	 l:49],
		[name:"Medium Turquoise",	 rgb:"#48D1CC",	 h:178,	 s:60,	 l:55],
		[name:"Medium Violet Red",	 rgb:"#C71585",	 h:322,	 s:81,	 l:43],
		[name:"Midnight Blue",	 rgb:"#191970",	 h:240,	 s:64,	 l:27],
		[name:"Mint Cream",	 rgb:"#F5FFFA",	 h:150,	 s:100,	 l:98],
		[name:"Misty Rose",	 rgb:"#FFE4E1",	 h:6,	 s:100,	 l:94],
		[name:"Moccasin",	 rgb:"#FFE4B5",	 h:38,	 s:100,	 l:85],
		[name:"Navajo White",	 rgb:"#FFDEAD",	 h:36,	 s:100,	 l:84],
		[name:"Navy",	 rgb:"#000080",	 h:240,	 s:100,	 l:25],
		[name:"Old Lace",	 rgb:"#FDF5E6",	 h:39,	 s:85,	 l:95],
		[name:"Olive",	 rgb:"#808000",	 h:60,	 s:100,	 l:25],
		[name:"Olive Drab",	 rgb:"#6B8E23",	 h:80,	 s:60,	 l:35],
		[name:"Orange",	 rgb:"#FFA500",	 h:39,	 s:100,	 l:50],
		[name:"Orange Red",	 rgb:"#FF4500",	 h:16,	 s:100,	 l:50],
		[name:"Orchid",	 rgb:"#DA70D6",	 h:302,	 s:59,	 l:65],
		[name:"Pale Golden Rod",	 rgb:"#EEE8AA",	 h:55,	 s:67,	 l:80],
		[name:"Pale Green",	 rgb:"#98FB98",	 h:120,	 s:93,	 l:79],
		[name:"Pale Turquoise",	 rgb:"#AFEEEE",	 h:180,	 s:65,	 l:81],
		[name:"Pale Violet Red",	 rgb:"#DB7093",	 h:340,	 s:60,	 l:65],
		[name:"Papaya Whip",	 rgb:"#FFEFD5",	 h:37,	 s:100,	 l:92],
		[name:"Peach Puff",	 rgb:"#FFDAB9",	 h:28,	 s:100,	 l:86],
		[name:"Peru",	 rgb:"#CD853F",	 h:30,	 s:59,	 l:53],
		[name:"Pink",	 rgb:"#FFC0CB",	 h:350,	 s:100,	 l:88],
		[name:"Plum",	 rgb:"#DDA0DD",	 h:300,	 s:47,	 l:75],
		[name:"Powder Blue",	 rgb:"#B0E0E6",	 h:187,	 s:52,	 l:80],
		[name:"Purple",	 rgb:"#800080",	 h:300,	 s:100,	 l:25],
		[name:"Red",	 rgb:"#FF0000",	 h:0,	 s:100,	 l:50],
		[name:"Rosy Brown",	 rgb:"#BC8F8F",	 h:0,	 s:25,	 l:65],
		[name:"Royal Blue",	 rgb:"#4169E1",	 h:225,	 s:73,	 l:57],
		[name:"Saddle Brown",	 rgb:"#8B4513",	 h:25,	 s:76,	 l:31],
		[name:"Salmon",	 rgb:"#FA8072",	 h:6,	 s:93,	 l:71],
		[name:"Sandy Brown",	 rgb:"#F4A460",	 h:28,	 s:87,	 l:67],
		[name:"Sea Green",	 rgb:"#2E8B57",	 h:146,	 s:50,	 l:36],
		[name:"Sea Shell",	 rgb:"#FFF5EE",	 h:25,	 s:100,	 l:97],
		[name:"Sienna",	 rgb:"#A0522D",	 h:19,	 s:56,	 l:40],
		[name:"Silver",	 rgb:"#C0C0C0",	 h:0,	 s:0,	 l:75],
		[name:"Sky Blue",	 rgb:"#87CEEB",	 h:197,	 s:71,	 l:73],
		[name:"Slate Blue",	 rgb:"#6A5ACD",	 h:248,	 s:53,	 l:58],
		[name:"Slate Gray",	 rgb:"#708090",	 h:210,	 s:13,	 l:50],
		[name:"Snow",	 rgb:"#FFFAFA",	 h:0,	 s:100,	 l:99],
		[name:"Soft White",	 rgb:"#B6DA7C",	 h:83,	 s:44,	 l:67],
		[name:"Spring Green",	 rgb:"#00FF7F",	 h:150,	 s:100,	 l:50],
		[name:"Steel Blue",	 rgb:"#4682B4",	 h:207,	 s:44,	 l:49],
		[name:"Tan",	 rgb:"#D2B48C",	 h:34,	 s:44,	 l:69],
		[name:"Teal",	 rgb:"#008080",	 h:180,	 s:100,	 l:25],
		[name:"Thistle",	 rgb:"#D8BFD8",	 h:300,	 s:24,	 l:80],
		[name:"Tomato",	 rgb:"#FF6347",	 h:9,	 s:100,	 l:64],
		[name:"Turquoise",	 rgb:"#40E0D0",	 h:174,	 s:72,	 l:56],
		[name:"Violet",	 rgb:"#EE82EE",	 h:300,	 s:76,	 l:72],
		[name:"Warm White",	 rgb:"#DAF17E",	 h:72,	 s:20,	 l:72],
		[name:"Wheat",	 rgb:"#F5DEB3",	 h:39,	 s:77,	 l:83],
		[name:"White",	 rgb:"#FFFFFF",	 h:0,	 s:0,	 l:100],
		[name:"White Smoke",	 rgb:"#F5F5F5",	 h:0,	 s:0,	 l:96],
		[name:"Yellow",	 rgb:"#FFFF00",	 h:60,	 s:100,	 l:50],
		[name:"Yellow Green",	 rgb:"#9ACD32",	 h:80,	 s:61,	 l:50]
	]
}

private isHubitat(){
 	return hubUID != null
}
