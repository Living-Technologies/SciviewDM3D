terminal.addClasses();
function importer( fullName ){
	tokens = fullName.split(".");
	className = tokens[ tokens.length - 1];
	eval(className + " = Java.type('" + fullName + "');");
	echo(className + " imported");	
}

function makeTransparentMaterial(mesh,color){

        var material = new DefaultMaterial();
        material.setDiffuse(color);
        material.setAmbient(color);
        material.setSpecular(new Vector3f(1, 1, 1));
        var b = new Blending();
        b.setOpacity(0.1);
		b.setTransparent(true);
		b.setOverlayBlending();
        material.setBlending(b);
        mesh.setMaterial(material);
    }


importer("graphics.scenery.Blending");
importer("graphics.scenery.attribute.material.DefaultMaterial");
importer("graphics.scenery.attribute.material.Material");

mesh1 = TestMeshes.barbell(3);
mesh2 = TestMeshes.generateSphere(3);

sciView.addMesh(mesh1);
sciView.addMesh(mesh2);

mesh1.setPosition( new Vector3f(0.3, 0.3, 0.3) );
c1 = new Vector3f(0.7, 0.7, 1);
makeTransparentMaterial(mesh1,c1);
c2 = new Vector3f(1, 0.7, 0.7);
mesh2.setPosition( new Vector3f(-0.3, -0.3, -0.3) );
makeTransparentMaterial(mesh2, c2);

