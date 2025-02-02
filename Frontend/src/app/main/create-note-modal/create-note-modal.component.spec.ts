import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateNoteModalComponent } from './create-note-modal.component';

describe('CreateNoteModalComponent', () => {
  let component: CreateNoteModalComponent;
  let fixture: ComponentFixture<CreateNoteModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CreateNoteModalComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateNoteModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
