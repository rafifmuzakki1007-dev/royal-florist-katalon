import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

// TC-15.1 : Menampilkan daftar pelanggan (Positive)

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S15_ManajemenPelanggan/'
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
if (WebUI.getUrl().contains('/login')) {
	KeywordUtil.markFailedAndStop('Login admin gagal. Periksa email dan password admin di script.')
}

// ===== Langkah 1: Buka menu Pelanggan =====
if (WebUI.verifyElementPresent(obj("//a[contains(@href,'/admin/pelanggan')]"), 5, FailureHandling.OPTIONAL)) {
	WebUI.click(obj("//a[contains(@href,'/admin/pelanggan')]"))
} else {
	KeywordUtil.logInfo('Menu Pelanggan tidak ditemukan, membuka lewat URL langsung')
	WebUI.navigateToUrl(base + '/admin/pelanggan')
}
WebUI.delay(2)
WebUI.verifyMatch(WebUI.getUrl(), '.*admin/pelanggan.*', true, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.takeScreenshot(shot + 'TC-15.1_Gambar1_menu_pelanggan.png')

// ===== Langkah 2: Amati kartu statistik dan tabel daftar pelanggan =====
for (t in ['Daftar Pelanggan', 'Total Pelanggan', 'Pelanggan Lama', 'Pelanggan Baru',
		   'Nama Pelanggan', 'Email', 'Kontak', 'Kategori', 'Aksi']) {
	WebUI.verifyTextPresent(t, false, FailureHandling.CONTINUE_ON_FAILURE)
}
WebUI.takeScreenshot(shot + 'TC-15.1_Gambar2_kartu_statistik.png')

WebUI.scrollToElement(obj('//table'), 5, FailureHandling.OPTIONAL)
WebUI.delay(1)
WebUI.takeScreenshot(shot + 'TC-15.1_Gambar3_tabel_pelanggan.png')

// ===== Cek konsistensi kartu vs tabel vs footer, dan kelengkapan data =====
// Hasil: [kartuTotal, kartuLama, kartuBaru, barisTabel, barisLama, barisBaru, footerTotal, jumlahNamaKosong, nomorBarisNamaKosong]
def cek = WebUI.executeJavaScript('''
var txt = document.body.innerText;
function n(label) {
	var m = txt.match(new RegExp(label + '[^0-9]{1,10}([0-9]+)', 'i'));
	return m ? parseInt(m[1]) : -1;
}
var rows = Array.from(document.querySelectorAll('table tbody tr')).filter(function(r) {
	return r.querySelectorAll('td').length > 1;
});
var lama = 0, baru = 0, kosong = 0, nomor = [];
rows.forEach(function(r, i) {
	var td = r.querySelectorAll('td');
	var kat = td[4] ? td[4].innerText.toLowerCase() : '';
	if (kat.indexOf('lama') >= 0) { lama++; }
	else if (kat.indexOf('baru') >= 0) { baru++; }
	var nama = td[1] ? td[1].innerText.trim() : '';
	if (nama === '' || nama === '-') { kosong++; nomor.push(i + 1); }
});
var f = txt.match(/Menampilkan ([0-9]+) dari ([0-9]+)/);
return [n('Total Pelanggan'), n('Pelanggan Lama'), n('Pelanggan Baru'),
		rows.length, lama, baru, f ? parseInt(f[2]) : -1, kosong, nomor.join(',')];
''', null)

KeywordUtil.logInfo("Kartu: total=${cek[0]} lama=${cek[1]} baru=${cek[2]} | Tabel: baris=${cek[3]} lama=${cek[4]} baru=${cek[5]} | footerTotal=${cek[6]} | namaKosong=${cek[7]} (baris ${cek[8]})")

WebUI.verifyEqual(cek[0] as int, cek[3] as int, FailureHandling.CONTINUE_ON_FAILURE)                 // total kartu = jumlah baris
WebUI.verifyEqual((cek[1] as int) + (cek[2] as int), cek[0] as int, FailureHandling.CONTINUE_ON_FAILURE)  // lama + baru = total
WebUI.verifyEqual(cek[1] as int, cek[4] as int, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(cek[2] as int, cek[5] as int, FailureHandling.CONTINUE_ON_FAILURE)
WebUI.verifyEqual(cek[6] as int, cek[3] as int, FailureHandling.CONTINUE_ON_FAILURE)                 // footer "Menampilkan X dari Y"

// Kelengkapan data: setiap pelanggan seharusnya punya nama.
// Baris ini akan FAIL bila ada pelanggan tanpa nama (kandidat bug data). Beri komentar bila tidak ingin diuji.
if ((cek[7] as int) > 0) {
	KeywordUtil.logInfo("Pelanggan tanpa nama pada baris: ${cek[8]}")
	WebUI.takeScreenshot(shot + 'TC-15.1_Gambar4_pelanggan_tanpa_nama.png')
}
WebUI.verifyEqual(cek[7] as int, 0, FailureHandling.CONTINUE_ON_FAILURE)

WebUI.closeBrowser()