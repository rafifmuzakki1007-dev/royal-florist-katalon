import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

// TC-09.3 : Checkout tanpa memilih alamat pengiriman (Negative)
// Expected: sistem menolak (validasi alamat wajib) dan pesanan TIDAK dibuat.
// Hasil FAIL (BUG) hanya bila pelanggan tetap diarahkan ke Pembayaran atau pesanan baru tercatat.

// ===================== KONFIGURASI =====================
String base = 'http://127.0.0.1:8000'
String emailPelanggan = 'karmi@gmail.com'     // akun yang BELUM punya alamat tersimpan
String passPelanggan  = '12345678'
int tungguManualDetik = 90
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S09_Checkout/'
// =======================================================

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

// Klik elemen bila ada (tidak error bila tidak ada). Hasil: true bila diklik
def klik = { String xp ->
	if (WebUI.verifyElementPresent(obj(xp), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(obj(xp), FailureHandling.OPTIONAL)
		return true
	}
	return false
}

def teksHalaman = { -> WebUI.getText(obj('//body')) }

// Tunggu sampai kondisi terpenuhi (untuk bantuan manual)
def tunggu = { Closure kondisi, String pesan ->
	KeywordUtil.logInfo(pesan)
	for (int i = 0; i < tungguManualDetik; i += 3) {
		if (kondisi()) { return true }
		WebUI.delay(3)
	}
	return kondisi()
}

def loginPelanggan = {
	WebUI.navigateToUrl(base + '/login')
	WebUI.waitForPageLoad(15)
	WebUI.setText(obj("(//input[@type='email' or @name='email'])[1]"), emailPelanggan)
	WebUI.setText(obj("(//input[@type='password'])[1]"), passPelanggan)
	WebUI.click(obj("(//button[@type='submit'])[1]"))
	WebUI.waitForPageLoad(15)
	WebUI.delay(2)
	if (WebUI.getUrl().contains('/login')) {
		WebUI.takeScreenshot(shot + 'TC-09.3_LOGIN_gagal.png')
		KeywordUtil.markFailedAndStop('Login gagal untuk ' + emailPelanggan + '. Pastikan password akun ini 12345678.')
	}
}

def bukaCheckout = {
	if (!WebUI.getUrl().contains('checkout')) {
		if (klik("//a[contains(@href,'keranjang') or contains(@href,'cart')]")) {
			WebUI.delay(2)
			klik("//*[self::a or self::button][contains(normalize-space(),'Checkout')]")
			WebUI.delay(2)
		}
	}
	if (!WebUI.getUrl().contains('checkout')) {
		WebUI.navigateToUrl(base + '/pelanggan/checkout')
		WebUI.delay(2)
	}
}

// Ringkasan Tagihan: [subtotal, biayaKurir, total] (-1 bila tidak terbaca)
def ringkasan = {
	return WebUI.executeJavaScript('''
var lines = document.body.innerText.split('\\n').map(function(l) { return l.trim(); }).filter(function(l) { return l !== ''; });
function nilai(label) {
	for (var i = 0; i < lines.length; i++) {
		if (lines[i].indexOf(label) === 0) {
			for (var j = i; j <= i + 2 && j < lines.length; j++) {
				if (lines[j].indexOf('Rp') >= 0) { return parseInt(lines[j].replace(/[^0-9]/g, '')); }
			}
		}
	}
	return -1;
}
return [nilai('Subtotal Produk'), nilai('Biaya Kurir'), nilai('Total Wajib Bayar')];
''', null)
}

// Jumlah nomor invoice unik di Riwayat Pesanan (-1 bila halaman tidak bisa dibuka)
def hitungInvoice = {
	if (!klik("//a[normalize-space()='Riwayat Pesanan']")) { return -1 }
	WebUI.delay(2)
	def n = WebUI.executeJavaScript('''
var m = document.body.innerText.match(/INV-[A-Za-z0-9-]+/g);
return m ? new Set(m).size : 0;
''', null)
	return n as int
}

// Tambah produk dari Katalog (bila keranjang kosong)
def tambahProdukOtomatis = {
	try {
		if (!klik("//a[normalize-space()='Katalog']")) { return false }
		WebUI.delay(3)
		if (!klik("(//*[self::button or self::a][contains(normalize-space(),'Keranjang') or contains(normalize-space(),'Tambah')])[1]")) {
			klik("(//main//a[.//img])[1]")
			WebUI.delay(3)
		}
		klik("(//input[@type='radio'])[1]")
		klik("//*[self::button or self::a][contains(normalize-space(),'Tambah ke Keranjang')]")
		WebUI.delay(2)
		return true
	} catch (Exception e) {
		KeywordUtil.logInfo('Tambah produk otomatis gagal: ' + e.getMessage())
		return false
	}
}

// Ambil teks pesan validasi/alert/toast yang tampil di halaman
def ambilPesan = {
	return WebUI.executeJavaScript('''
var el = document.querySelectorAll('[role="alert"], .alert, .invalid-feedback, .swal2-popup, .toast, [class*="error"], [class*="toast"], [class*="alert"]');
var out = [];
el.forEach(function(e) {
	var t = (e.innerText || '').trim();
	if (t !== '' && out.indexOf(t) < 0) { out.push(t.substring(0, 120)); }
});
return out;
''', null)
}

boolean bug = false
String alasanBug = ''

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
	// ===== Precondition: login & catat jumlah pesanan awal =====
	loginPelanggan()
	int invoiceAwal = hitungInvoice()
	KeywordUtil.logInfo("Jumlah invoice sebelum uji: ${invoiceAwal}")

	// ===== Langkah 1: Buka halaman Checkout dari Keranjang =====
	bukaCheckout()
	def r = ringkasan()
	if ((r[2] as int) <= 0) {
		KeywordUtil.logInfo('Keranjang kosong, menambahkan produk dari Katalog')
		tambahProdukOtomatis()
		bukaCheckout()
		r = ringkasan()
		if ((r[2] as int) <= 0) {
			WebUI.takeScreenshot(shot + 'TC-09.3_Gambar0_keranjang_kosong.png')
			boolean adaProduk = tunggu({ (ringkasan()[2] as int) > 0 },
				'Keranjang masih kosong. TAMBAHKAN PRODUK lalu buka halaman Checkout di jendela browser ini (skrip menunggu).')
			if (!adaProduk) {
				KeywordUtil.markFailedAndStop('Keranjang kosong dan produk tidak berhasil ditambahkan.')
			}
		}
	}

	// Precondition: belum ada alamat pengiriman
	if (!teksHalaman().contains('Belum ada alamat pengiriman')) {
		WebUI.takeScreenshot(shot + 'TC-09.3_Gambar0_akun_sudah_punya_alamat.png')
		KeywordUtil.markFailedAndStop('Precondition TC-09.3 tidak terpenuhi: akun ' + emailPelanggan + ' sudah punya alamat. Pakai akun lain yang belum punya alamat (ubah emailPelanggan di bagian KONFIGURASI).')
	}
	WebUI.takeScreenshot(shot + 'TC-09.3_Gambar1_checkout_tanpa_alamat.png')

	// ===== Langkah 2: Jangan pilih alamat. Langkah 3: Klik Lanjut Bayar =====
	TestObject btnLanjut = obj("//*[self::button or self::a][contains(normalize-space(),'Lanjut Bayar')]")
	boolean tombolNonaktif = false
	String attrDisabled = WebUI.getAttribute(btnLanjut, 'disabled', FailureHandling.OPTIONAL)
	if (attrDisabled != null) { tombolNonaktif = true }

	if (tombolNonaktif) {
		KeywordUtil.logInfo('Tombol Lanjut Bayar NONAKTIF saat alamat kosong (validasi lewat tombol dinonaktifkan).')
	} else {
		WebUI.click(btnLanjut)
		WebUI.delay(2)
		// Tangkap pop-up (alert) bila validasi memakai alert browser
		if (WebUI.waitForAlert(2, FailureHandling.OPTIONAL)) {
			KeywordUtil.logInfo('Alert muncul: ' + WebUI.getAlertText(FailureHandling.OPTIONAL))
			WebUI.acceptAlert(FailureHandling.OPTIONAL)
		}
	}
	WebUI.takeScreenshot(shot + 'TC-09.3_Gambar2_setelah_klik_lanjut_bayar.png')   // bukti pesan validasi (bila ada)
	KeywordUtil.logInfo('Pesan validasi di halaman: ' + ambilPesan())

	// ===== Verifikasi: tetap di Checkout, tidak lanjut ke Pembayaran =====
	String urlSekarang = WebUI.getUrl()
	KeywordUtil.logInfo('URL setelah klik: ' + urlSekarang)
	if (!urlSekarang.contains('checkout')) {
		bug = true
		alasanBug += 'pelanggan diarahkan ke halaman lain (' + urlSekarang + ') tanpa alamat; '
	}

	// ===== Verifikasi: tidak ada pesanan baru =====
	int invoiceAkhir = hitungInvoice()
	KeywordUtil.logInfo("Jumlah invoice setelah uji: ${invoiceAkhir}")
	if (invoiceAwal >= 0 && invoiceAkhir >= 0 && invoiceAkhir != invoiceAwal) {
		bug = true
		alasanBug += 'pesanan baru tercatat tanpa alamat (' + invoiceAwal + ' menjadi ' + invoiceAkhir + '); '
	}
	WebUI.takeScreenshot(shot + 'TC-09.3_Gambar3_riwayat_pesanan.png')
} catch (Exception e) {
	WebUI.takeScreenshot(shot + 'TC-09.3_ERROR.png')
	KeywordUtil.markFailed('TC-09.3 berhenti: ' + e.getMessage())
} finally {
	WebUI.closeBrowser()
}

if (bug) {
	KeywordUtil.markFailed('TC-09.3 FAIL (BUG): checkout tanpa alamat tetap diproses: ' + alasanBug)
} else {
	KeywordUtil.markPassed('TC-09.3 PASS: checkout tanpa alamat ditolak, pelanggan tetap di Checkout dan tidak ada pesanan baru.')
}