import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil
import org.openqa.selenium.Keys

// TC-18.2 : Memperbarui jumlah stok barang (Positive)
// Data    : Buket Mawar Merah (#PRD-001); stok awal 20; Jumlah Stok Baru 30

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S18_ManajemenStok/'
String base = 'http://127.0.0.1:8000'
String adminEmail = 'admin@royalflorist.com'
String adminPass  = '12345678'

String sku      = '#PRD-001'
String stokBaru = '30'

TestObject stokCell   = obj("//tr[contains(.,'" + sku + "')]/td[6]")
TestObject statusCell = obj("//tr[contains(.,'" + sku + "')]/td[7]")
TestObject btnEdit    = obj("(//tr[contains(.,'" + sku + "')]/td[last()]//*[self::button or self::a])[1]")
TestObject modalTitle = obj("//*[normalize-space()='Penyesuaian Stok']")
TestObject inputStok  = obj("//label[normalize-space()='Jumlah Stok Baru']/following::input[@type='number'][1]")
TestObject btnSimpan  = obj("//button[normalize-space()='Simpan Stok']")

WebUI.openBrowser('')
WebUI.maximizeWindow()

// ===== Precondition: Admin sudah login =====
WebUI.navigateToUrl(base + '/login')
WebUI.delay(2)
WebUI.setText(obj("(//input[@type='email' or @name='email'])[1]"), adminEmail)
WebUI.setText(obj("(//input[@type='password'])[1]"), adminPass)
WebUI.click(obj("(//button[@type='submit'])[1]"))
WebUI.delay(3)
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
WebUI.scrollToElement(stokCell, 5, FailureHandling.OPTIONAL)

String stokAwal = WebUI.getText(stokCell).trim()
String statusAwal = WebUI.getText(statusCell).trim()
KeywordUtil.logInfo("Stok awal ${sku} = ${stokAwal} (${statusAwal})")
if (stokAwal == stokBaru) {
	// bila test sudah pernah dijalankan, stok sudah 30 -> pakai nilai lain agar perubahan tetap teruji
	stokBaru = '20'
	KeywordUtil.logInfo('Stok awal sudah 30, memakai nilai baru 20 agar perubahan teruji')
}
WebUI.takeScreenshot(shot + 'TC-18.2_Gambar1_stok_sebelum.png')

// ===== Langkah 2: Klik ikon edit (pensil) pada produk =====
WebUI.click(btnEdit)
WebUI.delay(1)
WebUI.verifyElementVisible(modalTitle, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-18.2_Gambar2_modal_penyesuaian_stok.png')

// ===== Langkah 3: Ubah Jumlah Stok Baru =====
WebUI.click(inputStok)
WebUI.sendKeys(inputStok, Keys.chord(Keys.CONTROL, 'a'))
WebUI.sendKeys(inputStok, stokBaru)
WebUI.verifyElementAttributeValue(inputStok, 'value', stokBaru, 5, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-18.2_Gambar3_input_stok_baru.png')

// ===== Langkah 4: Klik Simpan Stok =====
WebUI.click(btnSimpan)
WebUI.delay(2)
WebUI.takeScreenshot(shot + 'TC-18.2_Gambar4_setelah_simpan.png')

// ===== Verifikasi: stok & status di tabel ter-update =====
WebUI.scrollToElement(stokCell, 5, FailureHandling.OPTIONAL)
String stokAkhir = WebUI.getText(stokCell).trim()
String statusAkhir = WebUI.getText(statusCell).trim()
KeywordUtil.logInfo("Stok akhir ${sku} = ${stokAkhir} (${statusAkhir})")
WebUI.verifyEqual(stokAkhir, stokBaru, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyMatch(statusAkhir, '(?i).*aman.*', true, FailureHandling.CONTINUE_ON_FAILURE)

// ===== Verifikasi data benar-benar tersimpan (refresh) =====
WebUI.refresh()
WebUI.delay(2)
WebUI.scrollToElement(stokCell, 5, FailureHandling.OPTIONAL)
WebUI.verifyEqual(WebUI.getText(stokCell).trim(), stokBaru, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-18.2_Gambar5_setelah_refresh.png')

WebUI.closeBrowser()