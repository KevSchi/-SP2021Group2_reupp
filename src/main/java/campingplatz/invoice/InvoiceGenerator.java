package campingplatz.invoice;


import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import javax.servlet.http.HttpServletResponse;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.FontSelector;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.javamoney.moneta.format.CurrencyStyle;

import campingplatz.booking.Booking;
import campingplatz.booking.RentedObject;
import campingplatz.user.User;


public class InvoiceGenerator {
	public static void createInvoice(HttpServletResponse response, Booking order, User user) {
		InvoiceGenerator generateInvoice = new InvoiceGenerator();

		generateInvoice.createPDF(response, order, user);

	}

	final MonetaryAmountFormat format = MonetaryFormats.getAmountFormat(
      AmountFormatQueryBuilder.of(Locale.GERMANY)
        .set(CurrencyStyle.NAME)
        .build()
    );
  
  
	private void createPDF (HttpServletResponse response, Booking order, User user){
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");
			Document document = new Document();
			PdfWriter.getInstance(document , response.getOutputStream());
			//Inserting Image in PDF
            
			//Ist Schuld das JAR nicht ging
			Image image = Image.getInstance ("src/main/resources/static/resources/img/campsite.jpg");//Header Image
			image.scaleAbsolute(530, 160);//image width,height 

			PdfPTable irdTable = new PdfPTable(2);
			irdTable.addCell(getIRDCell("Zahlungsart"));
			irdTable.addCell(getIRDCell("Datum"));
			irdTable.addCell(getIRDCell(order.getPayment().toString())); // pass invoice number
			irdTable.addCell(getIRDCell(formatter.format(order.getDateCreated()))); // pass invoice date				

			PdfPTable irhTable = new PdfPTable(3);
			irhTable.setWidthPercentage(100);
			
			irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT));
			irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT));
			irhTable.addCell(getIRHCellsmall("Rechnung "  + order.getId(), PdfPCell.ALIGN_RIGHT));
			irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT));
			irhTable.addCell(getIRHCell("", PdfPCell.ALIGN_RIGHT));
			PdfPCell invoiceTable = new PdfPCell (irdTable);
			invoiceTable.setBorder(0);
			irhTable.addCell(invoiceTable);

			FontSelector fs = new FontSelector();
			Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 13, Font.BOLD);
			fs.addFont(font);
			Phrase bill = fs.process("Kunde"); // customer information
			Paragraph name = new Paragraph(user.getName());
			name.setIndentationLeft(20);
			Paragraph contact = new Paragraph(user.getStreet());
			contact.setIndentationLeft(20);
			Paragraph address = new Paragraph(user.getPlz() + " " + user.getCity());
			address.setIndentationLeft(20);

			PdfPTable billTable = new PdfPTable(6); //one page contains 15 records 
			billTable.setWidthPercentage(100);
			billTable.setWidths(new float[] { 2, 2,10,4,3,4 });
			billTable.setSpacingBefore(30.0f);
			billTable.addCell(getBillHeaderCell("Index"));
			billTable.addCell(getBillHeaderCell("Item"));
			billTable.addCell(getBillHeaderCell("Beschreibung"));
			billTable.addCell(getBillHeaderCell("Stückpreis"));
			billTable.addCell(getBillHeaderCell("Menge"));
			billTable.addCell(getBillHeaderCell("Gesamtpreis"));
            int i = 1;
			billTable.addCell(getBillRowCell(String.valueOf(i)));
			billTable.addCell(getBillRowCell("Platz"));
			
			billTable.addCell(getBillRowCell(order.getCampingSite() +" " +formatter.format(order.getStartDate()) + " - " + formatter.format(order.getEndDate())));
			billTable.addCell(getBillRowCell(format.format(order.getPrice())));
			billTable.addCell(getBillRowCell(String.valueOf(order.getDuration())));
			billTable.addCell(getBillRowCell(format.format(order.getCompleteSiteCostsUndiscounted())));
            i++;
            for(RentedObject rented : order.getExtras()){
                billTable.addCell(getBillRowCell(String.valueOf(i)));
                billTable.addCell(getBillRowCell("Extra"));
                billTable.addCell(getBillRowCell(rented.getName()));
                billTable.addCell(getBillRowCell(format.format(rented.getPrice())));
                billTable.addCell(getBillRowCell(rented.getQuantity().toString()));
                billTable.addCell(getBillRowCell(format.format(rented.getTotal())));
                i++;
            }
			
			

			PdfPTable validity = new PdfPTable(1);
			validity.setWidthPercentage(100);
			validity.addCell(getValidityCell(" "));
			if(order.getOrderStatus().toString().equals("COMPLETED"))validity.addCell(getValidityCell("Bezahlt am: "+ order.getDateCreated().getDayOfMonth() +"." +order.getDateCreated().getMonthValue() +"." +order.getDateCreated().getYear() +"\nDer Stückpreis sowie der Gesamtpreis sind Bruttopreise. Der Rabatt gilt auf den Nettopreis des gebuchten Platzes.\n"));
			else if(order.getPayment().toString().equals("Überweisung"))validity.addCell(getValidityCell("Der Stückpreis sowie der Gesamtpreis sind Bruttopreise. Der Rabatt gilt auf den Nettopreis des gebuchten Platzes.\n\nZahlung innerhalb von 14 Tagen ab Rechnungserhalt ohne Abzüge an die folgende Bankverbindung.\nMusterbank Musterstadt\nIBAN: DE34233004333401\nBIC: GENODE61FR1\nKto. Inh.: Dr. Mr. Sun"));
			else if(order.getPayment().toString().equals("Bar"))validity.addCell(getValidityCell("Der Stückpreis sowie der Gesamtpreis sind Bruttopreise. Der Rabatt gilt auf den Nettopreis des gebuchten Platzes.\n\nZahlung innerhalb von 14 Tagen ab Rechnungserhalt ohne Abzüge an der Kasse von Below-The-Sun"));
		
			PdfPCell summaryL = new PdfPCell (validity);
			summaryL.setColspan (3);
			summaryL.setPadding (1.0f);	                   
			billTable.addCell(summaryL);

			String rabatt = "";
			if(order.isInGroup()) rabatt = " (5%)";
			else{rabatt = " (0%)";}
			PdfPTable accounts = new PdfPTable(2);
			accounts.setWidthPercentage(100);
			accounts.addCell(getAccountsCell("Preis"));
			accounts.addCell(getAccountsCellR(format.format(order.getCompleteCostsUndiscountedWithoutTaxMoney())));
			accounts.addCell(getAccountsCell("Rabatt" + rabatt));
			accounts.addCell(getAccountsCellR(format.format(order.getDiscount())));
			accounts.addCell(getAccountsCell("Steuer (7%)"));
			accounts.addCell(getAccountsCellR(format.format(order.getTax())));
			accounts.addCell(getAccountsCell("Gesamt"));
			accounts.addCell(getAccountsCellR(order.getCompleteCostsDiscountedMoneyRounded()));			
			PdfPCell summaryR = new PdfPCell (accounts);
			summaryR.setColspan (3);         
			billTable.addCell(summaryR);  

			PdfPTable describer = new PdfPTable(1);
			describer.setWidthPercentage(100);
			describer.addCell(getdescCell(" "));
			describer.addCell(getdescCell("Vielen Dank für ihren Besuch! Die Firma Dankt $_$"));	

			document.open();//PDF document opened........	

			document.add(image);
			document.add(irhTable);
			document.add(bill);
			document.add(name);
			document.add(contact);
			document.add(address);			
			document.add(billTable);
			document.add(describer);

			document.close();

			System.out.println("Pdf erstellt");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setHeader() {

	}

	public static PdfPCell getIRHCellsmall(String text, int alignment) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
		/*	font.setColor(BaseColor.GRAY);*/
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setPadding(5);
		cell.setHorizontalAlignment(alignment);
		cell.setBorder(PdfPCell.NO_BORDER);
		return cell;
	}
	public static PdfPCell getIRHCell(String text, int alignment) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 16);
		/*	font.setColor(BaseColor.GRAY);*/
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell(phrase);
		cell.setPadding(5);
		cell.setHorizontalAlignment(alignment);
		cell.setBorder(PdfPCell.NO_BORDER);
		return cell;
	}

	public static PdfPCell getIRDCell(String text) {
		PdfPCell cell = new PdfPCell (new Paragraph (text));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setPadding (5.0f);
		cell.setBorderColor(BaseColor.LIGHT_GRAY);
		return cell;
	}

	public static PdfPCell getBillHeaderCell(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 11);
		font.setColor(BaseColor.GRAY);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setPadding (5.0f);
		return cell;
	}

	public static PdfPCell getBillRowCell(String text) {
		PdfPCell cell = new PdfPCell (new Paragraph (text));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setPadding (5.0f);
		cell.setBorderWidthBottom(0);
		cell.setBorderWidthTop(0);
		return cell;
	}

	public static PdfPCell getBillFooterCell(String text) {
		PdfPCell cell = new PdfPCell (new Paragraph (text));
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setPadding (5.0f);
		cell.setBorderWidthBottom(0);
		cell.setBorderWidthTop(0);
		return cell;
	}

	public static PdfPCell getValidityCell(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
		font.setColor(BaseColor.GRAY);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);		
		cell.setBorder(0);
		return cell;
	}

	public static PdfPCell getAccountsCell(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);		
		cell.setBorderWidthRight(0);
		cell.setBorderWidthTop(0);
		cell.setPadding (5.0f);
		return cell;
	}
	public static PdfPCell getAccountsCellR(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);		
		cell.setBorderWidthLeft(0);
		cell.setBorderWidthTop(0);
		cell.setHorizontalAlignment (Element.ALIGN_RIGHT);
		cell.setPadding (5.0f);
		cell.setPaddingRight(20.0f);
		return cell;
	}

	public static PdfPCell getdescCell(String text) {
		FontSelector fs = new FontSelector();
		Font font = FontFactory.getFont(FontFactory.HELVETICA, 10);
		font.setColor(BaseColor.GRAY);
		fs.addFont(font);
		Phrase phrase = fs.process(text);
		PdfPCell cell = new PdfPCell (phrase);	
		cell.setHorizontalAlignment (Element.ALIGN_CENTER);
		cell.setBorder(0);
		return cell;
	}

}