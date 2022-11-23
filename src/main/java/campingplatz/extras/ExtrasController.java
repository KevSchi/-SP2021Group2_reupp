package campingplatz.extras;

import static org.salespointframework.core.Currencies.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.javamoney.moneta.Money;
import org.salespointframework.inventory.UniqueInventory;
import org.salespointframework.inventory.UniqueInventoryItem;
import org.salespointframework.quantity.Quantity;
import org.salespointframework.useraccount.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import campingplatz.booking.Booking;
import campingplatz.booking.RentedObject;

import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.inventory.InventoryItem;

@Controller
class ExtrasController{
    @Autowired OrderManagement<Order> orderManagement;
	private static final Quantity NONE = Quantity.of(0);
	private final UniqueInventory<UniqueInventoryItem> extrasInventory;
	private final ExtrasCatalog extrasCatalog;

    ExtrasController(ExtrasCatalog extrasCatalog, UniqueInventory<UniqueInventoryItem> extrasInventory) {
		this.extrasCatalog = extrasCatalog;
		this.extrasInventory = extrasInventory;
	}

    @GetMapping("/extras/{extra}")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String detail(@PathVariable Extras extra, Model model) {

		var quantity = extrasInventory.findByProductIdentifier(extra.getId()) //
				.map(InventoryItem::getQuantity) //
				.orElse(NONE);

		model.addAttribute("extra", extra);
		model.addAttribute("quantity", quantity);
		model.addAttribute("orderable", quantity.isGreaterThan(NONE));
		return "extrasdetail";
	}

	
	@GetMapping("/bookextra/{order}/{extra}")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	public String bookextras(Model model, @PathVariable Order order, @PathVariable Extras extra) {
		System.out.println("ExtrasController.bookekextras()" + extra);
		model.addAttribute("extra",extra);
		model.addAttribute("order", order);
		return "bookextra";
	}

	@GetMapping("/bookextras/{user}/{order}")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	public String bookextras(Model model, @PathVariable UserAccount user, @PathVariable Order order) {
		
		model.addAttribute("extrasCatalog", extrasCatalog.findAll());
		model.addAttribute("user", user);
		model.addAttribute("order", order);
		return "bookextras";
	}

	@GetMapping("/extras")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	public String extras(Model model) {
		model.addAttribute("extrasCatalog", extrasCatalog.findAll());
		return "extras";
	}


	@PostMapping("/extra/{id}/")//wir benutzen die ID aus der Pfadvariable garnicht?
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	public String changePrice(@RequestParam("extra") Extras extra, @RequestParam("price") String money,
			@RequestParam("extraquantity") Quantity extraquantity) {

		money = money.replaceAll("[^0-9.]", "");

		BigDecimal validAmount = new BigDecimal(money);
		Money m2 = Money.of(validAmount, "EUR");

		UniqueInventoryItem id = extrasInventory.findByProductIdentifier(extra.getId()).get();

		id.decreaseQuantity(id.getQuantity());
		id.increaseQuantity(extraquantity);
		extra.setQuantity(extraquantity);
		extra.setPrice(m2);
		extrasCatalog.save(extra);
		return "redirect:/extras/";
	}

	@PostMapping("/createExtra")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	public String createExtra(@RequestParam("name") String name, @RequestParam(value="image", required = false) MultipartFile multipartFile, @RequestParam("price") Double price,
			@RequestParam("quantity") Quantity quantity, @RequestParam(value="returnable", defaultValue = "false") boolean returnable, @RequestParam(value="imagename", defaultValue = "") String imagename, @RequestParam(value="isRequired", defaultValue = "false") boolean required, @RequestParam(value = "paidDaily", defaultValue = "false") boolean paidDaily) throws IOException {
		
		if(quantity.isLessThan(Quantity.of(0)))	return "redirect:/orders";
		if(price.isNaN()) return "redirect:/orders";
		if(price < 0) return "redirect:/orders";
		String fileFormat ="";
		if(multipartFile != null){
		if(multipartFile.getContentType().equals("image/png")){fileFormat = ".png";}
		if(multipartFile.getContentType().equals("image/jpg")){fileFormat = ".jpg";}
		if(multipartFile.getContentType().equals("image/jpeg")){fileFormat = ".jpeg";}
		}
		String uploadDir = "src/main/resources/static/resources/img";
		String fileName;
		try {
			if(!fileFormat.equals("")) fileName = FileUploadUtil.saveFile(uploadDir, name, fileFormat, multipartFile);
			else{fileName = imagename;}
			Extras temp = new Extras(name, fileName, Money.of(price, EURO),quantity,returnable, required, paidDaily);
			extrasCatalog.save(temp);
			extrasInventory.save(new UniqueInventoryItem(temp, quantity));
		} catch (Exception e) {
			System.out.println(e);		
		}

		
		return "redirect:/extras";
	}
	@PostMapping("/returnextra")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String returnextra(@RequestParam("id") UUID id,@RequestParam("order") Booking booking) {
		Extras extra = null;
		RentedObject object = null;
		for (RentedObject rentedObject : booking.getExtras()) {
			if(rentedObject.getId().equals(id))object = rentedObject;
		}
		for (Extras item : extrasCatalog.findByName(object.getName())) {
			extra = item;
		}
		
		object.setReturned(true);
		object.setDateReturned(LocalDate.now());
		extra.returnExtra(object.getQuantity());		//increase extra by bookingquant
		extrasCatalog.save(extra);
		return "redirect:/orders";
	}

	@PostMapping("/deleteextra")
	@PreAuthorize("hasRole('BOSS')"+
	"|| hasRole('EMPLOYEE')")
	String deleteextra(@RequestParam("id") UUID id,@RequestParam("order") Booking booking) {
		System.out.println("remove");
		Extras extra = null;
		RentedObject object = null;
		for (RentedObject rentedObject : booking.getExtras()) {
			if(rentedObject.getId().equals(id))object = rentedObject;
		}
		for (Extras item : extrasCatalog.findByName(object.getName())) {
			extra = item;
		}
		
		object.setReturned(true);
		extra.returnExtra(object.getQuantity());		//increase extra by bookingquant
		extrasCatalog.save(extra);
		
		booking.getExtras().remove(object);
		orderManagement.save(booking);
		return "redirect:/orders";
	}
	
}