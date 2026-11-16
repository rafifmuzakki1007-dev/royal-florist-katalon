import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

// TC-09.2 (revisi) : Opsi Transfer Bank belum tersedia pada halaman Checkout
// Fitur Transfer Bank belum diimplementasikan. Test memastikan opsi tampil "Belum tersedia",
// tidak dapat dipilih, QRIS tetap terpilih, dan tidak ada pesanan dibuat (tidak klik Lanjut Bayar).

// ===================== KONFIGURASI =====================
String base = 'http://127.0.0.1:8000'
String emailPelanggan = 'parmi@gmail.com'
String passPelanggan  = '12345678'
String alamatText    = 'Jl. Melati No. 12'
String alamatLengkap = 'Jl. Melati No. 12, Nganjuk'
int tungguManualDetik = 90
String shot = RunConfiguration.getProjectDir() + '/Docs/Screenshot/S09_Checkout/'
// =======================================================

def obj = { String xp ->
	TestObject t = new TestObject('obj_' + Math.abs(xp.hashCode()))
	t.addProperty('xpath', ConditionType.EQUALS, xp)
	return t
}

def klik = { String xp ->
	if (WebUI.verifyElementPresent(obj(xp), 3, FailureHandling.OPTIONAL)) {
		WebUI.click(obj(xp), FailureHandling.OPTIONAL)
		return true
	}
	return false
}

def teksHalaman = { -> WebUI.getText(obj('//body')) }

String jsPilihan = '''
var teks = arguments[0];
var rs = Array.from(document.querySelectorAll('input[type=radio]')).filter(function(e) {
	var n = e;
	for (var i = 0; i < 6 && n; i++) {
		if (n.innerText && n.innerText.length < 250 && n.innerText.indexOf(teks) >= 0
			&& n.querySelectorAll('input[type=radio]').length === 1) { return true; }
		n = n.parentElement;
	}
	return false;
});
if (rs.length === 0) { return 'TIDAK_ADA_RADIO'; }
return rs[0].checked ? 'TERPILIH' : 'TIDAK_TERPILIH';
'''
def statusPilihan = { String t -> WebUI.executeJavaScript(jsPilihan, [t]) }

def teksTombolBayar = {
	return WebUI.executeJavaScript("var b = document.getElementById('cta'); return b ? b.innerText.trim() : ''", null)
}

String jsTransfer = '''
var daun = Array.from(document.querySelectorAll('*')).filter(function(e) {
	return e.children.length === 0 && e.innerText && e.innerText.trim() === 'Transfer Bank';
});
if (daun.length === 0) { return 'OPSI TRANSFER BANK TIDAK ADA DI HALAMAN'; }
var n = daun[0];
while (n.parentElement && n.parentElement.innerText.length < 250 && n.parentElement.innerText.indexOf('QRIS') < 0) {
	n = n.parentElement;
}
var rb = n.querySelector('input[type=radio]');
var st = window.getComputedStyle(n);
return 'teks=' + n.innerText.replace(/\\s+/g, ' ').trim() + ' | radioAda=' + (rb != null)
	+ ' | radioDisabled=' + (rb ? rb.disabled : 'n/a') + ' | opacity=' + st.opacity
	+ ' | pointerEvents=' + st.pointerEvents + ' | class=' + n.className;
'''

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
		WebUI.takeScreenshot(shot + 'TC-09.2_LOGIN_gagal.png')
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

def tambahAlamatOtomatis = {
	try {
		if (!klik("//*[self::button or self::a][contains(normalize-space(),'Tambah Alamat Baru')]")) { return false }
		WebUI.delay(2)
		def terisi = WebUI.executeJavaScript('''
var alamat = arguments[0];
function terlihat(e) { return !!(e.offsetWidth || e.offsetHeight || e.getClientRects().length); }
function petunjuk(e) {
	var t = (e.name || '') + ' ' + (e.id || '') + ' ' + (e.placeholder || '');
	var lab = e.id ? document.querySelector('label[for="' + e.id + '"]') : null;
	if (lab) { t += ' ' + lab.innerText; }
	var p = e.closest('label');
	if (p) { t += ' ' + p.innerText; }
	return t.toLowerCase();
}
function setNilai(e, v) {
	var proto = e.tagName === 'TEXTAREA' ? window.HTMLTextAreaElement.prototype : window.HTMLInputElement.prototype;
	Object.getOwnPropertyDescriptor(proto, 'value').set.call(e, v);
	e.dispatchEvent(new Event('input', {bubbles: true}));
	e.dispatchEvent(new Event('change', {bubbles: true}));
}
var hasil = [];
document.querySelectorAll('input, textarea, select').forEach(function(e) {
	if (!terlihat(e) || e.disabled || e.readOnly) { return; }
	var tipe = (e.type || '').toLowerCase();
	if (['hidden', 'submit', 'button', 'checkbox', 'radio', 'file', 'password', 'search'].indexOf(tipe) >= 0) { return; }
	var h = petunjuk(e);
	if (e.tagName === 'SELECT') {
		for (var i = 0; i < e.options.length; i++) {
			if (e.options[i].value !== '') { e.selectedIndex = i; break; }
		}
		e.dispatchEvent(new Event('change', {bubbles: true}));
		hasil.push('select:' + h.trim());
		return;
	}
	if (e.value && e.value.trim() !== '') { return; }
	var v;
	if (h.indexOf('telp') >= 0 || h.indexOf('hp') >= 0 || h.indexOf('phone') >= 0 || h.indexOf('whatsapp') >= 0 || tipe === 'tel') { v = '081234567890'; }
	else if (h.indexOf('pos') >= 0) { v = '64411'; }
	else if (h.indexOf('nama') >= 0 || h.indexOf('penerima') >= 0) { v = 'Rina Oktavia'; }
	else if (h.indexOf('provinsi') >= 0) { v = 'Jawa Timur'; }
	else if (h.indexOf('alamat') >= 0 || h.indexOf('jalan') >= 0 || h.indexOf('detail') >= 0 || e.tagName === 'TEXTAREA') { v = alamat; }
	else { v = 'Nganjuk'; }
	setNilai(e, v);
	hasil.push(h.trim() + ' = ' + v);
});
return hasil;
''', [alamatLengkap])
		KeywordUtil.logInfo('Kolom alamat terisi otomatis: ' + terisi)
		klik("(//button[contains(normalize-space(),'Simpan')])[last()]")
		WebUI.delay(3)
		return !teksHalaman().contains('Belum ada alamat pengiriman')
	} catch (Exception e) {
		KeywordUtil.logInfo('Tambah alamat otomatis gagal: ' + e.getMessage())
		return false
	}
}

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

WebUI.openBrowser('')
WebUI.maximizeWindow()

try {
	// ===== Precondition: pelanggan login, keranjang berisi produk, alamat tersimpan =====
	loginPelanggan()

	// ===== Langkah 1: Buka halaman Checkout dari Keranjang =====
	bukaCheckout()
	def r = ringkasan()
	if ((r[0] as int) <= 0) {
		KeywordUtil.logInfo('Keranjang kosong, menambahkan produk dari Katalog')
		tambahProdukOtomatis()
		bukaCheckout()
		r = ringkasan()
		if ((r[0] as int) <= 0) {
			WebUI.takeScreenshot(shot + 'TC-09.2_Gambar0_keranjang_kosong.png')
			boolean adaProduk = tunggu({ (ringkasan()[0] as int) > 0 },
				'Keranjang masih kosong. TAMBAHKAN PRODUK lalu buka halaman Checkout di jendela browser ini (skrip menunggu).')
			if (!adaProduk) {
				KeywordUtil.markFailedAndStop('Keranjang kosong dan produk tidak berhasil ditambahkan.')
			}
		}
	}
	if (teksHalaman().contains('Belum ada alamat pengiriman')) {
		boolean alamatOk = tambahAlamatOtomatis()
		if (!alamatOk) {
			alamatOk = tunggu({ !teksHalaman().contains('Belum ada alamat pengiriman') },
				'TAMBAHKAN ALAMAT di jendela browser ini (isi form lalu Simpan). Skrip menunggu.')
		}
		if (!alamatOk) {
			KeywordUtil.markFailedAndStop('Alamat pengiriman tidak berhasil ditambahkan.')
		}
		bukaCheckout()
	}
	WebUI.takeScreenshot(shot + 'TC-09.2_Gambar1_halaman_checkout.png')

	// ===== Langkah 2: Pilih alamat pengiriman =====
	boolean alamatDiklik = klik("//*[contains(normalize-space(text()),'" + alamatText + "')]")
	WebUI.delay(1)
	String stAlamat = statusPilihan(alamatText)
	KeywordUtil.logInfo("Alamat: diklik=${alamatDiklik} status=${stAlamat}")
	WebUI.verifyEqual(alamatDiklik, true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(stAlamat != 'TIDAK_TERPILIH', true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.takeScreenshot(shot + 'TC-09.2_Gambar2_alamat_dipilih.png')

	// ===== Langkah 3: Klik opsi Transfer Bank =====
	klik("//*[normalize-space(text())='Dikirim Kurir Florist']")
	String detail = WebUI.executeJavaScript(jsTransfer, null)
	KeywordUtil.logInfo('DETAIL OPSI TRANSFER BANK: ' + detail)
	WebUI.scrollToElement(obj("//*[normalize-space(text())='Transfer Bank']"), 3, FailureHandling.OPTIONAL)
	WebUI.takeScreenshot(shot + 'TC-09.2_Gambar3_opsi_transfer_bank.png')

	// Verifikasi: opsi tampil "Belum tersedia" dan tidak punya pilihan radio
	WebUI.verifyEqual(detail.contains('Belum tersedia'), true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(detail.contains('radioAda=false'), true, FailureHandling.CONTINUE_ON_FAILURE)

	klik("//*[normalize-space(text())='Transfer Bank']")
	WebUI.delay(1)
	String ctaTeks = teksTombolBayar()
	KeywordUtil.logInfo('Teks tombol bayar setelah klik Transfer Bank: ' + ctaTeks)
	String ctaLower = ctaTeks.toLowerCase()
	// Verifikasi: tombol tetap QRIS (Transfer Bank tidak terpilih)
	WebUI.verifyEqual(ctaLower.contains('qris'), true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.verifyEqual(ctaLower.contains('transfer'), false, FailureHandling.CONTINUE_ON_FAILURE)
	// Verifikasi: tetap di halaman Checkout, tidak ada pesanan dibuat
	WebUI.verifyMatch(WebUI.getUrl(), '.*checkout.*', true, FailureHandling.CONTINUE_ON_FAILURE)
	WebUI.takeScreenshot(shot + 'TC-09.2_Gambar4_setelah_klik_transfer_bank.png')
} catch (Exception e) {
	WebUI.takeScreenshot(shot + 'TC-09.2_ERROR.png')
	KeywordUtil.markFailed('TC-09.2 berhenti: ' + e.getMessage())
} finally {
	WebUI.closeBrowser()
}