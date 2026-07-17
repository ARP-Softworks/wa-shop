import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AdminDashboardPageComponent } from './admin-dashboard-page.component';
import { AdminDashboardApiService } from '../../../core/api/admin-dashboard-api.service';

describe('AdminDashboardPageComponent', () => {
  let fixture: ComponentFixture<AdminDashboardPageComponent>;
  let dashboardApi: jasmine.SpyObj<AdminDashboardApiService>;

  beforeEach(async () => {
    dashboardApi = jasmine.createSpyObj('AdminDashboardApiService', ['getDashboard']);
    dashboardApi.getDashboard.and.returnValue(
      of({
        publishedProducts: 2,
        newDevices: 1,
        usedDevices: 1,
        pendingInquiries: 0,
        activeTechnicalServices: 3,
        recentProducts: [],
        recentInquiries: [],
        activeServices: [],
      })
    );

    await TestBed.configureTestingModule({
      imports: [AdminDashboardPageComponent],
      providers: [provideRouter([]), { provide: AdminDashboardApiService, useValue: dashboardApi }],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardPageComponent);
    fixture.detectChanges();
  });

  it('loads dashboard stats on init', () => {
    expect(dashboardApi.getDashboard).toHaveBeenCalled();
    expect(fixture.componentInstance.state().status).toBe('success');
    expect(fixture.componentInstance.state().data?.publishedProducts).toBe(2);
  });

  it('shows error state when API fails', () => {
    dashboardApi.getDashboard.and.returnValue(throwError(() => ({ status: 500 })));
    fixture.componentInstance.load();
    fixture.detectChanges();
    expect(fixture.componentInstance.state().status).toBe('error');
  });
});
