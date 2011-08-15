package crowdsource.mturk;

import java.util.List;

import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.service.axis.RequesterService;

/**
 * The Reviewer sample application will retrieve the completed assignments for a given HIT,
 * output the results and approve the assignment.
 *
 * mturk.properties must be found in the current file path.
 * You will need to have the HIT ID of an existing HIT that has been accepted, completed and
 * submitted by a worker.
 * You will need to have the .success file generated from bulk loading several HITs (i.e. Site Category sample application).
 *
 * The following concepts are covered:
 * - Retrieve results for a HIT
 * - Output results for several HITs to a file
 * - Approve assignments
 */
public class PhraseTranslateReviewer extends Reviewer {
  public PhraseTranslateReviewer(String file) {
    super(file);
  }

  @SuppressWarnings("unchecked")
  /**
   * Prints the submitted results of a HIT when provided with a HIT ID.
   * @param hitId The HIT ID of the HIT to be retrieved.
   */
  public void reviewAnswers(String hitId) {
    Assignment[] assignments = service.getAllAssignmentsForHIT(hitId);

    System.out.println("--[Reviewing HITs]----------");
    System.out.println("  HIT Id: " + hitId);
    
    System.err.println("size:"+assignments.length);
    for (Assignment assignment : assignments) {

      //Only assignments that have been submitted will contain answer data
      if (assignment.getAssignmentStatus() == AssignmentStatus.Submitted) {
    	  System.err.println("Submitted hit");
        //By default, answers are specified in XML
        String answerXML = assignment.getAnswer();

        //Calling a convenience method that will parse the answer XML and extract out the question/answer pairs.
        QuestionFormAnswers qfa = RequesterService.parseAnswers(answerXML);
        List<QuestionFormAnswersType.AnswerType> answers = qfa.getAnswer();
          //(List<QuestionFormAnswersType.AnswerType>) 

        System.err.println(answers.size());
        
        for (QuestionFormAnswersType.AnswerType answer : answers) {
          String assignmentId = assignment.getAssignmentId();
          String answerValue = RequesterService.getAnswerValue(assignmentId, answer);

          if (answerValue != null) {
            System.out.println("Got an answer \"" + answerValue
                + "\" from worker " + assignment.getWorkerId() + ".");
          }
        }
        //Approving the assignment.
        // service.approveAssignment(assignment.getAssignmentId(), "Well Done!");
        // service.rejectAssignment(assignment.getAssignmentId(), "Rejecting Output!");
        // service.extendHIT(assignment.getAssignmentId(), 1, new Long(1));
        service.disableHIT(hitId); 
        
        System.out.println("Approved.");
      }
    }
    System.out.println("--[End Reviewing HITs]----------");
  }
}
