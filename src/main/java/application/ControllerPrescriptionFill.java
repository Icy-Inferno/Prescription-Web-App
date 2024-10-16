package application;

import application.model.Pharmacy.DrugCost;
import application.model.Prescription.FillRequest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.sql.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import application.model.*;
import application.service.*;
import view.*;

@Controller
public class ControllerPrescriptionFill {

	@Autowired
	PrescriptionRepository prescriptionRepository;

	@Autowired
	SequenceService sequence;
  @Autowired
  private PharmacyRepository pharmacyRepository;
  @Autowired
  private PatientRepository patientRepository;
  @Autowired
  private DoctorRepository doctorRepository;

	/*
	 * Patient requests form to fill prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new PrescriptionView());
		return "prescription_fill";
	}

	// process data from prescription_fill form
	@PostMapping("/prescription/fill")
	public String processFillForm(PrescriptionView p, Model model) {

		boolean isRefill = false;

		/*
		 * valid pharmacy name and address, get pharmacy id and phone
		 */
		Pharmacy pharmacy = pharmacyRepository.findByNameAndAddress(p.getPharmacyName(),
				p.getPharmacyAddress());
		if (pharmacy == null) {
			model.addAttribute("message", "Pharmacy not found. Please enter a"
					+ " valid pharmacy name and address.");
      model.addAttribute("prescription", p);
      return "prescription_fill";
		} else {
			p.setPharmacyID(pharmacy.getId());
			p.setPharmacyPhone(pharmacy.getPhone());
      System.out.println("end getPharmacyInfo " + p);
		}

		// find the patient information
		Patient patient = patientRepository.findByLastName(p.getPatientLastName());
		if (patient == null) {
			model.addAttribute("message", "Patient not found. Please enter a"
					+ " valid patient name.");
      model.addAttribute("prescription", p);
      return "prescription_fill";
		} else {
      p.setPatientId(patient.getId());
      p.setPatientFirstName(patient.getFirstName());
      System.out.println("end getPatientInfo " + p); // debug
    }

		// find the prescription
    // create a model.prescription instance
    Prescription prescription = prescriptionRepository.findByRxid(p.getRxid());
    if (prescription == null) {
      model.addAttribute("message", "Prescription not found. Please enter"
          + " a valid Rx ID.");
      model.addAttribute("prescription", p);
      return "prescription_fill";
    } else {
      p.setDrugName(prescription.getDrugName());
      p.setQuantity(prescription.getQuantity());
      p.setRefillsRemaining(prescription.getRefills());
      System.out.println("end getPrescriptionInfo " + p);  // debug
    }

		/*
		 * have we exceeded the number of allowed refills
		 * the first fill is not considered a refill.
		 */
    if (p.getRefillsRemaining() == 0) {
      model.addAttribute("message", "Unable to fill prescription. Maximum"
          + " refills reached.");
      model.addAttribute("prescription", p);
      return "prescription_fill";
    } else if (p.getRefillsRemaining() > 0) {
				isRefill = true;
    }
			System.out.println("end getRefillInfo "+ p);  // debug

		/*
		 * get doctor information
	 	 */
		Doctor doctor = doctorRepository.findById(prescription.getDoctorId());
		if (doctor != null) {
			p.setDoctorId(doctor.getId());
			p.setDoctorFirstName(doctor.getFirstName());
			p.setDoctorLastName(doctor.getLastName());
			System.out.println("end getDoctorInfo " + p);  // debug
		}

		/*
		 * calculate cost of prescription
		 */
    double pricePerUnit = 0;
    ArrayList<DrugCost> drugCosts = pharmacy.getDrugCosts();
    for (DrugCost drugCost : drugCosts) {
      if (prescription.getDrugName().equals(drugCost.getDrugName())) {
        pricePerUnit = drugCost.getCost();
        break;
      }
    }
    DecimalFormat decimalFormat = new DecimalFormat("0.00");
    p.setCost("$" + decimalFormat.format(pricePerUnit * p.getQuantity()));
    System.out.println("end getCostInfo "+p);  // debug

    // save updated prescription
    Date curDate = new Date(System.currentTimeMillis());
    p.setDateFilled(curDate.toString());

    if (isRefill) {
      p.setRefillsRemaining(p.getRefillsRemaining() - 1);
      // prescription_show.html passes "refills" rather than "refillsRemaining" to the
      // "Refills remaining:" display on the page, so this next line is necessary. Is this a bug?
      p.setRefills(p.getRefillsRemaining());
    }

    // copy data from PrescriptionView to model
    FillRequest fillRequest = new FillRequest();
    fillRequest.setDateFilled(p.getDateFilled());
    fillRequest.setCost(p.getCost());
    fillRequest.setPharmacyID(p.getPharmacyID());
    prescription.getFills().add(fillRequest);
    prescription.setRefills(p.getRefills());
    // update prescription
		prescriptionRepository.save(prescription);
		// show the updated prescription with the most recent fill information
		model.addAttribute("message", "Prescription filled.");
		model.addAttribute("prescription", p);
		return "prescription_show";
	}

}