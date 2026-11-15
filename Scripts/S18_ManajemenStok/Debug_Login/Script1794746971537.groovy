import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.util.KeywordUtil

def obj = { String xp ->
	TestObject t = new TestObject(xp)
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S18_ManajemenStok/'
String base = 'http://127.0.0.1:8000'

WebUI.openBrowser('')
WebUI.maximizeWindow()
WebUI.navigateToUrl(base + '/login')
WebUI.delay(2)

// 1. Daftar semua isian dan tombol di halaman login
def info = WebUI.executeJavaScript("return Array.from(document.querySelectorAll('input,button,select')).map(function(e){return e.tagName+' type='+e.type+' name='+e.name+' id='+e.id+' placeholder='+e.placeholder+' text='+(e.innerText||'').trim()}).join('\\n')", null)
KeywordUtil.logInfo('ISIAN HALAMAN LOGIN:\n' + info)

// 2. Isi form
WebUI.setText(obj("(//input[@type='email' or @name='email' or contains(@placeholder,'mail')])[1]"), 'admin@royalflorist.com')
WebUI.setText(obj("(//input[@type='password'])[1]"), '12345678')     // ganti jika password lain
WebUI.takeScreenshot(shot + 'DEBUG_login_1_terisi.png')

// 3. Kirim
WebUI.click(obj("(//button[@type='submit' or contains(.,'Login') or contains(.,'Masuk')])[1]"))
WebUI.delay(3)
WebUI.takeScreenshot(shot + 'DEBUG_login_2_sesudah.png')

// 4. Hasil: URL dan teks di halaman
KeywordUtil.logInfo('URL SESUDAH: ' + WebUI.getUrl())
def teks = WebUI.executeJavaScript("return document.body.innerText.substring(0,600)", null)
KeywordUtil.logInfo('TEKS HALAMAN:\n' + teks)

WebUI.closeBrowser()