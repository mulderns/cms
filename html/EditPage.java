package html;

public class EditPage extends CmsPage{

	//public ArrayList<String> menu;
	//public ArrayList<String> center;
	
	public EditPage(String title) {
		super(title);
		//menu = new ArrayList<String>();
		//center = new ArrayList<String>();
	}
	
	public void add(String line){
		center.add(line);
	}
	
	public void addMenu(String line){
		left.add(line);
	}

	public String getBody() {
		StringBuilder sb = new StringBuilder();
		
		/*sb.append("<div class=\"wrapper\"><div class=\"top\">\n");*/

		for(String item: left){
			sb.append(item);
		}
		
		/*sb.append("</div>\n<div class=\"center\">");*/
		sb.append("<div class=\"content\">\n");
				
		for(String unit: center){
			sb.append(unit);
		}
		
		sb.append("</div>\n");

		return sb.toString();
	}
	
}
/**
<div class="wrapper">
<div class="top">

---
<dl class="drop">
<dt>Toiminnot</dt>
<dd><a href="#">Etusivu</a></dd>
<dd><a href="#">Viikko-ohjelma</a></dd>
<dd><a href="#">Käyttäjien hallinta</a></dd>
<dd><a href="#">Ylläpito</a></dd>
<dd><a href="#">Tiedostot</a></dd>
</dl>
<p>&#187;</p>
<dl class="drop">
<dt><a href="#">Sivujen hallinta</a></dt>
<dd><a href="#">Hallitse</a></dd>
<dd><a href="#">Päivitä</a></dd>
<dd><a href="#">Rendaa</a></dd>
<dd><a href="#">Asetukset</a></dd>
</dl>
<p>&#187; Muokkaus</p>
<div class="rest">
<a href="#">settings</a>
<a href="#">Kirjaudu ulos</a>
</div>
---

</div>

<div class="center">
*/