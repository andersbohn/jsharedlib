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
    		scmResult = checkout scm
        echo "scm " + scmResult
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
              
              
              echo "write file - " + scmResult.GIT_URL

              def currentVersion = 'v1.0.0'

              echo "mkdir"
              sh 'mkdir -p target/buildsummary'
              echo "write buildsummary"
              
              def htmlString = '<html><body>Create release for ' + env.BRANCH_NAME + ' -> <a href="https://github.com/andersbohn/jdemoprj/releases/new?tag='+currentVersion+'">on github release page</a></body></html>'
              sh 'echo "'+htmlString+'" > target/buildsummary/index.html'
              echo "publish Html "
              publishHTML target: [
                          allowMissing:true,
                          alwaysLinkToLastBuild: false,
                          keepAll:true,
                          reportDir: 'target/buildsummary',
                          reportFiles: 'index.html',
                          reportName: 'BuildSummary'
                      ]

              echo "version link - " + currentVersion 
              def versionHtml = '<a href="'+ env.BUILD_NUMBER + '/BuildSummary">' + currentVersion + '</p>'
              addHtmlBadge html: versionHtml, id: 'versionlink'

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