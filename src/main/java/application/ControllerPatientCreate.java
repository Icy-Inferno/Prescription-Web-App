package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import application.model.Patient;
import application.model.Doctor;
import application.model.*;
import application.service.*;
import view.*;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 */
@Controller
public class ControllerPatientCreate {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	@Autowired
	private SequenceService sequenceService;

	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String getNewPatientForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_register";
	}

	/*
	 * Process data from the patient_register form
	 */
	@PostMapping("/patient/new")
	public String createPatient(PatientView p, Model model) {
		// find doctor by last name
		Doctor doctor = doctorRepository.findByLastName(p.getPrimaryName());

		if (doctor == null) {
			// doctor not found
			model.addAttribute("message", "Doctor not found. Please check the doctor's last name.");
			model.addAttribute("patient", p);
			return "patient_register";
		}

		// new patient entity from PatientView
		Patient patient = Patient.fromView(p);
		patient.setPrimaryName(doctor.getLastName());
		patient.setId(sequenceService.getNextSequence("PATIENT_SEQUENCE")); 

		// save patient entity
		patientRepository.save(patient);

		// update with the new patients ID
		p.setId(patient.getId());

		// message and patient data
		model.addAttribute("message", "Registration successful.");
		model.addAttribute("patient", p);
		return "patient_show";
	}

	/*
	 * Request blank form to search for patient by id and name
	 */
	@GetMapping("/patient/edit")
	public String getSearchForm(Model model) {
		model.addAttribute("patient", new PatientView());
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String showPatient(PatientView p, Model model) {
		// find patient by ID and last name
		Patient patient = patientRepository.findByIdAndLastName(p.getId(), p.getLastName());

		if (patient != null) {
			// map Patient entity to PatientView
			p = mapPatientToView(patient);

			model.addAttribute("patient", p);
			return "patient_show";
		} else {
			// patient not found
			model.addAttribute("message", "Patient not found. Please verify the ID and last name.");
			model.addAttribute("patient", p);
			return "patient_get";
		}
	}

	/*
	 * helper method to map a Patient entity to a PatientView
	 */
	private PatientView mapPatientToView(Patient patient) {
		PatientView pv = new PatientView();
		pv.setId(patient.getId());
		pv.setLastName(patient.getLastName());
		pv.setFirstName(patient.getFirstName());
		pv.setBirthdate(patient.getBirthdate());
		pv.setSsn(patient.getSsn());
		pv.setStreet(patient.getStreet());
		pv.setCity(patient.getCity());
		pv.setState(patient.getState());
		pv.setZipcode(patient.getZipcode());
		pv.setPrimaryName(patient.getPrimaryName());
		return pv;
	}
}
