db.drug.drop();
db.pharmacy.drop();

db.drug.insertMany([
	{_id: 1, name: "lisinopril"},
	{_id: 2, name: "loratadine"},
	{_id: 3, name: "acetaminophen"},
	{_id: 4, name: "lovastatin"}
]);
print("Drugs :");
print(db.drug.find());
print('');

db.pharmacy.insertMany([
	{_id: 1, name: "cvs", address: "123 main", phone: "813-774-1200", drugCosts: [{drugName: "lisinopril", cost: 7.5}]},
	{_id: 2, name: "Some Pharmacy", address: "123 Some Street", phone: "123-456-7890", drugCosts: [{drugName: "lisinopril", cost: 5.50}]},
	{_id: 3, name: "Some Other Pharmacy", address: "123 Some Otherstreet", phone: "234-567-8901", drugCosts: [{drugName: "lisinopril", cost: 6.50}]}
]);
print("Pharmacies:");
print(db.pharmacy.find());
print('');