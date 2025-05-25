import { expect, jest } from '@jest/globals';
import { TestBed } from '@angular/core/testing';
import { LocalStorageService } from 'ngx-webstorage';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  let service: ThemeService;
  let localStorageService: LocalStorageService;

  // Mock matchMedia
  const originalMatchMedia = window.matchMedia;

  beforeEach(() => {
    // Mock localStorage service
    const mockLocalStorageService = {
      retrieve: jest.fn(),
      store: jest.fn(),
      clear: jest.fn(),
    };

    // Mock matchMedia with proper type casting
    const mockMediaQueryList = {
      matches: false,
      addEventListener: jest.fn(),
    };

    window.matchMedia = jest.fn().mockImplementation(() => mockMediaQueryList) as unknown as typeof window.matchMedia;

    TestBed.configureTestingModule({
      providers: [ThemeService, { provide: LocalStorageService, useValue: mockLocalStorageService }],
    });

    service = TestBed.inject(ThemeService);
    localStorageService = TestBed.inject(LocalStorageService);
  });

  afterEach(() => {
    // Restore original matchMedia
    window.matchMedia = originalMatchMedia;
    jest.restoreAllMocks();
  });

  describe('getStoredTheme', () => {
    it('should return the stored theme if it is light', () => {
      jest.spyOn(localStorageService, 'retrieve').mockReturnValue('light');

      const result = service.getStoredTheme();

      expect(localStorageService.retrieve).toHaveBeenCalledWith('theme');
      expect(result).toBe('light');
    });

    it('should return the stored theme if it is dark', () => {
      jest.spyOn(localStorageService, 'retrieve').mockReturnValue('dark');

      const result = service.getStoredTheme();

      expect(localStorageService.retrieve).toHaveBeenCalledWith('theme');
      expect(result).toBe('dark');
    });

    it('should return system if the stored theme is neither light nor dark', () => {
      jest.spyOn(localStorageService, 'retrieve').mockReturnValue(null);

      const result = service.getStoredTheme();

      expect(localStorageService.retrieve).toHaveBeenCalledWith('theme');
      expect(result).toBe('system');
    });
  });

  describe('setTheme', () => {
    it('should update current theme and apply it', () => {
      // Create a spy on the private applyTheme method
      const applyThemeSpy = jest.spyOn(service as any, 'applyTheme');

      service.setTheme('dark');

      expect(applyThemeSpy).toHaveBeenCalledWith('dark');

      // Check if the observable emits the correct value
      service.currentTheme$.subscribe(theme => {
        expect(theme).toBe('dark');
      });
    });
  });

  describe('applyTheme', () => {
    let toggleMock: any;

    beforeEach(() => {
      // Mock document.documentElement.classList.toggle
      toggleMock = jest.spyOn(document.documentElement.classList, 'toggle');
      // Use a custom implementation that returns a boolean
      toggleMock.mockImplementation((token: string, force?: boolean) => {
        return !!force;
      });
    });

    afterEach(() => {
      toggleMock.mockRestore();
    });

    it('should apply dark theme when theme is dark', () => {
      // Access private method using type assertion
      (service as any).applyTheme('dark');

      expect(document.documentElement.classList.toggle).toHaveBeenCalledWith('dark', true);
      expect(localStorageService.store).toHaveBeenCalledWith('theme', 'dark');
    });

    it('should apply light theme when theme is light', () => {
      // Access private method using type assertion
      (service as any).applyTheme('light');

      expect(document.documentElement.classList.toggle).toHaveBeenCalledWith('dark', false);
      expect(localStorageService.store).toHaveBeenCalledWith('theme', 'light');
    });

    it('should apply system theme based on system preference (light)', () => {
      (window.matchMedia as jest.Mock).mockImplementation(() => ({
        matches: false,
        addEventListener: jest.fn(),
      }));

      // Access private method using type assertion
      (service as any).applyTheme('system');

      expect(document.documentElement.classList.toggle).toHaveBeenCalledWith('dark', false);
      expect(localStorageService.clear).toHaveBeenCalledWith('theme');
    });

    it('should apply system theme based on system preference (dark)', () => {
      (window.matchMedia as jest.Mock).mockImplementation(() => ({
        matches: true,
        addEventListener: jest.fn(),
      }));

      // Access private method using type assertion
      (service as any).applyTheme('system');

      expect(document.documentElement.classList.toggle).toHaveBeenCalledWith('dark', true);
      expect(localStorageService.clear).toHaveBeenCalledWith('theme');
    });
  });

  describe('constructor', () => {
    it('should initialize with stored theme and set up event listener', () => {
      // Reset test environment
      jest.clearAllMocks();
      TestBed.resetTestingModule();

      // Setup mocks first
      const mockLocalStorageService = {
        retrieve: jest.fn().mockReturnValue('system'),
        store: jest.fn(),
        clear: jest.fn(),
      };

      const eventListenerMock = jest.fn();
      const mockMediaQueryList = {
        matches: false,
        addEventListener: eventListenerMock,
      };

      window.matchMedia = jest.fn().mockImplementation(() => mockMediaQueryList) as unknown as typeof window.matchMedia;

      // Configure testing module with our mocks
      TestBed.configureTestingModule({
        providers: [ThemeService, { provide: LocalStorageService, useValue: mockLocalStorageService }],
      });

      // We need to create spy on applyTheme before service instantiation
      // Create a custom implementation of applyTheme to spy on
      const applyThemeSpy = jest.spyOn(ThemeService.prototype as any, 'applyTheme');
      // Use a simpler mock implementation with any parameters
      applyThemeSpy.mockImplementation(() => {
        // Empty mock implementation
      });

      // Create service instance - this will trigger constructor
      const testService = TestBed.inject(ThemeService);

      // Verify the constructor's behavior
      expect(mockLocalStorageService.retrieve).toHaveBeenCalledWith('theme');
      expect(applyThemeSpy).toHaveBeenCalledWith('system');
      expect(eventListenerMock).toHaveBeenCalledWith('change', expect.any(Function));

      // Clean up
      applyThemeSpy.mockRestore();
    });

    it('should update theme when system preference changes and theme is system', () => {
      // Reset test environment
      jest.clearAllMocks();
      TestBed.resetTestingModule();

      // Create mocks that capture the event listener callback
      let changeCallback: any;
      const eventListenerMock = jest.fn((event: string, callback: any) => {
        changeCallback = callback;
      });

      const mockMediaQueryList = {
        matches: false,
        addEventListener: eventListenerMock,
      };

      // Mock localStorage to return 'system'
      const mockLocalStorageService = {
        retrieve: jest.fn().mockReturnValue('system'),
        store: jest.fn(),
        clear: jest.fn(),
      };

      // Setup matchMedia mock
      window.matchMedia = jest.fn().mockImplementation(() => mockMediaQueryList) as unknown as typeof window.matchMedia;

      // Set up TestBed with mocks
      TestBed.configureTestingModule({
        providers: [ThemeService, { provide: LocalStorageService, useValue: mockLocalStorageService }],
      });

      // Create spy for applyTheme
      const applyThemeSpy = jest.spyOn(ThemeService.prototype as any, 'applyTheme');

      // Create service instance
      const testService = TestBed.inject(ThemeService);

      // Verify initial setup
      expect(eventListenerMock).toHaveBeenCalledWith('change', expect.any(Function));
      expect(applyThemeSpy).toHaveBeenCalledWith('system');

      // Clear the spy to only check calls after the event
      applyThemeSpy.mockClear();

      // Ensure we have a callback
      expect(changeCallback).toBeDefined();

      // Simulate the system preference change event
      changeCallback();

      // Verify applyTheme was called with 'system'
      expect(applyThemeSpy).toHaveBeenCalledWith('system');
    });
  });
});
