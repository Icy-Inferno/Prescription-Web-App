package application;

import application.model.*;
import application.service.SequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import view.PrescriptionView;

@Controller
public class ControllerPrescriptionCreate {

    @Autowired
    PrescriptionRepository prescriptionRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    SequenceService sequenceService;
    @Autowired
    private DrugRepository drugRepository;

    /*
     * Doctor requests blank form for new prescription.
     */
    @GetMapping("/prescription/new")
    public String getPrescriptionForm(Model model) {
        model.addAttribute("prescription", new PrescriptionView());
        return "prescription_create";
    }

    // process data entered on prescription_create form
    @PostMapping("/prescription")
    public String createPrescription(PrescriptionView p, Model model) {
        System.out.println("prescription");

        Doctor doctor = doctorRepository.findByIdAndFirstNameAndLastName(p.getDoctorId(), p.getDoctorFirstName(), p.getDoctorLastName());
        if (doctor == null) {
            model.addAttribute("message","doctor not found");
            model.addAttribute("prescription", p);
            return "prescription_create";
        }

        Patient patient = patientRepository.findByIdAndFirstNameAndLastName(p.getPatientId(), p.getPatientFirstName(), p.getPatientLastName());
        if (patient == null) {
            model.addAttribute("message","patient not found");
            model.addAttribute("prescription", p);
            return "prescription_create";
        }

        Drug drug = drugRepository.findByName(p.getDrugName());
        if(drug == null){
            model.addAttribute("message", "patient not found");
            model.addAttribute("prescription", p);
            return "prescription_create";
        }

        Prescription prescription = new Prescription();
        prescription.setRxid(sequenceService.getNextSequence("PRESCRIPTION_SEQUENCE"));
        prescription.setDoctorId(doctor.getId());
        prescription.setPatientId(patient.getId());
        prescription.setDrugName(drug.getName());
        prescription.setDateCreated(java.time.LocalDate.now().toString());
        prescription.setQuantity(p.getQuantity());
        prescription.setRefills(p.getRefillsRemaining());

        prescriptionRepository.save(prescription);
        p.setRxid(prescription.getRxid());
        model.addAttribute("message", "prescription created");
        model.addAttribute("prescription", p);
        return "prescription_create";
    }


}