import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { DriverRegistrationComponent } from './driver-registration';
import { DriverDataService } from '../services/driver-data';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('DriverRegistrationComponent', () => {
  let component: DriverRegistrationComponent;
  let fixture: ComponentFixture<DriverRegistrationComponent>;
  let driverDataService: jasmine.SpyObj<DriverDataService>;
  let router: jasmine.SpyObj<Router>;

  //priprema, kreira se komponenta i mock servisi
  beforeEach(async () => {
    //kreiramo spy objekte za mock servise DriverDataService i Router
    const driverDataServiceSpy = jasmine.createSpyObj('DriverDataService', [
      'setDriverData',
      'getDriverData',
      'getPhotoPreview'
    ]);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    //konfigurisemo okruzenje
    await TestBed.configureTestingModule({
      imports: [
        DriverRegistrationComponent,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        MatInputModule,
        MatFormFieldModule,
        MatButtonModule
      ],
      providers: [
        { provide: DriverDataService, useValue: driverDataServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    //kreiramo instancu komponente
    fixture = TestBed.createComponent(DriverRegistrationComponent);
    component = fixture.componentInstance;
    driverDataService = TestBed.inject(DriverDataService) as jasmine.SpyObj<DriverDataService>;
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    //podrazumevano nema sacuvanih podataka
    driverDataService.getDriverData.and.returnValue(null);
    driverDataService.getPhotoPreview.and.returnValue(null);

    fixture.detectChanges();
  });

  // ==================== KREIRANJE KOMPONENTE ====================
  //provera da li se komponenta uspesno kreirala
  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // ==================== INICIJALIZACIJA FORME ====================
  //provera da li sva polja postoje i da li su inicijalno prazna
  it('should initialize form with empty values and validators', () => {
    //da li forma postoji
    expect(component.registerForm).toBeDefined();
    //da li postoje polja
    expect(component.registerForm.get('name')).toBeDefined();
    expect(component.registerForm.get('surname')).toBeDefined();
    expect(component.registerForm.get('email')).toBeDefined();
    expect(component.registerForm.get('address')).toBeDefined();
    expect(component.registerForm.get('phoneNumber')).toBeDefined();

    //da li su na pocetku prazna
    expect(component.registerForm.get('name')?.value).toBe('');
    expect(component.registerForm.get('surname')?.value).toBe('');
    expect(component.registerForm.get('email')?.value).toBe('');
    expect(component.registerForm.get('address')?.value).toBe('');
    expect(component.registerForm.get('phoneNumber')?.value).toBe('');
  });

  //provera da li su sva polja oznacena kao obavezna
  it('should have all fields marked as required', () => {
    const nameControl = component.registerForm.get('name');
    const surnameControl = component.registerForm.get('surname');
    const emailControl = component.registerForm.get('email');
    const addressControl = component.registerForm.get('address');
    const phoneControl = component.registerForm.get('phoneNumber');

    expect(nameControl?.hasError('required')).toBe(true);
    expect(surnameControl?.hasError('required')).toBe(true);
    expect(emailControl?.hasError('required')).toBe(true);
    expect(addressControl?.hasError('required')).toBe(true);
    expect(phoneControl?.hasError('required')).toBe(true);
  });

  // ==================== VALIDACIJE - NEGATIVNI TESTOVI ====================
  //provera da li forma nije validna kada je ime prazno
  it('should invalidate form when name is empty', () => {
    component.registerForm.patchValue({
      name: '',
      surname: 'Petrovic',
      email: 'petar@gmail.com.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.invalid).toBe(true);
    expect(component.registerForm.get('name')?.hasError('required')).toBe(true);
  });

  //ime prazno
  it('should invalidate form when surname is empty', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: '',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.invalid).toBe(true);
    expect(component.registerForm.get('surname')?.hasError('required')).toBe(true);
  });

  //email prazan
  it('should invalidate form when email is empty', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrovic',
      email: '',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.invalid).toBe(true);
    expect(component.registerForm.get('email')?.hasError('required')).toBe(true);
  });

  //provera kada je format mejla neispravan
  it('should invalidate form when email format is incorrect', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrović',
      email: 'invalid-email',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.invalid).toBe(true);
    expect(component.registerForm.get('email')?.hasError('email')).toBe(true);
  });

  //adresa prazna
  it('should invalidate form when address is empty', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: '',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.invalid).toBe(true);
    expect(component.registerForm.get('address')?.hasError('required')).toBe(true);
  });

  //broj telefona prazan
  it('should invalidate form when phone number is empty', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: ''
    });

    expect(component.registerForm.invalid).toBe(true);
    expect(component.registerForm.get('phoneNumber')?.hasError('required')).toBe(true);
  });

  //provera da forma niej validna kada su sva polja prazna
  it('should invalidate form when all fields are empty', () => {
    expect(component.registerForm.invalid).toBe(true);
  });

  // ==================== VALIDACIJE - POZITIVNI TESTOVI ====================
  //sva polja su ispravno popunjena
  it('should validate form when all fields are correctly filled', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.valid).toBe(true);
  });

  //provera da li forma prihvata razlicite email formate
  it('should accept valid email formats', () => {
    const validEmails = [
      'test@example.com',
      'user.name@example.co.rs',
      'user+tag@domain.com',
      'user_name@sub.domain.com'
    ];

    validEmails.forEach(email => {
      component.registerForm.patchValue({
        name: 'Petar',
        surname: 'Petrovic',
        email: email,
        address: 'Bulevar Kralja Aleksandra 73',
        phoneNumber: '0641234567'
      });

      expect(component.registerForm.get('email')?.hasError('email')).toBe(false);
    });
  });

  // ==================== UPLOAD SLIKE - POZITIVNI TESTOVI ====================
  //provera da li radi ispravno upload slike
  it('should handle photo selection correctly', () => {
    const mockFile = new File(['dummy content'], 'test.png', { type: 'image/png' });
    const mockEvent = {
      target: {
        files: [mockFile]
      }
    };

    //spy na FileReader, mokujemo citanje fajla
    spyOn(window as any, 'FileReader').and.returnValue({
      readAsDataURL: function() {
        this.onload({ target: { result: 'data:image/png;base64,mockBase64' } });
      }
    });

    component.onPhotoSelect(mockEvent);

    //proveravamo da su fajl i preview postavljeni
    expect(component.selectedPhoto).toBe(mockFile);
    expect(component.photoPreview).toBe('data:image/png;base64,mockBase64');
  });

  //provera da li se preview slike postavlja nakon selektovanja slike
  it('should set photo preview when photo is selected', (done) => {
    const mockFile = new File(['dummy content'], 'test.png', { type: 'image/png' });
    const mockEvent = {
      target: {
        files: [mockFile]
      }
    };

    component.onPhotoSelect(mockEvent);

    setTimeout(() => {
      expect(component.photoPreview).toBeTruthy();
      done();
    }, 100);
  });

  // ==================== UKLANJANJE SLIKE ====================
  //provera da li se slika uspesno uklanja
  it('should remove photo when onRemovePhoto is called', () => {
    //prvo postavimo sliku
    component.selectedPhoto = new File(['dummy'], 'test.png', { type: 'image/png' });
    component.photoPreview = 'data:image/png;base64,mockBase64';

    //uklonimo je
    component.onRemovePhoto();

    //provera jesu li obe vrednosti null
    expect(component.selectedPhoto).toBeNull();
    expect(component.photoPreview).toBeNull();
  });

  //provera da forma moze biti poslata i bez slike jer slika nije obavezna
  it('should allow form submission without photo', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    component.selectedPhoto = null;
    component.photoPreview = null;

    expect(component.registerForm.valid).toBe(true);
  });

  // ==================== NEXT DUGME - SUBMIT ====================
  //provera da se podaci pravilno prosledjuju i provera navigacije na next stranicu
  it('should call setDriverData and navigate when form is valid', () => {
    const formData = {
      name: 'Petar',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    };

    component.registerForm.patchValue(formData);
    component.photoPreview = 'data:image/png;base64,mockBase64';

    component.onNext();

    //provera da je servis pozvan sa ispravnim parametrima
    expect(driverDataService.setDriverData).toHaveBeenCalledWith(
      formData,
      'data:image/png;base64,mockBase64'
    );
    //proveravamo je l navigacija ispravna
    expect(router.navigate).toHaveBeenCalledWith(['/vehicle-registration']);
  });

  //provera da se podaci ne salju kad forma nije validna
  it('should not call setDriverData when form is invalid', () => {
    component.registerForm.patchValue({
      name: '',
      surname: '',
      email: 'invalid-email',
      address: '',
      phoneNumber: ''
    });

    component.onNext();

    //provera da servisi nisu pozvani
    expect(driverDataService.setDriverData).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });

  //provera da se salje null za sliku ako nije izabrana
  it('should send null photo if no photo is selected', () => {
    const formData = {
      name: 'Petar',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    };

    component.registerForm.patchValue(formData);
    component.photoPreview = null;

    component.onNext();

    //provera da li je poslat null za sliku
    expect(driverDataService.setDriverData).toHaveBeenCalledWith(formData, null);
  });

  // ==================== NGONIT - UCITAVANJE SACUVANIH PODATAKA ====================
  //proverava da se podaci ucitavaju pri inicijalizaciji, testiramo dugme back kad se vrati sa vozila
  it('should load saved driver data on init if available', () => {
    const savedData = {
      name: 'Marko',
      surname: 'Markovic',
      email: 'marko@gmail.com',
      address: 'Kneza Miloša 10',
      phoneNumber: '0629876543'
    };
    const savedPhoto = 'data:image/jpeg;base64,savedPhoto';

    //mockujemo da servis vrati sacuvane podatke
    driverDataService.getDriverData.and.returnValue(savedData);
    driverDataService.getPhotoPreview.and.returnValue(savedPhoto);

    component.ngOnInit();

    //proveravamo da su podaci ucitani u formu
    expect(component.registerForm.value).toEqual(savedData);
    expect(component.photoPreview).toBe(savedPhoto);
  });

  //provera da je forma prazna ako nema sacuvanih podataka
  it('should not populate form if no saved data exists', () => {
    driverDataService.getDriverData.and.returnValue(null);
    driverDataService.getPhotoPreview.and.returnValue(null);

    component.ngOnInit();

    //proveravamo da polja ostaju prazna
    expect(component.registerForm.get('name')?.value).toBe('');
    expect(component.photoPreview).toBeNull();
  });

  // ==================== UI - DUGME DISABLED STATE ====================
  //provera da nije moguce next dugme kad podaci nisu validni
  it('should disable Next button when form is invalid', () => {
    fixture.detectChanges();
    const button: HTMLButtonElement = fixture.debugElement.query(
      By.css('.register-btn')
    ).nativeElement;

    expect(button.disabled).toBe(true);
  });

  //provera da se dugme omoguci kada su podaci validni
  it('should enable Next button when form is valid', () => {
    component.registerForm.patchValue({
      name: 'Petar',
      surname: 'Petrović',
      email: 'petar@example.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    fixture.detectChanges();

    const button: HTMLButtonElement = fixture.debugElement.query(
      By.css('.register-btn')
    ).nativeElement;

    expect(button.disabled).toBe(false);
  });

  // ==================== GRANICNI SLUCAJEVI ====================
  //ako se odabre vise slika uzastopno, ostaje samo poslednja
  it('should handle multiple photo selections - last one should be kept', () => {
    const mockFile1 = new File(['content1'], 'test1.png', { type: 'image/png' });
    const mockFile2 = new File(['content2'], 'test2.png', { type: 'image/png' });

    spyOn(window as any, 'FileReader').and.returnValue({
      readAsDataURL: function() {
        this.onload({ target: { result: 'data:image/png;base64,latestPhoto' } });
      }
    });

    component.onPhotoSelect({ target: { files: [mockFile1] } });
    component.onPhotoSelect({ target: { files: [mockFile2] } });

    //provera da je zadrzana poslenja
    expect(component.selectedPhoto).toBe(mockFile2);
  });

  //provera da ostaju razmaci kao sto korisnik unese
  it('should trim whitespace is not applied (test real behavior)', () => {
    component.registerForm.patchValue({
      name: '  Petar  ',
      surname: 'Petrovic',
      email: 'petar@gmail.com',
      address: 'Bulevar Kralja Aleksandra 73',
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.get('name')?.value).toBe('  Petar  ');
  });

  //provera da forma prihvata dugacke stringove tipa za adresu moze sta hoce da napise
  it('should accept very long valid inputs', () => {
    const longString = 'A'.repeat(500);

    component.registerForm.patchValue({
      name: longString,
      surname: longString,
      email: 'valid@example.com',
      address: longString,
      phoneNumber: '0641234567'
    });

    expect(component.registerForm.valid).toBe(true);
  });

  //provera da getter f vraca ispravne form controls
  it('should provide form controls via f getter', () => {
  expect(component.f['name']).toBe(component.registerForm.get('name')!);
  expect(component.f['surname']).toBe(component.registerForm.get('surname')!);
  expect(component.f['email']).toBe(component.registerForm.get('email')!);
  expect(component.f['address']).toBe(component.registerForm.get('address')!);
  expect(component.f['phoneNumber']).toBe(component.registerForm.get('phoneNumber')!);
});
});