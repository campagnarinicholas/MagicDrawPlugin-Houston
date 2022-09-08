from flask import Flask,request
CAMEO_ROOT = "C:\Program Files\MagicDraw Demo"
app = Flask(__name__)

@app.route("/result",methods=["POST", "GET"])
def result():
    xmlFilename = request.args.get('xml')
    if xmlFilename is None or len(xmlFilename) == 0:
        return {"Status": request.args}
    xml = parseXML(xmlFilename)

    return {"xml":xml}

def parseXML(xmlFilename):
    path = CAMEO_ROOT + xmlFilename
    with open(path, 'r') as f:
        data = f.read()

    return data
    
if __name__ == '__main__':
    app.run(debug=True)