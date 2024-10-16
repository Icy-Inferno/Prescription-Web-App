package application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import application.model.Patient;
import application.model.Doctor;
import application.model.*;
import view.*;
/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatientUpdate {

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private DoctorRepository doctorRepository;

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{id}")
	public String getUpdateForm(@PathVariable int id, Model model) {
		Patient patient = patientRepository.findById(id).orElse(null);

		if (patient == null) {
			// patient not found
			model.addAttribute("message", "Patient not found.");
			return "index";
		}

		// map the patient entity to PatientView
		PatientView pv = mapPatientToView(patient);

		model.addAttribute("patient", pv);
		model.addAttribute("message", "Patient profile found.");
		return "patient_edit";		 		
	}

	/*
	 * Process changes from patient_edit form
	 *  Primary doctor, street, city, state, zip can be changed
	 *  ssn, patient id, name, birthdate, ssn are read only in template.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(PatientView p, Model model) {
		Doctor doctor = doctorRepository.findByLastName(p.getPrimaryName());

		if (doctor == null) {
			// doctor not found
			model.addAttribute("message", "Doctor not found. Verify the doctors last name.");
			model.addAttribute("patient", p);
			return "patient_edit";
		}

		// find patient by ID and update the details
		Patient patient = patientRepository.findById(p.getId()).orElse(null);
		if (patient == null) {
			model.addAttribute("message", "Patient not found.");
			return "index";
		}

		// update patient profile details
		Patient updatedPatient = Patient.fromView(p);
		updatedPatient.setPrimaryName(doctor.getLastName()); 

		// save updated patient entity
		patientRepository.save(updatedPatient);

		// success message updated patient data
		model.addAttribute("message", "Profile updated successfully.");
		model.addAttribute("patient", p);
		return "patient_show";
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
