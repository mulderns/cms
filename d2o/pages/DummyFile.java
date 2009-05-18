package d2o.pages;

public class DummyFile extends CmsFile {

	public DummyFile(CmsFile cmsFile) {
		super(cmsFile);
		type = Type.DUMMY;
	}

//	public DummyFile(IndexRecord ir) {
//		super(ir);
//		type = Type.DUMMY;
//	}
	
	public String getData() {
		return "dummy file has no data"; 
	}
	
}
