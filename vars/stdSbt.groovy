def call(configOverrides) {

  def config = [
     releaseBranch: ['*/master','master'],
     autoVersionRelease: false 
  ]

  configOverrides.resolveStrategy = Closure.DELEGATE_FIRST
  configOverrides.delegate = config
  
  configOverrides()

  def branchName = scm.branches[0].name //env.BRANCH_NAME

  def isReleaseBranch = config.releaseBranch.contains(branchName)

  echo "std sbt called with config ${config} - ${isReleaseBranch} - ${branchName}"  

  try {
    node('master') {  
    	stage('checkout') {
    		def gitparams = checkout scm
        echo "scm " + gitparams
        for (String i : gitparams) {
          println i
        }
    	}
      stage('Build') {
	      echo "Std Build"

      }
      stage('Test') {
        echo "Std Test" 	
      }
      

      if (isReleaseBranch) {
      	if (!config.autoVersionRelease) {
      		stage("Confirm Release") {
              echo "mkdir"
              sh 'mkdir -p target/releaselink'
              def giturl = scm.GIT_URL
              echo "write file - " + giturl

              def currentVersion = 'v1.0.0'
              def htmlString = '<a href="https://github.com/andersbohn/jdemoprj/releases/new?tag='+currentVersion+'">Rel-' + env.BRANCH_NAME + '</a>'              
              addHtmlBadge html: htmlString, id: 'releaselink'

            /*def proceedWithRelease = true
            try { 
      			  milestone()
              link()
      			  //input(id: "Confirmation", message: "Confirm Release")
      			  milestone()
            } catch(e) {
              proceedWithRelease = false
            }
            if (proceedWithRelease) {
              stage("Release") {
                echo "Released by confirmation"
              }
            }*/
      		}
      	} else {
      	  stage("Release") {
            echo "Auto Released"
          }
        }

      } else {
      	stage("Publish") {
		      echo "Std Publish"	
      	}
      }
      stage("Clean") {
	     	echo "Std Clean"	
      }
    }
  } catch(err) {
  	echo "error - cleaning up ${err}"
  	stage("Clean") {
	  echo "Std Clean After error"	
    }
  } 
}

def binding = getBinding()
def manager = binding.getVariable("manager")

def link() {
  input(id: 'Confirmation', message: 'Add tag in git then confirm Release')
  //manager.createSummary("warning.gif").appendText("<h1>You have been warned!</h1>", false, false, false, "red") 
}

