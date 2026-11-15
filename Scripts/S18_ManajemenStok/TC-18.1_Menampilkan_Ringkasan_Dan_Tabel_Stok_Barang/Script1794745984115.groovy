import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

def obj = { String xp ->
	TestObject t = new TestObject(xp)
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S18_ManajemenStok/'
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'

WebUI.openBrowser('')
WebUI.maximizeWindow()

// ===== Precondition: Admin sudah login =====
WebUI.navigateToUrl(base + '/login')
WebUI.delay(2)
WebUI.setText(obj("(//input[@type='email' or @name='email'])[1]"), adminEmail)
WebUI.setText(obj("(//input[@type='password'])[1]"), adminPass)
WebUI.click(obj("(//button[@type='submit'])[1]"))
WebUI.delay(3)
WebUI.takeScreenshot(shot + 'TC-18.1_Gambar0_setelah_login.png')
KeywordUtil.logInfo('URL setelah login: ' + WebUI.getUrl())
if (WebUI.getUrl().contains('/login')) {
	KeywordUtil.markFailedAndStop('Login admin gagal. Periksa email dan password admin di script.')
}

// ===== Langkah 1: Buka menu Stok =====
if (WebUI.verifyElementPresent(obj("//a[contains(@href,'/admin/stok')]"), 5, FailureHandling.OPTIONAL)) {
	WebUI.click(obj("//a[contains(@href,'/admin/stok')]"))
} else {
	KeywordUtil.logInfo('Menu Stok tidak ditemukan, membuka lewat URL langsung')
	WebUI.navigateToUrl(base + '/admin/stok')
}
WebUI.delay(2)
WebUI.verifyMatch(WebUI.getUrl(), '.*admin/stok.*', true, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-18.1_Gambar1_menu_stok.png')

// ===== Langkah 2: Amati kartu ringkasan, grafik, tabel =====
for (t in ['Total Stok Produk', 'Stok Aman', 'Stok Menipis', 'Stok Kritis', 'Stok per Kategori', 'Daftar Stok Produk']) {
	WebUI.verifyTextPresent(t, false, FailureHandling.CONTINUE_ON_FAILURE)
}
WebUI.takeScreenshot(shot + 'TC-18.1_Gambar2_kartu_dan_grafik.png')

WebUI.scrollToElement(obj("//*[normalize-space()='Daftar Stok Produk']"), 5, FailureHandling.OPTIONAL)
WebUI.delay(1)
WebUI.takeScreenshot(shot + 'TC-18.1_Gambar3_tabel_stok.png')

// ===== Cek konsistensi kartu vs tabel =====
// Hasil: [totalKartu, amanKartu, menipisKartu, kritisKartu, barisTotal, barisAman, barisMenipis, barisKritis, semuaPunyaStatus]
def cek = WebUI.executeJavaScript('''
var txt = document.body.innerText;
function n(label) {
	var m = txt.match(new RegExp(label + '\\\\s+(\\\\d+)', 'i'));
	return m ? parseInt(m[1]) : -1;
}
var rows = Array.from(document.querySelectorAll('table tbody tr')).filter(function(r) {
	return r.querySelectorAll('td').length > 1;
});
var aman = 0, menipis = 0, kritis = 0, punyaStatus = true;
rows.forEach(function(r) {
	var t = r.innerText.toLowerCase();
	if (t.indexOf('kritis') >= 0) { kritis++; }
	else if (t.indexOf('menipis') >= 0) { menipis++; }
	else if (t.indexOf('aman') >= 0) { aman++; }
	else { punyaStatus = false; }
});
return [n('Total Stok Produk'), n('Stok Aman'), n('Stok Menipis'), n('Stok Kritis'),
		rows.length, aman, menipis, kritis, punyaStatus];
''', null)

KeywordUtil.logInfo("Kartu: total=${cek[0]} aman=${cek[1]} menipis=${cek[2]} kritis=${cek[3]} | Tabel: baris=${cek[4]} aman=${cek[5]} menipis=${cek[6]} kritis=${cek[7]} | semuaPunyaStatus=${cek[8]}")

WebUI.verifyEqual(cek[0] as int, cek[4] as int, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(cek[1] as int, cek[5] as int, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(cek[2] as int, cek[6] as int, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(cek[3] as int, cek[7] as int, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(cek[8], true, FailureHandling.CONTINUE_ON_FAILURE)

WebUI.closeBrowser()